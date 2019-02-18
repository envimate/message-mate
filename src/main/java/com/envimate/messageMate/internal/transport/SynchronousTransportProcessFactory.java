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

package com.envimate.messageMate.internal.transport;

import com.envimate.messageMate.filtering.Filter;
import com.envimate.messageMate.internal.eventloop.TransportEventLoop;
import com.envimate.messageMate.internal.filtering.FilterApplier;
import com.envimate.messageMate.internal.filtering.PostFilterActions;
import com.envimate.messageMate.subscribing.Subscriber;

import java.util.Date;
import java.util.List;

import static com.envimate.messageMate.internal.filtering.FilterApplierFactory.filterApplier;

public class SynchronousTransportProcessFactory<T> implements MessageTransportProcessFactory<T> {

    private final FilterApplier<T> filterApplier;
    private final List<Filter<T>> filters;
    private final TransportEventLoop<T> eventLoop;
    private final SubscriberCalculation<T> subscriberCalculation;
    private boolean closed;

    protected SynchronousTransportProcessFactory(final List<Filter<T>> filters, final TransportEventLoop<T> eventLoop,
                                                 final SubscriberCalculation<T> subscriberCalculation) {
        this.filterApplier = filterApplier();
        this.filters = filters;
        this.eventLoop = eventLoop;
        this.subscriberCalculation = subscriberCalculation;
    }

    @Override
    public MessageTransportProcess<T> getNext(final T message) {
        return initialMessage -> {
            eventLoop.messageTransportStarted(initialMessage);
            final List<Subscriber<T>> receivers = subscriberCalculation.apply(initialMessage);
            eventLoop.messageFilteringStarted(initialMessage);
            filterApplier.applyAll(initialMessage, filters, receivers, new PostFilterActions<T>() {
                @Override
                public void onAllPassed(final T message) {
                    eventLoop.messagePassedAllFilter(message);
                    eventLoop.messageTransportFinished(message);
                    eventLoop.requestDelivery(message, receivers);
                }

                @Override
                public void onReplaced(final T message) {
                    eventLoop.messageTransportFinished(initialMessage);
                    eventLoop.messageReplacedByFilter(message);
                }

                @Override
                public void onBlock(final T message) {
                    eventLoop.messageTransportFinished(message);
                    eventLoop.messageBlockedByFilter(message);
                }

                @Override
                public void onForgotten(final T message) {
                    eventLoop.messageTransportFinished(message);
                    eventLoop.messageForgottenByFilter(message);
                }
            });
        };
    }

    @Override
    public synchronized void close(final boolean finishRemainingTasks) {
        closed = true;
    }

    @Override
    public boolean awaitTermination(final Date deadline) {
        return closed;
    }

    @Override
    public boolean isShutdown() {
        return closed;
    }
}
