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

package com.envimate.messageMate.messageBus.channelCreating;

import com.envimate.messageMate.channel.Channel;
import com.envimate.messageMate.messageBus.error.DelegatingChannelExceptionHandlerForDelieveryChannel;
import com.envimate.messageMate.messageBus.error.ErrorListenerHandler;
import com.envimate.messageMate.messageBus.error.MessageBusExceptionHandler;
import com.envimate.messageMate.subscribing.Subscriber;
import lombok.RequiredArgsConstructor;

import static com.envimate.messageMate.channel.ChannelBuilder.aChannel;
import static com.envimate.messageMate.channel.action.Subscription.subscription;
import static com.envimate.messageMate.messageBus.error.DelegatingChannelExceptionHandlerForDelieveryChannel.delegatingChannelExceptionHandlerForDeliveryChannel;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class SynchronousMessageBusChannelFactory implements MessageBusChannelFactory {
    private final MessageBusExceptionHandler exceptionHandler;
    private final ErrorListenerHandler errorListenerHandler;

    public static SynchronousMessageBusChannelFactory synchronousMessageBusChannelFactory(
            final MessageBusExceptionHandler exceptionHandler, final ErrorListenerHandler errorListenerHandler) {
        return new SynchronousMessageBusChannelFactory(exceptionHandler, errorListenerHandler);
    }

    @Override
    public <T> Channel<?> createChannel(final Class<T> tClass, final Subscriber<T> subscriber) {
        final DelegatingChannelExceptionHandlerForDelieveryChannel<T> delegatingChannelExceptionHandler =
                delegatingChannelExceptionHandlerForDeliveryChannel(exceptionHandler, errorListenerHandler);
        final Channel<T> channel = aChannel(tClass)
                .withDefaultAction(subscription())
                .withChannelExceptionHandler(delegatingChannelExceptionHandler)
                .build();
        delegatingChannelExceptionHandler.setChannel(channel);
        return channel;
    }
}
