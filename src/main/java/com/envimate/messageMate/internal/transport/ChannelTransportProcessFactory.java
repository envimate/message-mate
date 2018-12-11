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
import com.envimate.messageMate.subscribing.Subscriber;
import lombok.NonNull;

import java.util.List;

public final class ChannelTransportProcessFactory<T> extends AbstractTransportProcessFactory<T> {

    private final List<Subscriber<T>> subscribers;

    private ChannelTransportProcessFactory(final List<Filter<T>> filters,
                                           final TransportEventLoop<T> eventLoop,
                                           final List<Subscriber<T>> subscribers) {
        super(filters, eventLoop);
        this.subscribers = subscribers;
    }

    public static <T> ChannelTransportProcessFactory<T> channelTransportProcessFactory(
            @NonNull final List<Filter<T>> filters,
            @NonNull final TransportEventLoop<T> eventLoop,
            @NonNull final List<Subscriber<T>> subscribers) {
        return new ChannelTransportProcessFactory<>(filters, eventLoop, subscribers);
    }

    @Override
    public List<Subscriber<T>> calculateReceivingSubscriber(final Object message) {
        return subscribers;
    }

}
