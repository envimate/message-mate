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

import com.envimate.messageMate.internal.eventloop.TransportEventLoop;
import com.envimate.messageMate.subscribing.Subscriber;

import java.util.Date;
import java.util.List;

public class SynchronousTransportProcessFactory<T> implements MessageTransportProcessFactory<T> {
    private final TransportEventLoop<T> eventLoop;
    private final SubscriberCalculation<T> subscriberCalculation;
    private boolean closed;

    protected SynchronousTransportProcessFactory(final TransportEventLoop<T> eventLoop,
                                                 final SubscriberCalculation<T> subscriberCalculation) {
        this.eventLoop = eventLoop;
        this.subscriberCalculation = subscriberCalculation;
    }

    @Override
    public MessageTransportProcess<T> getNext(final T message) {
        return initialMessage -> {
            eventLoop.messageTransportStarted(initialMessage);
            final List<Subscriber<T>> receivers = subscriberCalculation.apply(initialMessage);
            eventLoop.messageTransportFinished(message);
            eventLoop.requestDelivery(message, receivers);
            eventLoop.messageFilteringStarted(initialMessage);
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
