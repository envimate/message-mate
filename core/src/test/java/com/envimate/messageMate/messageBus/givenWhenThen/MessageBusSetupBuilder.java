/*
 * Copyright (c) 2018 envimate GmbH - https://envimate.com/.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.envimate.messageMate.messageBus.givenWhenThen;

import com.envimate.messageMate.channel.Channel;
import com.envimate.messageMate.channel.ChannelBuilder;
import com.envimate.messageMate.identification.CorrelationId;
import com.envimate.messageMate.internal.pipe.configuration.AsynchronousConfiguration;
import com.envimate.messageMate.messageBus.EventType;
import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.messageBus.MessageBusBuilder;
import com.envimate.messageMate.messageBus.MessageBusType;
import com.envimate.messageMate.messageBus.channelCreating.MessageBusChannelFactory;
import com.envimate.messageMate.messageBus.config.MessageBusTestConfig;
import com.envimate.messageMate.messageBus.exception.MessageBusExceptionHandler;
import com.envimate.messageMate.processingContext.ProcessingContext;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeMessageBusSutActions;
import com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.SetupAction;
import com.envimate.messageMate.shared.subscriber.SimpleTestSubscriber;
import com.envimate.messageMate.subscribing.Subscriber;
import com.envimate.messageMate.subscribing.SubscriptionId;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;

import static com.envimate.messageMate.channel.action.Subscription.subscription;
import static com.envimate.messageMate.identification.CorrelationId.newUniqueCorrelationId;
import static com.envimate.messageMate.messageBus.givenWhenThen.MessageBusTestActionsOld.messageBusTestActions;
import static com.envimate.messageMate.messageBus.givenWhenThen.MessageBusTestExceptionHandler.*;
import static com.envimate.messageMate.messageBus.givenWhenThen.MessageBusTestProperties.CORRELATION_SUBSCRIPTION_ID;
import static com.envimate.messageMate.messageBus.givenWhenThen.MessageBusTestProperties.MESSAGE_RECEIVED_BY_ERROR_LISTENER;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.EXPECTED_RECEIVERS;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.*;
import static com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeChannelMessageBusSharedTestProperties.*;
import static com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeMessageBusSetupActions.*;
import static com.envimate.messageMate.shared.subscriber.SimpleTestSubscriber.testSubscriber;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class MessageBusSetupBuilder {
    private final TestEnvironment testEnvironment = TestEnvironment.emptyTestEnvironment();
    private final List<SetupAction<MessageBus>> setupActions = new LinkedList<>();
    private final MessageBusBuilder messageBusBuilder = MessageBusBuilder.aMessageBus();


    public static MessageBusSetupBuilder aConfiguredMessageBus(final MessageBusTestConfig testConfig) {
        return new MessageBusSetupBuilder()
                .configuredWith(testConfig);
    }

    private MessageBusSetupBuilder configuredWith(final MessageBusTestConfig testConfig) {
        final MessageBusType type = testConfig.getType();
        final AsynchronousConfiguration asynchronousConfiguration = testConfig.getAsynchronousConfiguration();
        messageBusBuilder.forType(type)
                .withAsynchronousConfiguration(asynchronousConfiguration);
        storeSleepTimesInTestEnvironment(testConfig, testEnvironment);
        return this;
    }

    private void storeSleepTimesInTestEnvironment(final MessageBusTestConfig messageBusTestConfig, final TestEnvironment testEnvironment) {
        final long millisecondsSleepAfterExecution = messageBusTestConfig.getMillisecondsSleepAfterExecution();
        if (millisecondsSleepAfterExecution > 0) {
            testEnvironment.setProperty(SLEEP_AFTER_EXECUTION, millisecondsSleepAfterExecution);
        }
        final long millisecondsSleepBetweenExecutionActionSteps = messageBusTestConfig.getMillisecondsSleepBetweenExecutionActionSteps();
        if (millisecondsSleepBetweenExecutionActionSteps > 0) {
            testEnvironment.setProperty(SLEEP_BETWEEN_EXECUTION_STEPS, millisecondsSleepAfterExecution);
        }
    }

    public <T> MessageBusSetupBuilder withASubscriberForTyp(final EventType eventType) {
        setupActions.add((messageBus, testEnvironment) -> {
            final Subscriber<Object> subscriber = SimpleTestSubscriber.testSubscriber();
            MessageBusTestActions.addASingleSubscriber(messageBus, testEnvironment, eventType, subscriber);
        });
        return this;
    }

    public MessageBusSetupBuilder withACustomChannelFactory() {
        messageBusBuilder.withAChannelFactory(new MessageBusChannelFactory() {
            @Override
            public Channel<Object> createChannel(final EventType eventType, final Subscriber<?> subscriber, final MessageBusExceptionHandler exceptionHandler) {
                final Channel<Object> channel = ChannelBuilder.aChannel(Object.class)
                        .withDefaultAction(subscription())
                        .build();
                testEnvironment.setPropertyIfNotSet(EXPECTED_RESULT, channel);
                return channel;
            }
        });
        return this;
    }

    public MessageBusSetupBuilder withoutASubscriber() {
        return this;
    }

    public MessageBusSetupBuilder withASingleSubscriber() {
        setupActions.add((t, testEnvironment) -> {
            MessageBusTestActions.addASingleSubscriber(t, testEnvironment);
        });
        return this;
    }

    public MessageBusSetupBuilder withASingleRawSubscriber() {
        setupActions.add((t, testEnvironment) -> MessageBusTestActions.addASingleRawSubscriber(t, testEnvironment));
        return this;
    }

    public <T> MessageBusSetupBuilder withARawSubscriberForType(final EventType eventType) {
        setupActions.add((t, testEnvironment) -> MessageBusTestActions.addASingleRawSubscriber(t, testEnvironment, eventType));
        return this;
    }

    public MessageBusSetupBuilder withASingleSubscriber(final Class<?> clazz) {
        setupActions.add((t, testEnvironment) -> addASingleSubscriber(sutActions(t), testEnvironment, clazz));
        return this;
    }

    public MessageBusSetupBuilder withASubscriberForACorrelationId() {
        setupActions.add((t, testEnvironment) -> {
            final CorrelationId correlationId = newUniqueCorrelationId();
            final SimpleTestSubscriber<ProcessingContext<Object>> subscriber = testSubscriber();
            final MessageBus messageBus = testEnvironment.getPropertyAsType(SUT, MessageBus.class);
            final SubscriptionId subscriptionId = messageBus.subscribe(correlationId, subscriber);
            testEnvironment.setProperty(EXPECTED_CORRELATION_ID, correlationId);
            testEnvironment.setProperty(CORRELATION_SUBSCRIPTION_ID, subscriptionId);
            testEnvironment.addToListProperty(EXPECTED_RECEIVERS, subscriber);
        });
        return this;
    }

    public MessageBusSetupBuilder withSeveralSubscriber(final int numberOfSubscribers) {
        setupActions.add((t, testEnvironment) -> MessageBusTestActions.withSeveralSubscriber(t, testEnvironment, numberOfSubscribers));
        return this;
    }

    public MessageBusSetupBuilder withAFilterThatChangesTheContentOfEveryMessage() {
        setupActions.add((t, testEnvironment) -> addAFilterThatChangesTheContentOfEveryMessage(sutActions(t), testEnvironment));
        return this;
    }

    public MessageBusSetupBuilder withAFilterThatDropsMessages() {
        setupActions.add((t, testEnvironment) -> addAFilterThatDropsMessages(sutActions(t), testEnvironment));
        return this;
    }

    public MessageBusSetupBuilder withAnInvalidFilterThatDoesNotUseAnyFilterMethods() {
        setupActions.add((t, testEnvironment) -> addAnInvalidFilterThatDoesNotUseAnyFilterMethods(sutActions(t), testEnvironment));
        return this;
    }

    public MessageBusSetupBuilder withTwoFilterOnSpecificPositions() {
        setupActions.add((t, testEnvironment) -> addTwoFilterOnSpecificPositions(sutActions(t), testEnvironment));
        return this;
    }

    public MessageBusSetupBuilder withAFilterAtAnInvalidPosition(final int position) {
        setupActions.add((t, testEnvironment) -> addAFilterAtAnInvalidPosition(sutActions(t), testEnvironment, position));
        return this;
    }

    public MessageBusSetupBuilder withAnExceptionThrowingFilter() {
        setupActions.add((t, testEnvironment) -> addAFilterThatThrowsExceptions(sutActions(t), testEnvironment));
        return this;
    }

    public MessageBusSetupBuilder withASubscriberThatBlocksWhenAccepting() {
        setupActions.add((t, testEnvironment) -> MessageBusTestActions.addASubscriberThatBlocksWhenAccepting(t, testEnvironment));
        return this;
    }

    public MessageBusSetupBuilder withAnExceptionAcceptingSubscriber() {
        setupActions.add((t, testEnvironment) -> addAnExceptionAcceptingSubscriber(sutActions(t), testEnvironment));
        return this;
    }

    public MessageBusSetupBuilder withAnExceptionThrowingSubscriber() {
        setupActions.add((t, testEnvironment) -> MessageBusTestActions.addAErrorThrowingSubscriber(t, testEnvironment));
        return this;
    }

    public MessageBusSetupBuilder withACustomExceptionHandler() {
        messageBusBuilder.withExceptionHandler(allExceptionAsResultHandlingTestExceptionHandler(testEnvironment));
        return this;
    }

    public MessageBusSetupBuilder withACustomExceptionHandlerMarkingExceptionAsIgnored() {
        messageBusBuilder.withExceptionHandler(testExceptionAllowingExceptionHandler(testEnvironment));
        return this;
    }

    public MessageBusSetupBuilder withADynamicExceptionListenerForEventType() {
        messageBusBuilder.withExceptionHandler(allExceptionIgnoringExceptionHandler());
        setupActions.add((messageBus, testEnvironment) -> {
            MessageBusTestActions.addDynamicErrorListenerForEventType(messageBus, testEnvironment);
        });
        return this;
    }

    public MessageBusSetupBuilder withTwoDynamicExceptionListenerForEventType() {
        messageBusBuilder.withExceptionHandler(allExceptionIgnoringExceptionHandler());
        setupActions.add((messageBus, testEnvironment) -> {
            MessageBusTestActions.addTwoDynamicErrorListenerForEventType_whereTheFirstWillBeRemoved(messageBus, testEnvironment);
        });
        return this;
    }

    public MessageBusSetupBuilder withADynamicErrorListenerAndAnErrorThrowingExceptionHandler() {
        setupActions.add((messageBus, testEnvironment) -> {
            MessageBusTestActions.addDynamicErrorListenerForEventType(messageBus, testEnvironment);
        });
        return this;
    }

    public MessageBusSetupBuilder withAnErrorThrowingExceptionHandler() {
        return this;
    }

    public MessageBusSetupBuilder withADynamicCorrelationIdBasedExceptionListener() {
        messageBusBuilder.withExceptionHandler(allExceptionIgnoringExceptionHandler());
        setupActions.add((messageBus, testEnvironment) -> {
            final CorrelationId correlationId = newUniqueCorrelationId();
            testEnvironment.setProperty(EXPECTED_CORRELATION_ID, correlationId);
            final SubscriptionId subscriptionId = messageBus.onException(correlationId, (m, e) -> {
                this.testEnvironment.setPropertyIfNotSet(RESULT, e);
                this.testEnvironment.setPropertyIfNotSet(MESSAGE_RECEIVED_BY_ERROR_LISTENER, m);
            });
            this.testEnvironment.setProperty(USED_SUBSCRIPTION_ID, subscriptionId);
        });
        return this;
    }

    public MessageBusSetupBuilder withTwoDynamicCorrelationBasedExceptionListener() {
        messageBusBuilder.withExceptionHandler(allExceptionIgnoringExceptionHandler());
        setupActions.add((messageBus, testEnvironment) -> {
            final CorrelationId correlationId = CorrelationId.newUniqueCorrelationId();
            testEnvironment.setProperty(EXPECTED_CORRELATION_ID, correlationId);

            final SubscriptionId subscriptionId = messageBus.onException(correlationId, (m, e) -> {
                throw new RuntimeException("Should not be called");
            });
            this.testEnvironment.setProperty(USED_SUBSCRIPTION_ID, subscriptionId);

            messageBus.onException(correlationId, (m, e) -> {
                this.testEnvironment.setProperty(RESULT, e);
                this.testEnvironment.setPropertyIfNotSet(MESSAGE_RECEIVED_BY_ERROR_LISTENER, m);
            });
        });
        return this;
    }

    public MessageBusSetup build() {
        final MessageBus messageBus = messageBusBuilder.build();
        return MessageBusSetup.setup(messageBus, testEnvironment, setupActions);
    }

    private PipeMessageBusSutActions sutActions(final MessageBus messageBus) {
        return messageBusTestActions(messageBus);
    }
}
