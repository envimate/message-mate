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
import com.envimate.messageMate.channel.ProcessingContext;
import com.envimate.messageMate.filtering.Filter;
import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.messageBus.MessageBusStatusInformation;
import com.envimate.messageMate.messageFunction.correlation.CorrelationId;
import com.envimate.messageMate.qcec.shared.TestAction;
import com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeMessageBusSutActions;
import com.envimate.messageMate.shared.subscriber.ExceptionThrowingTestSubscriber;
import com.envimate.messageMate.shared.subscriber.TestException;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import com.envimate.messageMate.shared.testMessages.TestMessageOfInterest;
import com.envimate.messageMate.subscribing.Subscriber;
import com.envimate.messageMate.subscribing.SubscriptionId;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.envimate.messageMate.messageBus.givenWhenThen.MessageBusTestActions.messageBusTestActions;
import static com.envimate.messageMate.messageBus.givenWhenThen.MessageBusTestProperties.CORRELATION_ID;
import static com.envimate.messageMate.messageBus.givenWhenThen.MessageBusTestProperties.CORRELATION_SUBSCRIPTION_ID;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.EXPECTED_RESULT;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.RESULT;
import static com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.AsynchronousSendingTestUtils.sendMessagesBeforeAndAfterShutdownAsynchronously;
import static com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeChannelMessageBusSharedTestProperties.SEND_CORRELATION_ID;
import static com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeChannelMessageBusSharedTestProperties.USED_SUBSCRIPTION_ID;
import static com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeMessageBusSetupActions.addASingleSubscriber;
import static com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeMessageBusTestActions.*;
import static com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.TestFilter.anErrorThrowingFilter;
import static com.envimate.messageMate.shared.subscriber.ExceptionThrowingTestSubscriber.exceptionThrowingTestSubscriber;


public final class MessageBusActionBuilder {
    private List<TestAction<MessageBus>> actions = new ArrayList<>();

    private MessageBusActionBuilder(final TestAction<MessageBus> action) {
        this.actions.add(action);
    }

