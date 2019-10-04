/*
 * Copyright (c) 2019 envimate GmbH - https://envimate.com/.
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
import com.envimate.messageMate.channel.givenWhenThen.FilterPosition;
import com.envimate.messageMate.filtering.Filter;
import com.envimate.messageMate.identification.CorrelationId;
import com.envimate.messageMate.identification.MessageId;
import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.messageBus.MessageBusStatusInformation;
import com.envimate.messageMate.messageBus.exception.MessageBusExceptionListener;
import com.envimate.messageMate.messageBus.statistics.MessageBusStatistics;
import com.envimate.messageMate.processingContext.EventType;
import com.envimate.messageMate.processingContext.ProcessingContext;
import com.envimate.messageMate.shared.environment.TestEnvironment;
import com.envimate.messageMate.shared.pipeChannelMessageBus.testActions.*;
import com.envimate.messageMate.shared.subscriber.SimpleTestSubscriber;
import com.envimate.messageMate.shared.exceptions.TestException;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import com.envimate.messageMate.shared.testMessages.TestMessageOfInterest;
import com.envimate.messageMate.shared.utils.FilterTestUtils;
import com.envimate.messageMate.subscribing.Subscriber;
import com.envimate.messageMate.subscribing.SubscriptionId;
import lombok.RequiredArgsConstructor;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.envimate.messageMate.identification.CorrelationId.newUniqueCorrelationId;
import static com.envimate.messageMate.messageBus.givenWhenThen.MessageBusTestProperties.CORRELATION_SUBSCRIPTION_ID;
import static com.envimate.messageMate.messageBus.givenWhenThen.MessageBusTestProperties.MESSAGE_RECEIVED_BY_ERROR_LISTENER;
import static com.envimate.messageMate.shared.environment.TestEnvironmentProperty.EXPECTED_RECEIVERS;
import static com.envimate.messageMate.shared.environment.TestEnvironmentProperty.RESULT;
import static com.envimate.messageMate.shared.eventType.TestEventType.testEventType;
import static com.envimate.messageMate.shared.pipeChannelMessageBus.testActions.TestFilter.*;
import static com.envimate.messageMate.shared.properties.SharedTestProperties.*;
import static com.envimate.messageMate.shared.subscriber.SimpleTestSubscriber.testSubscriber;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
final class MessageBusTestActions implements SendingAndReceivingActions, RawSubscribeActions, ProcessingContextSendingActions,
        CorrelationIdSendingActions, FilterTestActions, SimplifiedFilterTestActions {
    private final MessageBus messageBus;

    static MessageBusTestActions messageBusTestActions(final MessageBus messageBus) {
        return new MessageBusTestActions(messageBus);
    }

    static void addSubscriberForACorrelationId(final MessageBus messageBus,
                                               final TestEnvironment testEnvironment) {
        final CorrelationId correlationId = newUniqueCorrelationId();
        final SimpleTestSubscriber<ProcessingContext<Object>> subscriber = testSubscriber();
        final SubscriptionId subscriptionId = messageBus.subscribe(correlationId, subscriber);
        testEnvironment.setProperty(EXPECTED_CORRELATION_ID, correlationId);
        testEnvironment.setProperty(CORRELATION_SUBSCRIPTION_ID, subscriptionId);
        testEnvironment.addToListProperty(EXPECTED_RECEIVERS, subscriber);
    }

    static void addDynamicErrorListenerForEventType(final MessageBus messageBus, final TestEnvironment testEnvironment) {
        final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
        final SubscriptionId subscriptionId = messageBus.onException(eventType, (m, e) -> {
            testEnvironment.setPropertyIfNotSet(RESULT, e);
            testEnvironment.setPropertyIfNotSet(MESSAGE_RECEIVED_BY_ERROR_LISTENER, m);
        });
        testEnvironment.setProperty(USED_SUBSCRIPTION_ID, subscriptionId);
    }

    static void addTwoDynamicErrorListenerForEventType_whereTheFirstWillBeRemoved(final MessageBus messageBus,
                                                                                  final TestEnvironment testEnvironment) {
        final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
        final SubscriptionId subscriptionId = messageBus.onException(eventType, (m, e) -> {
            throw new RuntimeException("Should not be called");
        });
        testEnvironment.setProperty(USED_SUBSCRIPTION_ID, subscriptionId);
        messageBus.onException(eventType, (m, e) -> {
            testEnvironment.setPropertyIfNotSet(RESULT, e);
            testEnvironment.setPropertyIfNotSet(MESSAGE_RECEIVED_BY_ERROR_LISTENER, m);
        });
    }

    static List<MessageBusExceptionListener> queryListOfDynamicExceptionListener(final MessageBus messageBus) {
        return messageBus.getStatusInformation().getAllExceptionListener();
    }

    public static void removeDynamicExceptionHandler(final MessageBus messageBus,
                                                     final TestEnvironment testEnvironment) {
        final SubscriptionId subscriptionId = testEnvironment.getPropertyAsType(USED_SUBSCRIPTION_ID, SubscriptionId.class);
        messageBus.unregisterExceptionListener(subscriptionId);
    }

    static void addTwoFilterOnSpecificPositions(final MessageBus messageBus,
                                                final TestEnvironment testEnvironment) {
        final String firstAppend = "1nd";
        final String secondAppend = "2nd";
        testEnvironment.setProperty(EXPECTED_CHANGED_CONTENT, TestMessageOfInterest.CONTENT + firstAppend + secondAppend);
        final Filter<TestMessage> filter1 = aContentAppendingFilter(secondAppend);
        final MessageBusTestActions testActions = MessageBusTestActions.messageBusTestActions(messageBus);
        testActions.addNotRawFilter(filter1, 0);
        testEnvironment.addToListProperty(EXPECTED_FILTER, filter1);
        final Filter<TestMessage> filter2 = aContentAppendingFilter(firstAppend);
        testActions.addNotRawFilter(filter2, 0);
        testEnvironment.addToListProperty(EXPECTED_FILTER, filter2);
    }

    public static void addAnExceptionThrowingFilterInChannelOf(final MessageBus messageBus,
                                                               final TestEnvironment testEnvironment,
                                                               final EventType eventType) {
        final MessageBusStatusInformation statusInformation = messageBus.getStatusInformation();
        testEnvironment.setPropertyIfNotSet(EVENT_TYPE, eventType);
        final Channel<Object> channel = statusInformation.getChannelFor(eventType);
        final RuntimeException exception = new TestException();
        final Filter<ProcessingContext<Object>> filter = anErrorThrowingFilter(exception);
        channel.addProcessFilter(filter);
    }

    static void addARawFilterThatChangesTheContentOfEveryMessage(final MessageBus messageBus) {
        final Filter<ProcessingContext<Object>> filter = aRawFilterThatChangesTheCompleteProcessingContext();
        messageBus.addRaw(filter);
    }

    static void removeAFilter(final MessageBus messageBus, final TestEnvironment testEnvironment) {
        final MessageBusTestActions testActions = MessageBusTestActions.messageBusTestActions(messageBus);
        FilterTestUtils.removeAFilter(testActions, testEnvironment);
    }

    public static Channel<Object> queryChannelForEventType(final MessageBus messageBus, final EventType eventType) {
        final MessageBusStatusInformation statusInformation = messageBus.getStatusInformation();
        final Channel<Object> channel = statusInformation.getChannelFor(eventType);
        return channel;
    }

    @Override
    public void close(final boolean finishRemainingTasks) {
        messageBus.close(finishRemainingTasks);
    }

    @Override
    public boolean await(final int timeout, final TimeUnit timeUnit) throws InterruptedException {
        return messageBus.awaitTermination(timeout, timeUnit);
    }

    @Override
    public boolean isClosed() {
        return messageBus.isClosed();
    }

    @Override
    public MessageId send(final EventType eventType, final TestMessage message) {
        return messageBus.send(eventType, message);
    }

    @SuppressWarnings("unchecked")
    @Override
    public MessageId send(final ProcessingContext<TestMessage> processingContext) {
        final ProcessingContext<?> testMessageProcessingContext = processingContext;
        final ProcessingContext<Object> objectProcessingContext = (ProcessingContext<Object>) testMessageProcessingContext;
        return messageBus.send(objectProcessingContext);
    }

    @Override
    public MessageId send(final EventType eventType, final TestMessage testMessage, final CorrelationId correlationId) {
        return messageBus.send(eventType, testMessage, correlationId);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void subscribe(final EventType eventType, final Subscriber<TestMessage> subscriber) {
        final Subscriber degenerifiedSubscriber = subscriber;
        messageBus.subscribe(eventType, degenerifiedSubscriber);
    }

    @Override
    public void unsubscribe(final SubscriptionId subscriptionId) {
        messageBus.unsubcribe(subscriptionId);
    }

    @Override
    public List<Subscriber<?>> getAllSubscribers() {
        return messageBus.getStatusInformation().getAllSubscribers();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public SubscriptionId subscribeRaw(final EventType eventType, final Subscriber<ProcessingContext<TestMessage>> subscriber) {
        final Subscriber degenerifiedSubscriber = subscriber;
        return messageBus.subscribeRaw(eventType, degenerifiedSubscriber);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void addFilter(final Filter<ProcessingContext<TestMessage>> filter, final FilterPosition filterPosition) {
        final Filter degenerifiedFilter = filter;
        messageBus.add(degenerifiedFilter);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void addFilter(final Filter<ProcessingContext<TestMessage>> filter,
                          final FilterPosition filterPosition,
                          final int position) {
        final Filter degenerifiedFilter = filter;
        messageBus.add(degenerifiedFilter);
    }

    @Override
    public List<?> getFilter(final FilterPosition filterPosition) {
        return messageBus.getFilter();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void removeFilter(final Filter<?> filter, final FilterPosition filterPosition) {
        final Filter<Object> castedFilter = (Filter<Object>) filter;
        messageBus.remove(castedFilter);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void addNotRawFilter(final Filter<TestMessage> filter) {
        final Filter degenerifiedFilter = filter;
        messageBus.add(degenerifiedFilter);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void addNotRawFilter(final Filter<TestMessage> filter, final int position) {
        final Filter degenerifiedFilter = filter;
        messageBus.add(degenerifiedFilter, position);
    }

    @Override
    public long numberOfQueuedMessages() {
        return queryTheNumberOfQueuedMessages();
    }

    long queryTheNumberOfAcceptedMessages() {
        return queryMessageStatistics(MessageBusStatistics::getAcceptedMessages);
    }

    long queryTheNumberOfQueuedMessages() {
        return queryMessageStatistics(MessageBusStatistics::getQueuedMessages);
    }

    long queryTheNumberOfSuccessfulDeliveredMessages() {
        return queryMessageStatistics(MessageBusStatistics::getSuccessfulMessages);
    }

    long queryTheNumberOfFailedDeliveredMessages() {
        return queryMessageStatistics(MessageBusStatistics::getFailedMessages);
    }

    long queryTheNumberOfBlockedMessages() {
        return queryMessageStatistics(MessageBusStatistics::getBlockedMessages);
    }

    long queryTheNumberOfForgottenMessages() {
        return queryMessageStatistics(MessageBusStatistics::getForgottenMessages);
    }

    private long queryMessageStatistics(final Function<MessageBusStatistics, BigInteger> query) {
        final MessageBusStatistics messageBusStatistics = getMessageBusStatistics();
        final BigInteger statistic = query.apply(messageBusStatistics);
        final long longValueExact = statistic.longValueExact();
        return longValueExact;
    }

    Date queryTheTimestampOfTheMessageStatistics() {
        final MessageBusStatistics messageBusStatistics = getMessageBusStatistics();
        final Date timestamp = messageBusStatistics.getTimestamp();
        return timestamp;
    }

    private MessageBusStatistics getMessageBusStatistics() {
        final MessageBusStatusInformation statusInformation = messageBus.getStatusInformation();
        return statusInformation.getCurrentMessageStatistics();
    }

}
