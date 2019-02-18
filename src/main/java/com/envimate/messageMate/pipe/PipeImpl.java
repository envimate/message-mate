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

package com.envimate.messageMate.pipe;

import com.envimate.messageMate.internal.accepting.MessageAcceptingStrategy;
import com.envimate.messageMate.internal.delivering.DeliveryStrategy;
import com.envimate.messageMate.internal.eventloop.PipeEventLoopImpl;
import com.envimate.messageMate.internal.statistics.MessageStatistics;
import com.envimate.messageMate.internal.statistics.StatisticsCollector;
import com.envimate.messageMate.internal.transport.MessageTransportProcessFactory;
import com.envimate.messageMate.subscribing.ConsumerSubscriber;
import com.envimate.messageMate.subscribing.Subscriber;
import com.envimate.messageMate.subscribing.SubscriptionId;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public final class PipeImpl<T> implements Pipe<T> {

    private final MessageAcceptingStrategy<T> messageAcceptingStrategy;
    private final DeliveryStrategy<T> deliveryStrategy;
    private final List<Subscriber<T>> subscribers;
    private final PipeEventLoopImpl<T> eventLoop;
    private final StatisticsCollector statisticsCollector;
    private final MessageTransportProcessFactory<T> pipeTransportProcessFactory;
    //TODO: use only here and not in low level strategies
    private volatile boolean closedAlreadyCalled;

    //TODO: necessary? or only because of close? put all in event loop? or specific close object?
    public PipeImpl(MessageAcceptingStrategy<T> messageAcceptingStrategy,
                    DeliveryStrategy<T> deliveryStrategy,
                    List<Subscriber<T>> subscribers,
                    PipeEventLoopImpl<T> eventLoop,
                    StatisticsCollector statisticsCollector,
                    MessageTransportProcessFactory<T> pipeTransportProcessFactory) {
        this.messageAcceptingStrategy = messageAcceptingStrategy;
        this.deliveryStrategy = deliveryStrategy;
        this.subscribers = subscribers;
        this.eventLoop = eventLoop;
        this.statisticsCollector = statisticsCollector;
        this.pipeTransportProcessFactory = pipeTransportProcessFactory;
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
    public PipeStatusInformation<T> getStatusInformation() {
        return new PipeStatusInformation<T>() {
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
