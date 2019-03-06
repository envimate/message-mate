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

package com.envimate.messageMate.messageBus.config;

import com.envimate.messageMate.messageBus.MessageBusType;
import com.envimate.messageMate.pipe.configuration.AsynchronousConfiguration;
import lombok.*;

import static com.envimate.messageMate.messageBus.MessageBusType.ASYNCHRONOUS;
import static com.envimate.messageMate.messageBus.MessageBusType.SYNCHRONOUS;
import static com.envimate.messageMate.pipe.configuration.AsynchronousConfiguration.constantPoolSizeAsynchronousPipeConfiguration;

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
    private final long millisecondsSleepBetweenExecutionActionSteps;
    @Getter
    private final long millisecondsSleepAfterExecution;

    static MessageBusTestConfig aSynchronousMessageBus() {
        return new MessageBusTestConfig(SYNCHRONOUS, null, 0, 0);
    }

    static MessageBusTestConfig anAsynchronousMessageBus() {
        final AsynchronousConfiguration asynchronousConfiguration = constantPoolSizeAsynchronousPipeConfiguration(ASYNCHRONOUS_DELIVERY_POOL_SIZE);
        return new MessageBusTestConfig(ASYNCHRONOUS, asynchronousConfiguration, 5, 10);
    }

}
