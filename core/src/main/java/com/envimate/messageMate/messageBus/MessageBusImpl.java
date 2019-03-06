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
import com.envimate.messageMate.channel.ProcessingContext;
import com.envimate.messageMate.filtering.Filter;
import com.envimate.messageMate.filtering.FilterActions;
import com.envimate.messageMate.messageBus.internal.brokering.MessageBusBrokerStrategy;
import com.envimate.messageMate.messageBus.error.ErrorListenerHandler;
import com.envimate.messageMate.messageBus.internal.MessageBusStatusInformationAdapter;
import com.envimate.messageMate.messageBus.statistics.MessageBusStatisticsCollector;
import com.envimate.messageMate.subscribing.ConsumerSubscriber;
import com.envimate.messageMate.subscribing.Subscriber;
import com.envimate.messageMate.subscribing.SubscriptionId;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.envimate.messageMate.messageBus.internal.MessageBusStatusInformationAdapter.statusInformationAdapter;
import static com.envimate.messageMate.messageBus.statistics.ChannelBasedMessageBusStatisticsCollector.channelBasedMessageBusStatisticsCollector;
import static com.envimate.messageMate.subscribing.ConsumerSubscriber.consumerSubscriber;
import static lombok.AccessLevel.PRIVATE;

final class MessageBusImpl implements MessageBus {
    private final Channel<Object> acceptingChannel;
    private final MessageBusBrokerStrategy brokerStrategy;
    private final ErrorListenerHandler errorListenerHandler;
    private MessageBusStatusInformationAdapter statusInformationAdapter;

    MessageBusImpl(final Channel<Object> acceptingChannel, final MessageBusBrokerStrategy brokerStrategy,
                   final ErrorListenerHandler errorListenerHandler) {
        this.acceptingChannel = acceptingChannel;
        this.brokerStrategy = brokerStrategy;
        this.errorListenerHandler = errorListenerHandler;
        final MessageBusStatisticsCollector statisticsCollector = channelBasedMessageBusStatisticsCollector(acceptingChannel);
        statusInformationAdapter = statusInformationAdapter(statisticsCollector, brokerStrategy);
    }

    @Override
    public void send(final Object message) {
        acceptingChannel.send(message);
    }

    @Override
    public <T> SubscriptionId subscribe(final Class<T> messageClass, final Consumer<T> consumer) {
        final ConsumerSubscriber<T> subscriber = consumerSubscriber(consumer);
        return subscribe(messageClass, subscriber);
    }

    @Override
    public <T> SubscriptionId subscribe(final Class<T> messageClass, final Subscriber<T> subscriber) {
        brokerStrategy.addSubscriber(messageClass, subscriber);
        return subscriber.getSubscriptionId();
    }

    @Override
    public void unsubcribe(final SubscriptionId subscriptionId) {
        brokerStrategy.removeSubscriber(subscriptionId);
    }

    @Override
    public void add(final Filter<Object> filter) {
        acceptingChannel.addProcessFilter(new FilterAdapter(filter));
    }

    @Override
    public void add(final Filter<Object> filter, final int position) {
        acceptingChannel.addProcessFilter(new FilterAdapter(filter), position);
    }

    @Override
    public List<Filter<Object>> getFilter() {
        final List<Filter<Object>> filters = new LinkedList<>();
        final List<Filter<ProcessingContext<Object>>> processFilter = acceptingChannel.getProcessFilter();
        for (final Filter<ProcessingContext<Object>> filter : processFilter) {
            if (filter instanceof FilterAdapter) {
                final Filter<Object> originalFilter = ((FilterAdapter) filter).delegate;
                filters.add(originalFilter);
            } else {
                throw new IllegalStateException("Unexpected type of filter. Was the list of filter tampered with?");
            }
        }
        return filters;
    }

    @Override
    public void remove(final Filter<Object> filter) {
        final List<Filter<ProcessingContext<Object>>> processFilter = acceptingChannel.getProcessFilter();
        for (final Filter<ProcessingContext<Object>> processingContextFilter : processFilter) {
            if (processingContextFilter instanceof FilterAdapter) {
                if (((FilterAdapter) processingContextFilter).delegate.equals(filter)) {
                    acceptingChannel.removeProcessFilter(processingContextFilter);
                }
            }
        }
    }

    @Override
    public <T> SubscriptionId onError(final Class<T> errorClass, final BiConsumer<T, Exception> exceptionListener) {
        return errorListenerHandler.register(errorClass, exceptionListener);
    }

    @Override
    public <T> SubscriptionId onError(final List<Class<? extends T>> errorClasses,
                                      final BiConsumer<? extends T, Exception> exceptionListener) {
        return errorListenerHandler.register(errorClasses, exceptionListener);
    }

    @Override
    public void unregisterErrorHandler(final SubscriptionId subscriptionId) {
        errorListenerHandler.unregister(subscriptionId);
    }

    @Override
    public MessageBusStatusInformation getStatusInformation() {
        return statusInformationAdapter;
    }

    @Override
    public void close(final boolean finishRemainingTasks) {
        acceptingChannel.close(finishRemainingTasks);
    }

    @Override
    public void close() {
        close(true);
    }

    @Override
    public boolean awaitTermination(final long timeout, final TimeUnit unit) {
        return true;
    }

    @Override
    public boolean isShutdown() {
        return true;
    }

    @RequiredArgsConstructor(access = PRIVATE)
    private static final class FilterAdapter implements Filter<ProcessingContext<Object>> {
        private final Filter<Object> delegate;

        @Override
        public void apply(final ProcessingContext<Object> processingContext,
                          final FilterActions<ProcessingContext<Object>> filterActions) {
            final Object originalPayload = processingContext.getPayload();
            delegate.apply(originalPayload, new FilterActions<Object>() {
                @Override
                public void block(final Object message) {
                    if (originalPayload != message) {
                        processingContext.setPayload(message);
                    }
                    filterActions.block(processingContext);
                }

                @Override
                public void pass(final Object message) {
                    if (originalPayload != message) {
                        processingContext.setPayload(message);
                    }
                    filterActions.pass(processingContext);
                }
            });
        }
    }
}
