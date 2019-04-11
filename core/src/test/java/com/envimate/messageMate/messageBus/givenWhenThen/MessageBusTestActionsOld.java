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

import com.envimate.messageMate.filtering.Filter;
import com.envimate.messageMate.identification.MessageId;
import com.envimate.messageMate.internal.pipe.statistics.PipeStatistics;
import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.messageBus.MessageBusStatusInformation;
import com.envimate.messageMate.messageBus.statistics.MessageBusStatistics;
import com.envimate.messageMate.processingContext.EventType;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeMessageBusSutActions;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import com.envimate.messageMate.subscribing.Subscriber;
import com.envimate.messageMate.subscribing.SubscriptionId;
import lombok.RequiredArgsConstructor;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.RESULT;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class MessageBusTestActionsOld implements PipeMessageBusSutActions {
    private final MessageBus messageBus;

    public static MessageBusTestActionsOld messageBusTestActions(final MessageBus messageBus) {
        return new MessageBusTestActionsOld(messageBus);
    }

    @Override
    public boolean isClosed(final TestEnvironment testEnvironment) {
        return messageBus.isClosed();
    }

    @Override
    public <R> void subscribe(final Class<R> messageClass, final Subscriber<R> subscriber) {
        throw new UnsupportedOperationException();
    }

    public void subscribe(final EventType eventType, final Subscriber<Object> subscriber) {
        messageBus.subscribe(eventType, subscriber);
    }

    @Override
    public void close(final boolean finishRemainingTasks) {
        messageBus.close(finishRemainingTasks);
    }

    @Override
    public boolean awaitTermination(final int timeout, final TimeUnit timeUnit) throws InterruptedException {
        return messageBus.awaitTermination(timeout, timeUnit);
    }

    @Override
    public List<?> getFilter(final TestEnvironment testEnvironment) {
        return messageBus.getFilter();
    }

    @Override
    public List<?> getFilter() {
        return messageBus.getFilter();
    }

    @Override
    public void unsubscribe(final SubscriptionId subscriptionId) {
        messageBus.unsubcribe(subscriptionId);
    }

    @Override
    public MessageId send(final TestMessage message) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PipeStatistics getMessageStatistics() {
        throw new UnsupportedOperationException();
    }

    public MessageBusStatistics getMessageStatistics_real() {
        final MessageBusStatusInformation statusInformation = messageBus.getStatusInformation();
        return statusInformation.getCurrentMessageStatistics();
    }

    public void queryTheNumberOfAcceptedMessages(final TestEnvironment testEnvironment) {
        queryMessageStatistics(testEnvironment, MessageBusStatistics::getAcceptedMessages);
    }

    public void queryTheNumberOfQueuedMessages(final TestEnvironment testEnvironment) {
        queryMessageStatistics(testEnvironment, MessageBusStatistics::getQueuedMessages);
    }

    public void queryTheNumberOfSuccessfulDeliveredMessages(final TestEnvironment testEnvironment) {
        queryMessageStatistics(testEnvironment, MessageBusStatistics::getSuccessfulMessages);
    }

    public void queryTheNumberOfFailedDeliveredMessages(final TestEnvironment testEnvironment) {
        queryMessageStatistics(testEnvironment, MessageBusStatistics::getFailedMessages);
    }

    public void queryTheNumberOfBlockedMessages(final TestEnvironment testEnvironment) {
        queryMessageStatistics(testEnvironment, MessageBusStatistics::getBlockedMessages);
    }

    public void queryTheNumberOfForgottenMessages(final TestEnvironment testEnvironment) {
        queryMessageStatistics(testEnvironment, MessageBusStatistics::getForgottenMessages);
    }

    public void queryTheTimestampOfTheMessageStatistics(final TestEnvironment testEnvironment) {
        final MessageBusStatistics messageBusStatistics = getMessageStatistics_real();
        final Date timestamp = messageBusStatistics.getTimestamp();
        testEnvironment.setProperty(RESULT, timestamp);
    }

    private void queryMessageStatistics(final TestEnvironment testEnvironment,
                                        final MessageBusStatisticsQuery query) {
        final MessageBusStatistics messageBusStatistics = getMessageStatistics_real();
        final BigInteger statistic = query.query(messageBusStatistics);
        final long longValueExact = statistic.longValueExact();
        testEnvironment.setProperty(RESULT, longValueExact);
    }

    @Override
    public Object removeAFilter() {
        final List<Filter<Object>> filters = messageBus.getFilter();
        final int indexToRemove = (int) (Math.random() * filters.size());
        final Filter<Object> filter = filters.get(indexToRemove);
        messageBus.remove(filter);
        return filter;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addFilter(final Filter<?> filter) {
        final Filter<Object> objectFilter = (Filter<Object>) filter;
        messageBus.add(objectFilter);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addFilter(final Filter<?> filter, final int position) {
        final Filter<Object> objectFilter = (Filter<Object>) filter;
        messageBus.add(objectFilter, position);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Subscriber<?>> getAllSubscribers() {
        final MessageBusStatusInformation statusInformation = messageBus.getStatusInformation();
        final List<Subscriber<?>> allSubscribers = statusInformation.getAllSubscribers();
        return allSubscribers;
    }

    private interface MessageBusStatisticsQuery {
        BigInteger query(MessageBusStatistics messageBusStatistics);
    }
}
