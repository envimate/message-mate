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
import lombok.RequiredArgsConstructor;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class ErrorListenerDelegatingMessageBusExceptionHandler implements MessageBusExceptionHandler {
    private final MessageBusExceptionHandler delegate;
    private final ErrorListenerHandler errorListenerHandler;

    public static ErrorListenerDelegatingMessageBusExceptionHandler errorListenerDelegatingMessageBusExceptionHandler(
            final MessageBusExceptionHandler delegate, final ErrorListenerHandler errorListenerHandler) {
        return new ErrorListenerDelegatingMessageBusExceptionHandler(delegate, errorListenerHandler);
    }

    @Override
    public boolean shouldDeliveryChannelErrorBeHandledAndDeliveryAborted(final ProcessingContext<?> message, final Exception e,
                                                                         final Channel<?> channel) {
        return delegate.shouldDeliveryChannelErrorBeHandledAndDeliveryAborted(message, e, channel);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void handleDeliveryChannelException(final ProcessingContext<?> message, final Exception e, final Channel<?> channel) {
        try {
            delegate.handleDeliveryChannelException(message, e, channel);
        } finally {
            @SuppressWarnings("raw")
            final List listener = getListener(message);
            delegate.callTemporaryExceptionListener(message, e, listener);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void handleFilterException(final ProcessingContext<?> message, final Exception e, final Channel<?> channel) {
        try {
            delegate.handleDeliveryChannelException(message, e, channel);
        } finally {
            final List listener = getListener(message);
            delegate.callTemporaryExceptionListener(message, e, listener);
        }
    }

    @SuppressWarnings("rawtypes")
    private List getListener(final ProcessingContext<?> message) {
        final Class<?> aClass = message.getPayload().getClass();
        final List listener = errorListenerHandler.listenerFor(aClass);
        return listener;
    }
}
