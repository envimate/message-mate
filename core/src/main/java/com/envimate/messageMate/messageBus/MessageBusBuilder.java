/*
 * Copyright (c) 2019 envimate GmbH - https://envimate.com/.
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
import com.envimate.messageMate.configuration.AsynchronousConfiguration;
import com.envimate.messageMate.messageBus.channelCreating.MessageBusChannelFactory;
import com.envimate.messageMate.messageBus.exception.MessageBusExceptionHandler;
import com.envimate.messageMate.messageBus.internal.brokering.MessageBusBrokerStrategy;
import com.envimate.messageMate.messageBus.internal.correlationIds.CorrelationBasedSubscriptions;
import com.envimate.messageMate.messageBus.internal.correlationIds.CorrelationBasedSubscriptionsImpl;
import com.envimate.messageMate.messageBus.internal.exception.DelegatingChannelExceptionHandler;
import com.envimate.messageMate.messageBus.internal.exception.ExceptionListenerHandlerImpl;
import lombok.RequiredArgsConstructor;

import static com.envimate.messageMate.channel.ChannelBuilder.aChannel;
import static com.envimate.messageMate.messageBus.MessageBusConsumeAction.messageBusConsumeAction;
import static com.envimate.messageMate.messageBus.MessageBusType.SYNCHRONOUS;
import static com.envimate.messageMate.messageBus.channelCreating.SynchronousMessageBusChannelFactory.synchronousMessageBusChannelFactory;
import static com.envimate.messageMate.messageBus.exception.ErrorThrowingMessageBusExceptionHandler.errorThrowingMessageBusExceptionHandler;
import static com.envimate.messageMate.messageBus.internal.brokering.MessageBusBrokerStrategyImpl.messageBusBrokerStrategyImpl2;
import static com.envimate.messageMate.messageBus.internal.correlationIds.CorrelationBasedSubscriptionsImpl.correlationBasedSubscriptions;
import static com.envimate.messageMate.messageBus.internal.exception.DelegatingChannelExceptionHandler.delegatingChannelExceptionHandlerForAcceptingChannel;
import static com.envimate.messageMate.messageBus.internal.exception.ErrorListenerDelegatingMessageBusExceptionHandler.errorListenerDelegatingMessageBusExceptionHandler;
import static com.envimate.messageMate.messageBus.internal.exception.ExceptionListenerHandlerImpl.errorListenerHandler;
import static lombok.AccessLevel.PRIVATE;

/**
 * The {@code MessageBusBuilder} class provides a fluent interface to create and configure a {@link MessageBus}.
 *
 * <p>Most of the configurable properties have default values set by the builder. Per default a synchronous {@code MessageBus}
 * is created with an exception handler, that throws exception once they occur. The default {@link MessageBusChannelFactory}
 * creates synchronous class specific {@code Channels}.</p>
 *
 * @see <a href="https://github.com/envimate/configuring-the-messagebus#">Message Mate Documentation</a>
 */
@RequiredArgsConstructor(access = PRIVATE)
public final class MessageBusBuilder {
    private MessageBusChannelFactory channelFactory;
    private MessageBusType type = SYNCHRONOUS;
    private AsynchronousConfiguration asynchronousConfiguration;
    private MessageBusExceptionHandler exceptionHandler = errorThrowingMessageBusExceptionHandler();

    /**
     * Creates a new {@code MessageBusBuilder}.
     *
     * @return a new {@code MessageBusBuilder}.
     */
    public static MessageBusBuilder aMessageBus() {
        return new MessageBusBuilder();
    }

    /**
     * Overrides the {@code MessageBusType}. Per default {@code MessageBusType.SYNCHRONOUS} is configured.
     *
     * @param type the {@code MessageBusType} to overwrite
     * @return the same {@code MessageBusBuilder} instance the method was called one
     */
    public MessageBusBuilder forType(final MessageBusType type) {
        this.type = type;
        return this;
    }

    /**
     * Overrides the the {@code MessageBusChannelFactory}.
     *
     * @param channelFactory the new {@code MessageBusChannelFactory}.
     * @return the same {@code MessageBusBuilder} instance the method was called one
     */
    public MessageBusBuilder withAChannelFactory(final MessageBusChannelFactory channelFactory) {
        this.channelFactory = channelFactory;
        return this;
    }

    /**
     * In case an asynchronous {@code MessageBus} is created an {@code AsynchronousConfiguration} has to be provides with this
     * method.
     *
     * @param asynchronousConfiguration the required {@code AsynchronousConfiguration}
     * @return the same {@code MessageBusBuilder} instance the method was called one
     */
    public MessageBusBuilder withAsynchronousConfiguration(final AsynchronousConfiguration asynchronousConfiguration) {
        this.asynchronousConfiguration = asynchronousConfiguration;
        return this;
    }

    /**
     * Overrides the default exception throwing {@code MessageBusExceptionHandler}.
     *
     * @param exceptionHandler the new {@code MessageBusExceptionHandler}
     * @return the same {@code MessageBusBuilder} instance the method was called one
     */
    public MessageBusBuilder withExceptionHandler(final MessageBusExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
        return this;
    }

    /**
     * Creates the {@code MessageBus}
     *
     * @return the newly created {@code MessageBus}
     */
    public MessageBus build() {
        final ExceptionListenerHandlerImpl errorListenerHandler = errorListenerHandler();
        final MessageBusExceptionHandler exceptionHandler = createExceptionHandler(errorListenerHandler);
        final MessageBusBrokerStrategy brokerStrategy = createBrokerStrategy(exceptionHandler);
        final CorrelationBasedSubscriptionsImpl corSubscriptions = correlationBasedSubscriptions();
        final Channel<Object> acceptingChannel = createAcceptingChannel(brokerStrategy, exceptionHandler, corSubscriptions);
        return new MessageBusImpl(acceptingChannel, brokerStrategy, corSubscriptions, errorListenerHandler);
    }

    private MessageBusBrokerStrategy createBrokerStrategy(final MessageBusExceptionHandler exceptionHandler) {
        final MessageBusChannelFactory channelFactory = createChannelFactory();
        return messageBusBrokerStrategyImpl2(channelFactory, exceptionHandler);
    }

    private MessageBusExceptionHandler createExceptionHandler(final ExceptionListenerHandlerImpl errorListenerHandler) {
        return errorListenerDelegatingMessageBusExceptionHandler(exceptionHandler, errorListenerHandler);
    }

    private MessageBusChannelFactory createChannelFactory() {
        if (this.channelFactory == null) {
            return synchronousMessageBusChannelFactory();
        } else {
            return this.channelFactory;
        }
    }

    private Channel<Object> createAcceptingChannel(final MessageBusBrokerStrategy brokerStrategy,
                                                   final MessageBusExceptionHandler exceptionHandler,
                                                   final CorrelationBasedSubscriptions correlationBasedSubscriptions) {
        final ChannelType channelType = map(type);
        final DelegatingChannelExceptionHandler<Object> acceptingPipeExceptionHandler =
                delegatingChannelExceptionHandlerForAcceptingChannel(exceptionHandler);
        final Channel<Object> acceptingChannel = aChannel(Object.class)
                .forType(channelType)
                .withAsynchronousConfiguration(asynchronousConfiguration)
                .withChannelExceptionHandler(acceptingPipeExceptionHandler)
                .withDefaultAction(messageBusConsumeAction(brokerStrategy, correlationBasedSubscriptions))
                .build();
        acceptingPipeExceptionHandler.setChannel(acceptingChannel);
        return acceptingChannel;
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
