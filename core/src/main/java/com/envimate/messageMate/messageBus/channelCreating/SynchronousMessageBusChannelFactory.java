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
import com.envimate.messageMate.messageBus.EventType;
import com.envimate.messageMate.messageBus.exception.MessageBusExceptionHandler;
import com.envimate.messageMate.messageBus.internal.exception.DelegatingChannelExceptionHandler;
import com.envimate.messageMate.subscribing.Subscriber;
import lombok.RequiredArgsConstructor;

import static com.envimate.messageMate.channel.ChannelBuilder.aChannel;
import static com.envimate.messageMate.channel.action.Subscription.subscription;
import static com.envimate.messageMate.messageBus.internal.exception.DelegatingChannelExceptionHandler.delegatingChannelExceptionHandlerForDeliveryChannel;
import static lombok.AccessLevel.PRIVATE;

/**
 * The default {@code MessageBusChannelFactory}, that creates synchronous {@code Channels}.
 *
 * @see <a href="https://github.com/envimate/message-mate#configuring-the-messagebus">Message Mate Documentation</a>
 */
@RequiredArgsConstructor(access = PRIVATE)
public final class SynchronousMessageBusChannelFactory implements MessageBusChannelFactory {

    /**
     * Factory method to create a new {@code SynchronousMessageBusChannelFactory}.
     *
     * @return the newly {@code SynchronousMessageBusChannelFactory}
     */
    public static SynchronousMessageBusChannelFactory synchronousMessageBusChannelFactory() {
        return new SynchronousMessageBusChannelFactory();
    }

    @Override
    public Channel<Object> createChannel(final EventType eventType, final Subscriber<?> subscriber, final MessageBusExceptionHandler messageBusExceptionHandler) {
        final DelegatingChannelExceptionHandler<Object> delegatingChannelExceptionHandler =
                delegatingChannelExceptionHandlerForDeliveryChannel(messageBusExceptionHandler);
        final Channel<Object> channel = aChannel(Object.class)
                .withDefaultAction(subscription())
                .withChannelExceptionHandler(delegatingChannelExceptionHandler)
                .build();
        delegatingChannelExceptionHandler.setChannel(channel);
        return channel;
    }
}
