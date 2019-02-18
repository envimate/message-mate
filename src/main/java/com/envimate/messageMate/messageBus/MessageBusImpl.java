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

package com.envimate.messageMate.messageBus;

import com.envimate.messageMate.filtering.Filter;
import com.envimate.messageMate.internal.accepting.MessageAcceptingStrategy;
import com.envimate.messageMate.internal.accepting.MessageAcceptingStrategyFactory;
import com.envimate.messageMate.internal.brokering.BrokerStrategy;
import com.envimate.messageMate.internal.delivering.DeliveryStrategy;
import com.envimate.messageMate.internal.delivering.DeliveryStrategyFactory;
import com.envimate.messageMate.internal.eventloop.MessageBusEventLoopImpl;
import com.envimate.messageMate.internal.statistics.StatisticsCollector;
import com.envimate.messageMate.internal.transport.MessageTransportProcessFactory;
import com.envimate.messageMate.subscribing.Subscriber;
import com.envimate.messageMate.subscribing.SubscriptionId;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.envimate.messageMate.internal.transport.MessageTransportConfiguration.synchronTransportConfiguration;
import static com.envimate.messageMate.internal.transport.MessageTransportProcessFactoryFactory.messageTransportProcessFactory;
import static com.envimate.messageMate.messageBus.internal.MessageBusStatusInformationAdapter.statusInformationAdapter;
import static com.envimate.messageMate.subscribing.ConsumerSubscriber.consumerSubscriber;

final class MessageBusImpl implements MessageBus {
    private final MessageAcceptingStrategy<Object> messageAcceptingStrategy;
    private final BrokerStrategy brokerStrategy;
    private final StatisticsCollector statisticsCollector;
    private final List<Filter<Object>> filters;
    private final DeliveryStrategy<Object> deliveryStrategy;
    private final MessageTransportProcessFactory<Object> transportProcessFactory;
    private final MessageBusEventLoopImpl eventLoop;
    private volatile boolean closedAlreadyCalled;

    MessageBusImpl(final MessageAcceptingStrategyFactory<Object> messageAcceptingStrategyFactory,
                   final BrokerStrategy brokerStrategy,
                   final DeliveryStrategyFactory<Object> deliveryStrategyFactory,
                   final StatisticsCollector statisticsCollector) {
        this.filters = new CopyOnWriteArrayList<>();
        this.brokerStrategy = brokerStrategy;
        this.eventLoop = new MessageBusEventLoopImpl();
        this.transportProcessFactory = messageTransportProcessFactory(synchronTransportConfiguration(), filters, eventLoop, brokerStrategy::calculateReceivingSubscriber);//TODO: bad
        this.messageAcceptingStrategy = messageAcceptingStrategyFactory.createNew(eventLoop);
        this.statisticsCollector = statisticsCollector;
        this.deliveryStrategy = deliveryStrategyFactory.createNew(eventLoop);
        eventLoop.setRequiredObjects(messageAcceptingStrategy, transportProcessFactory, deliveryStrategy, statisticsCollector);
    }

    @Override
    public void send(final Object message) {
        messageAcceptingStrategy.accept(message);
    }

    @Override
    public <T> SubscriptionId subscribe(final Class<T> messageClass, final Consumer<T> consumer) {
        final Subscriber<T> subscriber = consumerSubscriber(consumer);
        return subscribe(messageClass, subscriber);
    }

    @Override
    public <T> SubscriptionId subscribe(final Class<T> messageClass, final Subscriber<T> subscriber) {
        @SuppressWarnings("unchecked")
        final Subscriber<Object> objectSubscriber = (Subscriber<Object>) subscriber;
        return brokerStrategy.add(messageClass, objectSubscriber);
    }

    @Override
    public void unsubcribe(final SubscriptionId subscriptionId) {
        brokerStrategy.remove(subscriptionId);
    }

    @Override
    public void add(final Filter<Object> filter) {
        filters.add(filter);
    }

    @Override
    public void add(final Filter<Object> filter, final int position) {
        filters.add(position, filter);
    }

    @Override
    public List<Filter<Object>> getFilter() {
        return filters;
    }

    @Override
    public void remove(final Filter<Object> filter) {
        filters.remove(filter);
    }

    @Override
    public void close(final boolean finishRemainingTasks) {
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
    public boolean awaitTermination(final long timeout, final TimeUnit unit) throws InterruptedException {
        if (timeout <= 0) {
            return false;
        }
        final long currentTimeMillis = System.currentTimeMillis();
        final long addedMillis = unit.toMillis(timeout);
        final Date deadline = new Date(currentTimeMillis + addedMillis);
        boolean result = messageAcceptingStrategy.awaitTermination(deadline);
        result = result && deliveryStrategy.awaitTermination(deadline);
        return result;
    }

    @Override
    public boolean isShutdown() {
        return messageAcceptingStrategy.isShutdown() && deliveryStrategy.isShutdown();
    }

    @Override
    public MessageBusStatusInformation getStatusInformation() {
        return statusInformationAdapter(statisticsCollector, brokerStrategy);
    }

}
