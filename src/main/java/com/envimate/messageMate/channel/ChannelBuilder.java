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

package com.envimate.messageMate.channel;

import com.envimate.messageMate.configuration.ChannelConfiguration;
import com.envimate.messageMate.filtering.Filter;
import com.envimate.messageMate.internal.accepting.MessageAcceptingStrategy;
import com.envimate.messageMate.internal.accepting.MessageAcceptingStrategyFactory;
import com.envimate.messageMate.internal.accepting.MessageAcceptingStrategyType;
import com.envimate.messageMate.internal.delivering.DeliveryStrategy;
import com.envimate.messageMate.internal.delivering.DeliveryStrategyFactory;
import com.envimate.messageMate.internal.delivering.DeliveryType;
import com.envimate.messageMate.internal.eventloop.ChannelEventLoopImpl;
import com.envimate.messageMate.internal.statistics.StatisticsCollector;
import com.envimate.messageMate.internal.transport.ChannelTransportProcessFactory;
import com.envimate.messageMate.subscribing.Subscriber;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.envimate.messageMate.internal.accepting.MessageAcceptingStrategyAbstractFactory.aMessageAcceptingStrategyFactory;
import static com.envimate.messageMate.internal.delivering.AbstractDeliveryStrategyFactory.deliveryStrategyForType;
import static com.envimate.messageMate.internal.statistics.StatisticsCollectorFactory.aStatisticsCollector;
import static com.envimate.messageMate.internal.transport.ChannelTransportProcessFactory.channelTransportProcessFactory;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ChannelBuilder<T> {
    private ChannelConfiguration configuration = ChannelConfiguration.defaultConfiguration();
    private DeliveryStrategyFactory<T> deliveryStrategyFactory;
    private MessageAcceptingStrategyFactory<T> messageAcceptingStrategyFactory;
    private StatisticsCollector statisticsCollector;

    public static <T> ChannelBuilder<T> aChannel() {
        return new ChannelBuilder<>();
    }

    public static <T> ChannelBuilder<T> aChannelForClass(final Class<T> tClass) {
        return new ChannelBuilder<>();
    }

    public ChannelBuilder<T> withDeliveryType(@NonNull final DeliveryType deliveryType) {
        configuration.setDeliveryType(deliveryType);
        return this;
    }

    public ChannelBuilder<T> withConfiguration(@NonNull final ChannelConfiguration channelConfiguration) {
        this.configuration = channelConfiguration;
        return this;
    }

    public ChannelBuilder<T> withACustomDeliveryStrategyFactory(final DeliveryStrategyFactory<T> deliveryStrategyFactory) {
        this.deliveryStrategyFactory = deliveryStrategyFactory;
        return this;
    }

    public ChannelBuilder<T> withACustomMessageAcceptingStrategyFactory(
            final MessageAcceptingStrategyFactory<T> messageAcceptingStrategyFactory) {
        this.messageAcceptingStrategyFactory = messageAcceptingStrategyFactory;
        return this;
    }

    public ChannelBuilder<T> withStatisticsCollector(final StatisticsCollector statisticsCollector) {
        this.statisticsCollector = statisticsCollector;
        return this;
    }

    public Channel<T> build() {
        final ChannelEventLoopImpl<T> channelEventLoop = new ChannelEventLoopImpl<>();

        final StatisticsCollector statisticsCollector = fieldOrDefault(this.statisticsCollector, aStatisticsCollector());

        final MessageAcceptingStrategyType messageAcceptingStrategyType = configuration.getMessageAcceptingStrategyType();
        final MessageAcceptingStrategyFactory<T> msgAccStrategyFactory = fieldOrDefault(this.messageAcceptingStrategyFactory,
                aMessageAcceptingStrategyFactory(messageAcceptingStrategyType));

        final DeliveryStrategyFactory<T> deliveryStrategyFactory = fieldOrDefault(this.deliveryStrategyFactory,
                deliveryStrategyForType(configuration));

        final MessageAcceptingStrategy<T> messageAcceptingStrategy = msgAccStrategyFactory.createNew(channelEventLoop);
        final DeliveryStrategy<T> deliveryStrategy = deliveryStrategyFactory.createNew(channelEventLoop);
        final List<Subscriber<T>> subscribers = new CopyOnWriteArrayList<>();
        final List<Filter<T>> filters = new CopyOnWriteArrayList<>();
        final ChannelTransportProcessFactory<T> channelTPF = channelTransportProcessFactory(filters, channelEventLoop, subscribers);
        channelEventLoop.setRequiredObjects(messageAcceptingStrategy, channelTPF, deliveryStrategy, statisticsCollector);

        return new ChannelImpl<>(msgAccStrategyFactory, deliveryStrategyFactory, statisticsCollector);
    }

    private <R> R fieldOrDefault(final R field, final R defaultValue) {
        if (field != null) {
            return field;
        } else {
            return defaultValue;
        }
    }

}
