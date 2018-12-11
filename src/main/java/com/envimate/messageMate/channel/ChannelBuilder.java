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
import com.envimate.messageMate.internal.accepting.MessageAcceptingStrategyFactory;
import com.envimate.messageMate.internal.delivering.DeliveryStrategyFactory;
import com.envimate.messageMate.internal.delivering.DeliveryType;
import com.envimate.messageMate.internal.statistics.StatisticsCollector;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import static com.envimate.messageMate.internal.accepting.MessageAcceptingStrategyAbstractFactory.aMessageAcceptingStrategyFactory;
import static com.envimate.messageMate.internal.delivering.AbstractDeliveryStrategyFactory.deliveryStrategyForType;
import static com.envimate.messageMate.internal.statistics.StatisticsCollectorFactory.aStatisticsCollector;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ChannelBuilder<T> {
    private ChannelConfiguration configuration = ChannelConfiguration.defaultConfiguration();
    private DeliveryStrategyFactory<T> deliveryStrategyFactory;
    private MessageAcceptingStrategyFactory<T> messageAcceptingStrategyFactory;
    private StatisticsCollector statisticsCollector;

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
        final StatisticsCollector statisticsCollector = fieldOrDefault(this.statisticsCollector, aStatisticsCollector());
        final MessageAcceptingStrategyFactory<T> msgAccStrategyFactory = fieldOrDefault(this.messageAcceptingStrategyFactory,
                aMessageAcceptingStrategyFactory());
        final DeliveryStrategyFactory<T> deliveryStrategyFactory = fieldOrDefault(this.deliveryStrategyFactory,
                deliveryStrategyForType(configuration));
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