    public static MessageBusActionBuilder aSingleMessageIsSend() {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = messageBusTestActions(messageBus);
            final CorrelationId correlationId = sendASingleMessage(sutActions, testEnvironment);
            testEnvironment.setProperty(SEND_CORRELATION_ID, correlationId);
            return null;
        });
    }

    public static MessageBusActionBuilder aMessageWithCorrelationIdIsSend() {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final CorrelationId correlationId;
            if (testEnvironment.has(CORRELATION_ID)) {
                correlationId = testEnvironment.getPropertyAsType(CORRELATION_ID, CorrelationId.class);
            } else {
                correlationId = CorrelationId.newUniqueId();
            }
            final CorrelationId sendCorrelationId = sendASingleMessage(messageBus, correlationId, testEnvironment);
            testEnvironment.setProperty(EXPECTED_RESULT, correlationId);
            testEnvironment.setProperty(SEND_CORRELATION_ID, sendCorrelationId);
            return null;
        });
    }

    public static MessageBusActionBuilder theMessageIsSend(final TestMessage message) {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = messageBusTestActions(messageBus);
            sendTheMessage(sutActions, testEnvironment, message);
            return null;
        });
    }

    public static MessageBusActionBuilder severalMessagesAreSend(final int numberOfMessages) {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = messageBusTestActions(messageBus);
            sendSeveralMessages(sutActions, testEnvironment, numberOfMessages);
            return null;
        });
    }

    public static MessageBusActionBuilder severalMessagesAreSendAsynchronously(final int numberOfSender, final int numberOfMessagesPerSender) {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = messageBusTestActions(messageBus);
            sendSeveralMessagesInTheirOwnThread(sutActions, testEnvironment, numberOfSender, numberOfMessagesPerSender, true);
            return null;
        });
    }

    public static MessageBusActionBuilder severalMessagesAreSendAsynchronouslyButWillBeBlocked(final int numberOfSender, final int numberOfMessagesPerSender) {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = messageBusTestActions(messageBus);
            sendSeveralMessagesInTheirOwnThread(sutActions, testEnvironment, numberOfSender, numberOfMessagesPerSender, false);
            return null;
        });
    }

    public static MessageBusActionBuilder sendSeveralMessagesBeforeTheBusIsShutdown(final int numberOfSender, final boolean finishRemainingTasks) {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = messageBusTestActions(messageBus);
            sendMessagesBeforeAndAfterShutdownAsynchronously(sutActions, testEnvironment, numberOfSender, 0, finishRemainingTasks);
            return null;
        });
    }

    public static MessageBusActionBuilder aSubscriberIsAdded(final Class<?> clazz) {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = messageBusTestActions(messageBus);
            addASingleSubscriber(sutActions, testEnvironment, clazz);
            return null;
        });
    }

    public static MessageBusActionBuilder oneSubscriberUnsubscribesSeveralTimes(final int numberOfUnsubscriptions) {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = messageBusTestActions(messageBus);
            unsubscribeASubscriberXTimes(sutActions, testEnvironment, numberOfUnsubscriptions);
            return null;
        });
    }

    public static MessageBusActionBuilder oneSubscriberUnsubscribes() {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = messageBusTestActions(messageBus);
            unsubscribeASubscriberXTimes(sutActions, testEnvironment, 1);
            return null;
        });
    }

    public static MessageBusActionBuilder theSubscriberForTheCorrelationIdUnsubscribes() {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final SubscriptionId subscriptionId = testEnvironment.getPropertyAsType(CORRELATION_SUBSCRIPTION_ID, SubscriptionId.class);
            messageBus.unsubcribe(subscriptionId);
            return null;
        });
    }

    public static MessageBusActionBuilder halfValidAndInvalidMessagesAreSendAsynchronously(final int numberOfSender, final int numberOfMessagesPerSender) {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = messageBusTestActions(messageBus);
            sendBothValidAndInvalidMessagesAsynchronously(sutActions, testEnvironment, numberOfSender, numberOfMessagesPerSender);
            return null;
        });
    }

    public static MessageBusActionBuilder severalInvalidMessagesAreSendAsynchronously(final int numberOfSender, final int numberOfMessagesPerSender) {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = messageBusTestActions(messageBus);
            sendSeveralInvalidMessagesAsynchronously(sutActions, testEnvironment, numberOfSender, numberOfMessagesPerSender);
            return null;
        });
    }

    public static MessageBusActionBuilder theNumberOfAcceptedMessagesIsQueried() {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            messageBusTestActions(messageBus)
                    .queryTheNumberOfAcceptedMessages(testEnvironment);
            return null;
        });
    }

    public static MessageBusActionBuilder theNumberOfAcceptedMessagesIsQueriedAsynchronously() {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            messageBusTestActions(messageBus)
                    .queryTheNumberOfAcceptedMessagesAsynchronously(testEnvironment);
            return null;
        });
    }

    public static MessageBusActionBuilder theNumberOfQueuedMessagesIsQueried() {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            messageBusTestActions(messageBus)
                    .queryTheNumberOfQueuedMessages(testEnvironment);
            return null;
        });
    }

    public static MessageBusActionBuilder theNumberOfSuccessfulMessagesIsQueried() {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            messageBusTestActions(messageBus)
                    .queryTheNumberOfSuccessfulDeliveredMessages(testEnvironment);
            return null;
        });
    }

    public static MessageBusActionBuilder theNumberOfFailedMessagesIsQueried() {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            messageBusTestActions(messageBus)
                    .queryTheNumberOfFailedDeliveredMessages(testEnvironment);
            return null;
        });
    }

    public static MessageBusActionBuilder theNumberOfBlockedMessagesIsQueried() {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            messageBusTestActions(messageBus)
                    .queryTheNumberOfBlockedMessages(testEnvironment);
            return null;
        });
    }

    public static MessageBusActionBuilder theNumberOfForgottenMessagesIsQueried() {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            messageBusTestActions(messageBus)
                    .queryTheNumberOfForgottenMessages(testEnvironment);
            return null;
        });
    }


    public static MessageBusActionBuilder theTimestampOfTheStatisticsIsQueried() {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            messageBusTestActions(messageBus)
                    .queryTheTimestampOfTheMessageStatistics(testEnvironment);
            return null;
        });
    }

    public static MessageBusActionBuilder aShortWaitIsDone(final long timeout, final TimeUnit timeUnit) {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            performAShortWait(timeout, timeUnit);
            return null;
        });
    }

    public static MessageBusActionBuilder theSubscriberAreQueriedPerType() {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final MessageBusStatusInformation statusInformation = messageBus.getStatusInformation();
            final Map<Class<?>, List<Subscriber<?>>> subscribersPerType = statusInformation.getSubscribersPerType();
            testEnvironment.setProperty(RESULT, subscribersPerType);
            return null;
        });
    }

    public static MessageBusActionBuilder allSubscribersAreQueriedAsList() {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final List<Subscriber<?>> allSubscribers = messageBus.getStatusInformation().getAllSubscribers();
            testEnvironment.setProperty(RESULT, allSubscribers);
            return null;
        });
    }

    public static MessageBusActionBuilder theChannelForTheClassIsQueried(final Class<?> clazz) {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final Channel<?> channel = messageBus.getStatusInformation()
                    .getChannelFor(clazz);
            testEnvironment.setProperty(RESULT, channel);
            return null;
        });
    }

    public static MessageBusActionBuilder severalMessagesAreSendAsynchronouslyBeforeTheMessageBusIsShutdown(final int numberOfSenders, final int numberOfMessages) {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = messageBusTestActions(messageBus);
            sendSeveralMessagesAsynchronouslyBeforeTheObjectIsShutdown(sutActions, testEnvironment, numberOfSenders, numberOfMessages);
            return null;
        });
    }

    public static MessageBusActionBuilder theMessageBusIsShutdownAsynchronouslyXTimes(final int numberOfThreads) {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = messageBusTestActions(messageBus);
            shutdownTheObjectAsynchronouslyXTimes(sutActions, numberOfThreads);
            return null;
        });
    }

    public static MessageBusActionBuilder theMessageBusIsShutdown() {
        return theMessageBusIsShutdown(true);
    }

    public static MessageBusActionBuilder theMessageBusIsShutdown(final boolean finishRemainingTasks) {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = messageBusTestActions(messageBus);
            shutdownTheSut(sutActions, finishRemainingTasks);
            return null;
        });
    }

    public static MessageBusActionBuilder theMessageBusShutdownIsExpectedForTimeoutInSeconds(final int timeoutInSeconds) {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = messageBusTestActions(messageBus);
            awaitTheShutdownTimeoutInSeconds(sutActions, testEnvironment, timeoutInSeconds);
            return null;
        });
    }

    public static MessageBusActionBuilder theListOfFiltersIsQueried() {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = messageBusTestActions(messageBus);
            queryTheListOfFilters(sutActions, testEnvironment);
            return null;
        });
    }

    public static MessageBusActionBuilder aFilterIsRemoved() {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = messageBusTestActions(messageBus);
            removeAFilter(sutActions, testEnvironment);
            return null;
        });
    }

    public static MessageBusActionBuilder anExceptionThrowingFilterIsAddedInChannelOf(final Class<TestMessageOfInterest> clazz) {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final MessageBusStatusInformation statusInformation = messageBus.getStatusInformation();
            final Channel<TestMessageOfInterest> channel = statusInformation.getChannelFor(clazz);
            final RuntimeException exception = new TestException();
            final Filter<ProcessingContext<TestMessageOfInterest>> filter = anErrorThrowingFilter(exception);
            channel.addProcessFilter(filter);
            return null;
        });
    }

    public static MessageBusActionBuilder anExceptionThrowingSubscriberIsAdded() {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final ExceptionThrowingTestSubscriber<TestMessageOfInterest> subscriber = exceptionThrowingTestSubscriber();
            messageBus.subscribe(TestMessageOfInterest.class, subscriber);
            return null;
        });
    }

    public static MessageBusActionBuilder theDynamicExceptionHandlerToBeRemoved() {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final SubscriptionId subscriptionId = testEnvironment.getPropertyAsType(USED_SUBSCRIPTION_ID, SubscriptionId.class);
            messageBus.unregisterExceptionListener(subscriptionId);
            return null;
        });
    }

    public MessageBusActionBuilder andThen(final MessageBusActionBuilder followUpBuilder) {
        actions.addAll(followUpBuilder.actions);
        return this;
    }

    public List<TestAction<MessageBus>> build() {
        return actions;
    }
}
