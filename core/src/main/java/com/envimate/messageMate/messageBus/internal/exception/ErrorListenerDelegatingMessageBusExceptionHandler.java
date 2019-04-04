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

package com.envimate.messageMate.messageBus.internal.exception;

import com.envimate.messageMate.channel.Channel;
import com.envimate.messageMate.internal.exceptions.BubbleUpWrappedException;
import com.envimate.messageMate.messageBus.exception.MessageBusExceptionHandler;
import com.envimate.messageMate.messageBus.exception.MessageBusExceptionListener;
import com.envimate.messageMate.processingContext.ProcessingContext;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class ErrorListenerDelegatingMessageBusExceptionHandler implements MessageBusExceptionHandler {
    private final MessageBusExceptionHandler delegate;
    private final ExceptionListenerHandler exceptionListenerHandler;

    public static ErrorListenerDelegatingMessageBusExceptionHandler errorListenerDelegatingMessageBusExceptionHandler(
            final MessageBusExceptionHandler delegate, final ExceptionListenerHandler exceptionListenerHandler) {
        return new ErrorListenerDelegatingMessageBusExceptionHandler(delegate, exceptionListenerHandler);
    }

    @Override
    public boolean shouldDeliveryChannelErrorBeHandledAndDeliveryAborted(final ProcessingContext<?> message, final Exception e,
                                                                         final Channel<?> channel) {
        return delegate.shouldDeliveryChannelErrorBeHandledAndDeliveryAborted(message, e, channel);
    }

    @Override
    public void handleDeliveryChannelException(final ProcessingContext<?> message, final Exception e, final Channel<?> channel) {
        try {
            callDeliveryExceptionHandlerIfNotBubbleUpException(message, e, channel);
        } finally {
            callTemporaryHandlerIfNotBubbleUpException(message, e, channel);
        }
    }

    @Override
    public void handleFilterException(final ProcessingContext<?> message, final Exception e, final Channel<?> channel) {
        try {
            callFilterExceptionHandlerIfNotBubbleUpException(message, e, channel);
        } finally {
            callTemporaryHandlerIfNotBubbleUpException(message, e, channel);
        }
    }

    private List<MessageBusExceptionListener> getListener(final ProcessingContext<?> message) {
        return exceptionListenerHandler.listenerFor(message);
    }

    private void callDeliveryExceptionHandlerIfNotBubbleUpException(final ProcessingContext<?> message, final Exception e, final Channel<?> channel) {
        if (e instanceof BubbleUpWrappedException) {
            return;
        }
        try {
            delegate.handleDeliveryChannelException(message, e, channel);
        } catch (final Exception rethrownException) {
            throw new BubbleUpWrappedException(rethrownException);
        }
    }

    private void callFilterExceptionHandlerIfNotBubbleUpException(final ProcessingContext<?> message, final Exception e, final Channel<?> channel) {
        if (e instanceof BubbleUpWrappedException) {
            return;
        }
        try {
            delegate.handleFilterException(message, e, channel);
        } catch (final Exception rethrownException) {
            throw new BubbleUpWrappedException(rethrownException);
        }
    }

    private void callTemporaryHandlerIfNotBubbleUpException(final ProcessingContext<?> message, final Exception e, final Channel<?> channel) {
        if (e instanceof BubbleUpWrappedException) {
            return;
        }
        final List<MessageBusExceptionListener> listener = getListener(message);
        delegate.callTemporaryExceptionListener(message, e, listener);
    }
}
