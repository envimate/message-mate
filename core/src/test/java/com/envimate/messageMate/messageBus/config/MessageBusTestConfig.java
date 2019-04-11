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

package com.envimate.messageMate.messageBus.config;

import com.envimate.messageMate.messageBus.MessageBusType;
import com.envimate.messageMate.internal.pipe.configuration.AsynchronousConfiguration;
import lombok.*;

import static com.envimate.messageMate.messageBus.MessageBusType.ASYNCHRONOUS;
import static com.envimate.messageMate.messageBus.MessageBusType.SYNCHRONOUS;
import static com.envimate.messageMate.internal.pipe.configuration.AsynchronousConfiguration.constantPoolSizeAsynchronousPipeConfiguration;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MessageBusTestConfig {
    public static final int ASYNCHRONOUS_DELIVERY_POOL_SIZE = 3;
    @Getter
    private final MessageBusType type;
    @Getter
    private final AsynchronousConfiguration asynchronousConfiguration;
    @Getter
    private final long millisecondsSleepBetweenExecutionSteps;
    @Getter
    private final long millisecondsSleepAfterExecution;

    static MessageBusTestConfig aSynchronousMessageBus() {
        final int millisecondsSleepAfterExecution = 0;
        final int millisecondsSleepBetweenExecutionSteps = millisecondsSleepAfterExecution;
        return new MessageBusTestConfig(SYNCHRONOUS, null, millisecondsSleepBetweenExecutionSteps,
                millisecondsSleepAfterExecution);
    }

    static MessageBusTestConfig anAsynchronousMessageBus() {
        final int poolSize = ASYNCHRONOUS_DELIVERY_POOL_SIZE;
        final AsynchronousConfiguration asynchronousConfiguration = constantPoolSizeAsynchronousPipeConfiguration(poolSize);
        final int millisecondsSleepBetweenExecutionSteps = 5;
        final int millisecondsSleepAfterExecution = 10;
        return new MessageBusTestConfig(ASYNCHRONOUS, asynchronousConfiguration, millisecondsSleepBetweenExecutionSteps,
                millisecondsSleepAfterExecution);
    }

}
