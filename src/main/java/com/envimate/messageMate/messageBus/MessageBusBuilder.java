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

import com.envimate.messageMate.configuration.MessageBusConfiguration;
import com.envimate.messageMate.internal.accepting.MessageAcceptingStrategyFactory;
import com.envimate.messageMate.internal.brokering.BrokerStrategy;
import com.envimate.messageMate.internal.brokering.BrokerStrategyType;
import com.envimate.messageMate.internal.delivering.DeliveryStrategyFactory;
import com.envimate.messageMate.internal.delivering.DeliveryType;
import com.envimate.messageMate.internal.statistics.StatisticsCollector;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import static com.envimate.messageMate.internal.accepting.MessageAcceptingStrategyAbstractFactory.aMessageAcceptingStrategyFactory;
import static com.envimate.messageMate.internal.brokering.BrokerStrategyFactory.aBrokerStrategy;
import static com.envimate.messageMate.internal.delivering.AbstractDeliveryStrategyFactory.deliveryStrategyForType;
import static com.envimate.messageMate.internal.statistics.StatisticsCollectorFactory.aStatisticsCollector;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MessageBusBuilder {

    private MessageBusConfiguration messageBusConfiguration = MessageBusConfiguration.defaultConfiguration();
    private DeliveryStrategyFactory<Object> deliveryStrategyFactory;
    private BrokerStrategy brokerStrategy;
    private MessageAcceptingStrategyFactory<Object> messageAcceptingStrategyFactory;
    private StatisticsCollector statisticsCollector;

    public static MessageBusBuilder aMessageBus() {
        return new MessageBusBuilder();
    }

    public MessageBusBuilder withDeliveryType(final DeliveryType deliveryType) {
        messageBusConfiguration.setDeliveryType(deliveryType);
        return this;
    }

    public MessageBusBuilder withACustomDeliveryStrategyFactory(final DeliveryStrategyFactory<Object> deliveryStrategyFactory) {
        this.deliveryStrategyFactory = deliveryStrategyFactory;
        return this;
    }

    public MessageBusBuilder withBrokerType(final BrokerStrategyType brokerStrategyType) {
        messageBusConfiguration.setBrokerStrategyType(brokerStrategyType);
        return this;
    }

    public MessageBusBuilder withACustomBrokerStrategy(final BrokerStrategy brokerStrategy) {
        this.brokerStrategy = brokerStrategy;
        return this;
    }

    public MessageBusBuilder withACustomMessageAcceptingStrategyFactory(
            final MessageAcceptingStrategyFactory<Object> messageAcceptingStrategyFactory) {
        this.messageAcceptingStrategyFactory = messageAcceptingStrategyFactory;
        return this;
    }

    public MessageBusBuilder withMessageBusConfiguration(@NonNull final MessageBusConfiguration messageBusConfiguration) {
        this.messageBusConfiguration = messageBusConfiguration;
        return this;
    }

    public MessageBusBuilder withStatisticsCollector(final StatisticsCollector statisticsCollector) {
        this.statisticsCollector = statisticsCollector;
        return this;
    }

    public MessageBus build() {
        final StatisticsCollector statisticsCollector = fieldOrDefault(this.statisticsCollector, aStatisticsCollector());
        final MessageAcceptingStrategyFactory<Object> msgAccStrFactory = fieldOrDefault(this.messageAcceptingStrategyFactory,
                aMessageAcceptingStrategyFactory());
        final BrokerStrategy brokerStrategy = fieldOrDefault(this.brokerStrategy, aBrokerStrategy(messageBusConfiguration));
        final DeliveryStrategyFactory<Object> deliveryStrategy = fieldOrDefault(this.deliveryStrategyFactory,
                deliveryStrategyForType(messageBusConfiguration));
        return new MessageBusImpl(msgAccStrFactory, brokerStrategy, deliveryStrategy, statisticsCollector);
    }

    private <T> T fieldOrDefault(final T field, final T defaultValue) {
        if (field != null) {
            return field;
        } else {
            return defaultValue;
        }
    }
}
