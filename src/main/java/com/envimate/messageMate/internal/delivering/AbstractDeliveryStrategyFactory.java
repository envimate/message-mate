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

package com.envimate.messageMate.internal.delivering;

import com.envimate.messageMate.configuration.PipeConfiguration;
import com.envimate.messageMate.configuration.MessageBusConfiguration;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import static com.envimate.messageMate.internal.delivering.AsynchronousDeliveryStrategyConfiguration.fromPipeConfiguration;
import static com.envimate.messageMate.internal.delivering.AsynchronousDeliveryStrategyConfiguration.fromMessageBusConfiguration;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AbstractDeliveryStrategyFactory {

    public static <T> DeliveryStrategyFactory<T> deliveryStrategyForType(final MessageBusConfiguration configuration) {
        final DeliveryType deliveryType = configuration.getDeliveryType();
        final AsynchronousDeliveryStrategyConfiguration deliveryStrategyConfig = fromMessageBusConfiguration(configuration);
        return new DeliveryStrategyFactoryImpl<>(deliveryType, deliveryStrategyConfig);
    }

    public static <T> DeliveryStrategyFactory<T> deliveryStrategyForType(final PipeConfiguration configuration) {
        final DeliveryType deliveryType = configuration.getDeliveryType();
        final AsynchronousDeliveryStrategyConfiguration deliveryStrategyConfiguration = fromPipeConfiguration(configuration);
        return new DeliveryStrategyFactoryImpl<>(deliveryType, deliveryStrategyConfiguration);
    }
}
