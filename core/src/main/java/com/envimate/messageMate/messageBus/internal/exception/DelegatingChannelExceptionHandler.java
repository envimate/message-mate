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
import com.envimate.messageMate.channel.ProcessingContext;
import com.envimate.messageMate.channel.exception.ChannelExceptionHandler;
import com.envimate.messageMate.messageBus.exception.MessageBusExceptionHandler;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class DelegatingChannelExceptionHandler<T> implements ChannelExceptionHandler<T> {
    private final MessageBusExceptionHandler messageBusExceptionHandler;
    private final DeliveryAbortDecision<T> deliveryAbortDecision;
    @Setter
    private Channel<?> channel;

    public static <T> DelegatingChannelExceptionHandler<T> delegatingChannelExceptionHandlerForDeliveryChannel(
            final MessageBusExceptionHandler messageBusExceptionHandler) {
        final DeliveryAbortDecision<T> d = messageBusExceptionHandler::shouldDeliveryChannelErrorBeHandledAndDeliveryAborted;
        return new DelegatingChannelExceptionHandler<>(messageBusExceptionHandler, d);
    }

    public static <T> DelegatingChannelExceptionHandler<T> delegatingChannelExceptionHandlerForAcceptingChannel(
            final MessageBusExceptionHandler messageBusExceptionHandler) {
        final DeliveryAbortDecision<T> d = (m, e, c) -> true;
        return new DelegatingChannelExceptionHandler<>(messageBusExceptionHandler, d);
    }

    @Override
    public boolean shouldSubscriberErrorBeHandledAndDeliveryAborted(final ProcessingContext<T> message, final Exception e) {
        return deliveryAbortDecision.shouldSubscriberErrorBeHandledAndDeliveryAborted(message, e, channel);
    }

    @Override
    public void handleSubscriberException(final ProcessingContext<T> message, final Exception e) {
        messageBusExceptionHandler.handleDeliveryChannelException(message, e, channel);
    }

    @Override
    public void handleFilterException(final ProcessingContext<T> message, final Exception e) {
        messageBusExceptionHandler.handleFilterException(message, e, channel);
    }

    private interface DeliveryAbortDecision<T> {

        boolean shouldSubscriberErrorBeHandledAndDeliveryAborted(ProcessingContext<T> m, Exception e, Channel<?> c);
    }
}
