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

package com.envimate.messageMate.messageBus.error;

import com.envimate.messageMate.channel.Channel;
import com.envimate.messageMate.channel.ProcessingContext;
import com.envimate.messageMate.channel.error.ChannelExceptionHandler;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.function.BiConsumer;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class DelegatingChannelExceptionHandlerForDelieveryChannel<T> implements ChannelExceptionHandler<T> {
    private final MessageBusExceptionHandler messageBusExceptionHandler;
    private final ErrorListenerHandler errorListenerHandler;
    @Setter
    private Channel<?> channel;

    public static <T> DelegatingChannelExceptionHandlerForDelieveryChannel<T> delegatingChannelExceptionHandlerForDeliveryChannel(
            final MessageBusExceptionHandler messageBusExceptionHandler, final ErrorListenerHandler errorListenerHandler) {
        return new DelegatingChannelExceptionHandlerForDelieveryChannel<>(messageBusExceptionHandler, errorListenerHandler);
    }

    @Override
    public boolean shouldSubscriberErrorBeHandledAndDeliveryAborted(final ProcessingContext<T> message, final Exception e) {
        return messageBusExceptionHandler.shouldDeliveryChannelErrorBeHandledAndDeliveryAborted(message, e, channel);
    }

    @Override
    public void handleSubscriberException(final ProcessingContext<T> message, final Exception e) {
        messageBusExceptionHandler.handleDeliveryChannelException(message, e, channel);
        final List<BiConsumer<T, Exception>> listener = getListener(message);
        messageBusExceptionHandler.callTemporaryExceptionListener(message, e, listener);
    }

    @Override
    public void handleFilterException(final ProcessingContext<T> message, final Exception e) {
        messageBusExceptionHandler.handleFilterException(message, e, channel);
        final List<BiConsumer<T, Exception>> listener = getListener(message);
        messageBusExceptionHandler.callTemporaryExceptionListener(message, e, listener);
    }

    private List<BiConsumer<T, Exception>> getListener(final ProcessingContext<T> message) {
        final Class<?> aClass = message.getPayload().getClass();
        final List<?> uncheckedListener = errorListenerHandler.listenerFor(aClass);
        @SuppressWarnings("unchecked")
        final List<BiConsumer<T, Exception>> castedListener = (List<BiConsumer<T, Exception>>) uncheckedListener;
        return castedListener;
    }
}
