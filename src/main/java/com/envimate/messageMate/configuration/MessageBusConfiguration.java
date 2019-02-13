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

package com.envimate.messageMate.configuration;

import com.envimate.messageMate.internal.accepting.MessageAcceptingStrategyType;
import com.envimate.messageMate.internal.brokering.BrokerStrategyType;
import com.envimate.messageMate.internal.delivering.DeliveryType;
import lombok.*;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static com.envimate.messageMate.configuration.ExceptionCatchingCondition.allCatchingExceptionCondition;
import static com.envimate.messageMate.internal.accepting.MessageAcceptingStrategyType.ATOMIC;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MessageBusConfiguration {

    public static final int DEFAULT_CORE_POOL_SIZE = 2;
    public static final int DEFAULT_MAXIMUM_POOL_SIZE = 2;
    public static final int DEFAULT_MAXIMUM_TIMEOUT = 60;
    public static final TimeUnit DEFAULT_TIMEUNIT = TimeUnit.SECONDS;
    public static final LinkedBlockingQueue<Runnable> DEFAULT_WORKING_QUEUE = new LinkedBlockingQueue<>();
    public static final MessageAcceptingStrategyType DEFAULT_MESSAGE_ACCEPTING_STRATEGY_TYPE = ATOMIC;

    @Getter
    @Setter
    private DeliveryType deliveryType = DeliveryType.SYNCHRONOUS;

    @Getter
    @Setter
    private int corePoolSize = DEFAULT_CORE_POOL_SIZE;

    @Getter
    @Setter
    private int maximumPoolSize = DEFAULT_MAXIMUM_POOL_SIZE;

    @Getter
    @Setter
    private int maximumTimeout = DEFAULT_MAXIMUM_TIMEOUT;

    @Getter
    @Setter
    private TimeUnit timeoutTimeUnit = DEFAULT_TIMEUNIT;

    @Getter
    @Setter
    private BlockingQueue<Runnable> threadPoolWorkingQueue = DEFAULT_WORKING_QUEUE;

    @Getter
    @Setter
    private BrokerStrategyType brokerStrategyType = BrokerStrategyType.DELIVERY_TO_SAME_CLASS_AS_MESSAGE;

    @Getter
    @Setter
    private ExceptionCatchingCondition exceptionCatchingCondition = allCatchingExceptionCondition();

    @Getter
    @Setter
    private MessageAcceptingStrategyType messageAcceptingStrategyType = DEFAULT_MESSAGE_ACCEPTING_STRATEGY_TYPE;

    public static MessageBusConfiguration defaultConfiguration() {
        return new MessageBusConfiguration();
    }
}
