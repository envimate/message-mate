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
import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.messageBus.MessageBusBuilder;
import com.envimate.messageMate.messageBus.MessageBusType;
import com.envimate.messageMate.messageBus.channelCreating.MessageBusChannelFactory;
import com.envimate.messageMate.messageBus.config.MessageBusTestConfig;
import com.envimate.messageMate.messageBus.exception.MessageBusExceptionHandler;
import com.envimate.messageMate.internal.pipe.configuration.AsynchronousConfiguration;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeMessageBusSutActions;
import com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.SetupAction;
import com.envimate.messageMate.shared.subscriber.SimpleTestSubscriber;
import com.envimate.messageMate.shared.testMessages.TestMessageOfInterest;
import com.envimate.messageMate.subscribing.Subscriber;
import com.envimate.messageMate.subscribing.SubscriptionId;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static com.envimate.messageMate.channel.action.Subscription.subscription;
import static com.envimate.messageMate.messageBus.givenWhenThen.MessageBusTestActions.messageBusTestActions;
import static com.envimate.messageMate.messageBus.givenWhenThen.MessageBusTestExceptionHandler.*;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.EXPECTED_RECEIVERS;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.*;
import static com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeMessageBusSetupActions.*;
import static com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeMessageBusTestProperties.*;
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

    public <T> MessageBusSetupBuilder withASubscriberForTyp(final Class<T> messageClass) {
        setupActions.add((messageBus, executionContext) -> {
            final SimpleTestSubscriber<T> subscriber = SimpleTestSubscriber.testSubscriber();
            messageBus.subscribe(messageClass, subscriber);
            executionContext.addToListProperty(EXPECTED_RECEIVERS, subscriber);
        });
        return this;
    }

    public MessageBusSetupBuilder withACustomChannelFactory() {
        messageBusBuilder.withAChannelFactory(new MessageBusChannelFactory() {
            @Override
            public <T> Channel<?> createChannel(final Class<T> tClass, final Subscriber<T> subscriber, final MessageBusExceptionHandler exceptionHandler) {
                final Channel<T> channel = ChannelBuilder.aChannel(tClass)
                        .withDefaultAction(subscription())
                        .build();
                if (testEnvironment.has(EXPECTED_RESULT)) {
                    throw new IllegalStateException();
                } else {
                    testEnvironment.setProperty(EXPECTED_RESULT, channel);
                }
                return channel;
            }
        });
        return this;
    }

    public MessageBusSetupBuilder withoutASubscriber() {
        return this;
    }

    public MessageBusSetupBuilder withASingleSubscriber() {
        setupActions.add((t, testEnvironment) -> addASingleSubscriber(sutActions(t), testEnvironment));
        return this;
    }

    public MessageBusSetupBuilder withASingleSubscriber(final Class<?> clazz) {
        setupActions.add((t, testEnvironment) -> addASingleSubscriber(sutActions(t), testEnvironment, clazz));
        return this;
    }

    public MessageBusSetupBuilder withSeveralSubscriber(final int numberOfReceivers) {
        setupActions.add((t, testEnvironment) -> addSeveralSubscriber(sutActions(t), testEnvironment, numberOfReceivers));
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
        setupActions.add((t, testEnvironment) -> addASubscriberThatBlocksWhenAccepting(sutActions(t), testEnvironment));
        return this;
    }

    public MessageBusSetupBuilder withAnExceptionAcceptingSubscriber() {
        setupActions.add((t, testEnvironment) -> addAnExceptionAcceptingSubscriber(sutActions(t), testEnvironment));
        return this;
    }

    public MessageBusSetupBuilder withAnExceptionThrowingSubscriber() {
        setupActions.add((t, testEnvironment) -> addAnExceptionThrowingSubscriber(sutActions(t), testEnvironment));
        return this;
    }

    public MessageBusSetupBuilder withACustomExceptionHandler() {
        messageBusBuilder.withExceptionHandler(allExceptionHandlingTestExceptionHandler(testEnvironment));
        return this;
    }

    public MessageBusSetupBuilder withADynamicExceptionListener() {
        messageBusBuilder.withExceptionHandler(allExceptionIgnoringExceptionHandler());
        setupActions.add((messageBus, testEnvironment1) -> {
            final SubscriptionId subscriptionId = messageBus.onException(TestMessageOfInterest.class, (m, e) -> {
                testEnvironment.setProperty(RESULT, e);
            });
            testEnvironment.setProperty(USED_SUBSCRIPTION_ID, subscriptionId);
        });
        return this;
    }

    public MessageBusSetupBuilder withTwoDynamicExceptionListener() {
        messageBusBuilder.withExceptionHandler(allExceptionIgnoringExceptionHandler());
        setupActions.add((messageBus, testEnvironment1) -> {

            final SubscriptionId subscriptionId = messageBus.onException(TestMessageOfInterest.class, (m, e) -> {
                throw new RuntimeException("Should not be called");
            });
            testEnvironment.setProperty(USED_SUBSCRIPTION_ID, subscriptionId);

            messageBus.onException(TestMessageOfInterest.class, (m, e) -> {
                testEnvironment.setProperty(RESULT, e);
            });
        });
        return this;
    }

    public MessageBusSetupBuilder withADynamicErrorListenerAndAnErrorThrowingExceptionHandler() {
        setupActions.add((messageBus, testEnvironment1) -> {
            messageBus.onException(TestMessageOfInterest.class, (m, e) -> {
                testEnvironment.setProperty(RESULT, e);
            });
        });
        return this;
    }

    public MessageBusSetupBuilder withADynamicExceptionListenerForSeveralClasses() {
        messageBusBuilder.withExceptionHandler(allExceptionIgnoringExceptionHandler());
        final List<Class<?>> errorClasses = Arrays.asList(TestMessageOfInterest.class, Object.class);
        setupActions.add((messageBus, testEnvironment1) -> {
            messageBus.onException(errorClasses, (m, e) -> {
                testEnvironment.setProperty(RESULT, e);
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
