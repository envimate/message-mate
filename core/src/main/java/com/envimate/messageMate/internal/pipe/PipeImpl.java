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

package com.envimate.messageMate.internal.pipe;

import com.envimate.messageMate.exceptions.AlreadyClosedException;
import com.envimate.messageMate.internal.pipe.statistics.PipeStatistics;
import com.envimate.messageMate.internal.pipe.statistics.PipeStatisticsCollector;
import com.envimate.messageMate.internal.pipe.transport.TransportMechanism;
import com.envimate.messageMate.subscribing.ConsumerSubscriber;
import com.envimate.messageMate.subscribing.Subscriber;
import com.envimate.messageMate.subscribing.SubscriptionId;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.envimate.messageMate.subscribing.ConsumerSubscriber.consumerSubscriber;

public final class PipeImpl<T> implements Pipe<T> {
    private final TransportMechanism<T> transportMechanism;
    private final PipeStatisticsCollector statisticsCollector;
    private final List<Subscriber<T>> subscribers;
    private volatile boolean closedAlreadyCalled;

    public PipeImpl(final TransportMechanism<T> transportMechanism, final PipeStatisticsCollector statisticsCollector,
                    final List<Subscriber<T>> subscribers) {
        this.transportMechanism = transportMechanism;
        this.statisticsCollector = statisticsCollector;
        this.subscribers = subscribers;
    }

    @Override
    public void send(final T message) {
        if (!closedAlreadyCalled) {
            transportMechanism.transport(message);
        } else {
            throw new AlreadyClosedException();
        }
    }

    @Override
    public SubscriptionId subscribe(final Subscriber<T> subscriber) {
        if (!closedAlreadyCalled) {
            subscribers.add(subscriber);
            return subscriber.getSubscriptionId();
        } else {
            throw new AlreadyClosedException();
        }
    }

    @Override
    public SubscriptionId subscribe(final Consumer<T> consumer) {
        final ConsumerSubscriber<T> subscriber = consumerSubscriber(consumer);
        return subscribe(subscriber);
    }

    @Override
    public void unsubscribe(final SubscriptionId subscriptionId) {
        if (!closedAlreadyCalled) {
            subscribers.removeIf(subscriber -> subscriber.getSubscriptionId().equals(subscriptionId));
        } else {
            throw new AlreadyClosedException();
        }
    }

    @Override
    public PipeStatusInformation<T> getStatusInformation() {
        return new PipeStatusInformation<T>() {
            @Override
            public PipeStatistics getCurrentMessageStatistics() {
                return statisticsCollector.getCurrentStatistics();
            }

            @Override
            public List<Subscriber<T>> getAllSubscribers() {
                return subscribers;
            }
        };
    }

    @Override
    public void close(final boolean finishRemainingTasks) {
        if (!closedAlreadyCalled) {
            closedAlreadyCalled = true;
            transportMechanism.close(finishRemainingTasks);
        }
    }

    @Override
    public void close() {
        close(true);
    }

    @Override
    public boolean isClosed() {
        if (!closedAlreadyCalled) {
            return false;
        } else {
            return transportMechanism.isShutdown();
        }
    }

    @Override
    public boolean awaitTermination(final int timeout, final TimeUnit timeUnit) throws InterruptedException {
        if (!closedAlreadyCalled) {
            return false;
        } else {
            return transportMechanism.awaitTermination(timeout, timeUnit);
        }
    }
}
