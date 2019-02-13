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

package com.envimate.messageMate.channel;

import com.envimate.messageMate.filtering.Filter;
import com.envimate.messageMate.internal.accepting.MessageAcceptingStrategy;
import com.envimate.messageMate.internal.accepting.MessageAcceptingStrategyFactory;
import com.envimate.messageMate.internal.delivering.DeliveryStrategy;
import com.envimate.messageMate.internal.delivering.DeliveryStrategyFactory;
import com.envimate.messageMate.internal.eventloop.ChannelEventLoopImpl;
import com.envimate.messageMate.internal.statistics.MessageStatistics;
import com.envimate.messageMate.internal.statistics.StatisticsCollector;
import com.envimate.messageMate.internal.transport.ChannelTransportProcessFactory;
import com.envimate.messageMate.subscribing.ConsumerSubscriber;
import com.envimate.messageMate.subscribing.Subscriber;
import com.envimate.messageMate.subscribing.SubscriptionId;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.envimate.messageMate.internal.transport.ChannelTransportProcessFactory.channelTransportProcessFactory;

public final class ChannelImpl<T> implements Channel<T> {

    private final MessageAcceptingStrategy<T> messageAcceptingStrategy;
    private final DeliveryStrategy<T> deliveryStrategy;
    private final List<Subscriber<T>> subscribers;
    private final List<Filter<T>> filters;
    private final ChannelEventLoopImpl<T> eventLoop;
    private final StatisticsCollector statisticsCollector;
    private volatile boolean closedAlreadyCalled;

    ChannelImpl(final MessageAcceptingStrategyFactory<T> messageAcceptingStrategyFactory,
                final DeliveryStrategyFactory<T> deliveryStrategyFactory,
                final StatisticsCollector statisticsCollector) {
        this.statisticsCollector = statisticsCollector;
        this.eventLoop = new ChannelEventLoopImpl<>();
        this.messageAcceptingStrategy = messageAcceptingStrategyFactory.createNew(eventLoop);
        this.deliveryStrategy = deliveryStrategyFactory.createNew(eventLoop);
        this.subscribers = new CopyOnWriteArrayList<>();
        this.filters = new CopyOnWriteArrayList<>();
        final ChannelTransportProcessFactory<T> channelTPF = channelTransportProcessFactory(filters, eventLoop, subscribers);
        eventLoop.setRequiredObjects(messageAcceptingStrategy, channelTPF, deliveryStrategy, statisticsCollector);
    }

    @Override
    public void send(final T message) {
        messageAcceptingStrategy.accept(message);
    }

    @Override
    public SubscriptionId subscribe(final Subscriber<T> subscriber) {
        subscribers.add(subscriber);
        return subscriber.getSubscriptionId();
    }

    @Override
    public SubscriptionId subscribe(final Consumer<T> consumer) {
        final ConsumerSubscriber<T> subscriber = ConsumerSubscriber.consumerSubscriber(consumer);
        return subscribe(subscriber);
    }

    @Override
    public void unsubscribe(final SubscriptionId subscriptionId) {
        subscribers.removeIf(subscriber -> subscriber.getSubscriptionId().equals(subscriptionId));
    }

    @Override
    public void add(final Filter<T> filter) {
        filters.add(filter);
    }

    @Override
    public void add(final Filter<T> filter, final int position) {
        filters.add(position, filter);
    }

    @Override
    public List<Filter<T>> getFilter() {
        return filters;
    }

    @Override
    public void remove(final Filter<T> filter) {
        filters.remove(filter);
    }

    @Override
    public void close(final boolean finishRemainingTasks) { //TODO: close transport + await
        if (!closedAlreadyCalled) {
            closedAlreadyCalled = true;
            messageAcceptingStrategy.close(finishRemainingTasks);

            deliveryStrategy.close(finishRemainingTasks);
        }
    }

    @Override
    public void close() {
        close(true);
    }

    @Override
    public ChannelStatusInformation<T> getStatusInformation() {
        return new ChannelStatusInformation<T>() {
            @Override
            public MessageStatistics getCurrentMessageStatistics() {
                return statisticsCollector.getCurrentStatistics();
            }

            @Override
            public List<Subscriber<T>> getAllSubscribers() {
                return subscribers;
            }
        };
    }

    @Override
    public boolean isShutdown() {
        return messageAcceptingStrategy.isShutdown() && deliveryStrategy.isShutdown();
    }

    @Override
    public boolean awaitTermination(final int timeout, final TimeUnit timeUnit) throws InterruptedException {
        if (timeout <= 0) {
            return false;
        }
        final long currentTimeMillis = System.currentTimeMillis();
        final long addedMillis = timeUnit.toMillis(timeout);
        final Date deadline = new Date(currentTimeMillis + addedMillis);
        boolean result = messageAcceptingStrategy.awaitTermination(deadline);
        result = result && deliveryStrategy.awaitTermination(deadline);
        return result;
    }
}
