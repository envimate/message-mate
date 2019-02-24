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

package com.envimate.messageMate.messageBus;

import com.envimate.messageMate.channel.Channel;
import com.envimate.messageMate.channel.ChannelType;
import com.envimate.messageMate.messageBus.brokering.MessageBusBrokerStrategyImpl;
import com.envimate.messageMate.messageBus.channelCreating.MessageBusChannelFactory;
import com.envimate.messageMate.messageBus.error.DelegatingChannelExceptionHandlerForAcceptingChannel;
import com.envimate.messageMate.messageBus.error.ErrorListenerHandlerImpl;
import com.envimate.messageMate.messageBus.error.MessageBusExceptionHandler;
import com.envimate.messageMate.pipe.configuration.AsynchronousConfiguration;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import static com.envimate.messageMate.channel.ChannelBuilder.aChannel;
import static com.envimate.messageMate.messageBus.MessageBusConsumeAction.messageBusConsumeAction;
import static com.envimate.messageMate.messageBus.MessageBusType.SYNCHRONOUS;
import static com.envimate.messageMate.messageBus.brokering.MessageBusBrokerStrategyImpl.messageBusBrokerStrategy;
import static com.envimate.messageMate.messageBus.channelCreating.SynchronousMessageBusChannelFactory.synchronousMessageBusChannelFactory;
import static com.envimate.messageMate.messageBus.error.DelegatingChannelExceptionHandlerForAcceptingChannel.channelExceptionHandlerForAcceptingChannel;
import static com.envimate.messageMate.messageBus.error.ErrorListenerHandlerImpl.errorListenerHandler;
import static com.envimate.messageMate.messageBus.error.ErrorThrowingMessageBusExceptionHandler.errorThrowingMessageBusExceptionHandler;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MessageBusBuilder {
    private MessageBusChannelFactory channelFactory;
    private MessageBusType type = SYNCHRONOUS;
    private AsynchronousConfiguration asynchronousConfiguration;
    private MessageBusExceptionHandler exceptionHandler = errorThrowingMessageBusExceptionHandler();

    public static MessageBusBuilder aMessageBus() {
        return new MessageBusBuilder();
    }

    public MessageBusBuilder forType(final MessageBusType type) {
        this.type = type;
        return this;
    }

    public MessageBusBuilder withAChannelFactory(final MessageBusChannelFactory channelFactory) {
        this.channelFactory = channelFactory;
        return this;
    }

    public MessageBusBuilder withAsynchronousConfiguration(final AsynchronousConfiguration asynchronousConfiguration) {
        this.asynchronousConfiguration = asynchronousConfiguration;
        return this;
    }

    public MessageBusBuilder withExceptionHandler(final MessageBusExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
        return this;
    }

    public MessageBus build() {
        final ChannelType channelType = map(type);
        final ErrorListenerHandlerImpl errorListenerHandler = errorListenerHandler();
        final DelegatingChannelExceptionHandlerForAcceptingChannel<Object> acceptingPipeExceptionHandler =
                channelExceptionHandlerForAcceptingChannel(exceptionHandler, errorListenerHandler);
        final MessageBusChannelFactory channelFactory = createChannelFactory(errorListenerHandler);
        final MessageBusBrokerStrategyImpl brokerStrategy = messageBusBrokerStrategy(channelFactory);
        final Channel<Object> acceptingChannel = aChannel(Object.class)
                .forType(channelType)
                .withAsynchronousConfiguration(asynchronousConfiguration)
                .withChannelExceptionHandler(acceptingPipeExceptionHandler)
                .withDefaultAction(messageBusConsumeAction(brokerStrategy))
                .build();
        acceptingPipeExceptionHandler.setChannel(acceptingChannel);
        return new MessageBusImpl(acceptingChannel, brokerStrategy, errorListenerHandler);
    }

    private MessageBusChannelFactory createChannelFactory(final ErrorListenerHandlerImpl errorListenerHandler) {
        if (this.channelFactory == null) {
            return synchronousMessageBusChannelFactory(exceptionHandler, errorListenerHandler);
        } else {
            return this.channelFactory;
        }
    }

    private ChannelType map(final MessageBusType messageBusType) {
        switch (messageBusType) {
            case SYNCHRONOUS:
                return ChannelType.SYNCHRONOUS;
            case ASYNCHRONOUS:
                return ChannelType.ASYNCHRONOUS;
            default:
                throw new IllegalArgumentException("Unknown type for message bus: " + messageBusType);
        }
    }

}
