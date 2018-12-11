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
import com.envimate.messageMate.internal.brokering.BrokerStrategy;
import com.envimate.messageMate.internal.eventloop.TransportEventLoop;
import com.envimate.messageMate.subscribing.Subscriber;
import lombok.NonNull;

import java.util.List;

public final class MessageBusTransportProcessFactory extends AbstractTransportProcessFactory<Object> {

    private final BrokerStrategy brokerStrategy;

    private MessageBusTransportProcessFactory(final BrokerStrategy brokerStrategy,
                                              final List<Filter<Object>> filters,
                                              final TransportEventLoop<Object> eventLoop) {
        super(filters, eventLoop);
        this.brokerStrategy = brokerStrategy;
    }

    public static MessageBusTransportProcessFactory messageBusTransportProcessFactory(
            @NonNull final BrokerStrategy brokerStrategy,
            @NonNull final List<Filter<Object>> filters,
            @NonNull final TransportEventLoop<Object> eventLoop) {
        return new MessageBusTransportProcessFactory(brokerStrategy, filters, eventLoop);
    }

    @Override
    public List<Subscriber<Object>> calculateReceivingSubscriber(final Object message) {
        return brokerStrategy.calculateReceivingSubscriber(message);
    }

}
