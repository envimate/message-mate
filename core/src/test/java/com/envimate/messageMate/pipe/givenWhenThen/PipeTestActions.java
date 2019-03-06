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

package com.envimate.messageMate.pipe.givenWhenThen;

import com.envimate.messageMate.filtering.Filter;
import com.envimate.messageMate.pipe.Pipe;
import com.envimate.messageMate.pipe.PipeStatusInformation;
import com.envimate.messageMate.pipe.statistics.PipeStatistics;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeMessageBusSutActions;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import com.envimate.messageMate.subscribing.Subscriber;
import com.envimate.messageMate.subscribing.SubscriptionId;
import lombok.RequiredArgsConstructor;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.RESULT;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class PipeTestActions implements PipeMessageBusSutActions {
    private final Pipe<TestMessage> pipe;

    public static PipeTestActions pipeTestActions(final Pipe<TestMessage> pipe) {
        return new PipeTestActions(pipe);
    }

    @Override
    public boolean isShutdown(final TestEnvironment testEnvironment) {
        return pipe.isShutdown();
    }

    @Override
    public List<?> getFilter(final TestEnvironment testEnvironment) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <R> void subscribe(final Class<R> messageClass, final Subscriber<R> subscriber) {
        @SuppressWarnings("unchecked")
        final Subscriber<TestMessage> messageSubscriber = (Subscriber<TestMessage>) subscriber;
        pipe.subscribe(messageSubscriber);
    }

    @Override
    public void close(final boolean finishRemainingTasks) {
        pipe.close(finishRemainingTasks);
    }

    @Override
    public boolean awaitTermination(final int timeout, final TimeUnit timeUnit) throws InterruptedException {
        return pipe.awaitTermination(timeout, timeUnit);
    }

    @Override
    public List<?> getFilter() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void unsubscribe(final SubscriptionId subscriptionId) {
        pipe.unsubscribe(subscriptionId);
    }

    @Override
    public void send(final TestMessage message) {
        pipe.send(message);
    }

    @Override
    public PipeStatistics getMessageStatistics() {
        final PipeStatusInformation<TestMessage> statusInformation = pipe.getStatusInformation();
        return statusInformation.getCurrentMessageStatistics();
    }

    public void queryTheNumberOfAcceptedMessages(final TestEnvironment testEnvironment) {
        queryMessageStatistics(testEnvironment, PipeStatistics::getAcceptedMessages);
    }

    public void queryTheNumberOfAcceptedMessagesAsynchronously(final TestEnvironment testEnvironment) {
        final Semaphore semaphore = new Semaphore(0);
        new Thread(() -> {
            queryMessageStatistics(testEnvironment, PipeStatistics::getAcceptedMessages);
            semaphore.release();
        }).start();
        try {
            semaphore.acquire();
        } catch (final InterruptedException e) {
            //not necessary to do anything here
        }
    }

    public void queryTheNumberOfQueuedMessages(final TestEnvironment testEnvironment) {
        queryMessageStatistics(testEnvironment, PipeStatistics::getQueuedMessages);
    }

    public void queryTheNumberOfSuccessfulDeliveredMessages(final TestEnvironment testEnvironment) {
        queryMessageStatistics(testEnvironment, PipeStatistics::getSuccessfulMessages);
    }

    public void queryTheNumberOfFailedDeliveredMessages(final TestEnvironment testEnvironment) {
        queryMessageStatistics(testEnvironment, PipeStatistics::getFailedMessages);
    }

    public void queryTheTimestampOfTheMessageStatistics(final TestEnvironment testEnvironment) {
        final PipeStatistics pipeStatistics = getMessageStatistics();
        final Date timestamp = pipeStatistics.getTimestamp();
        testEnvironment.setProperty(RESULT, timestamp);
    }

    private void queryMessageStatistics(final TestEnvironment testEnvironment,
                                        final PipeStatisticsQuery query) {
        final PipeStatistics pipeStatistics = getMessageStatistics();
        final BigInteger statistic = query.query(pipeStatistics);
        final long longValueExact = statistic.longValueExact();
        testEnvironment.setProperty(RESULT, longValueExact);
    }

    @Override
    public Object removeAFilter() {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addFilter(final Filter<?> filter) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addFilter(final Filter<?> filter, final int position) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Subscriber<?>> getAllSubscribers() {
        final PipeStatusInformation<TestMessage> statusInformation = pipe.getStatusInformation();
        final List<?> allSubscribers = statusInformation.getAllSubscribers();
        return (List<Subscriber<?>>) allSubscribers;
    }

    private interface PipeStatisticsQuery {
        BigInteger query(PipeStatistics pipeStatistics);
    }
}
