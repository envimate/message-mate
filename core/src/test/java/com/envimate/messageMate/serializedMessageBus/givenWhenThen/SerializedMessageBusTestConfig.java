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

package com.envimate.messageMate.serializedMessageBus.givenWhenThen;

import com.envimate.messageMate.internal.pipe.configuration.AsynchronousConfiguration;
import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.messageBus.givenWhenThen.MessageBusTestExceptionHandler;
import lombok.RequiredArgsConstructor;

import static com.envimate.messageMate.internal.pipe.configuration.AsynchronousConfiguration.constantPoolSizeAsynchronousPipeConfiguration;
import static com.envimate.messageMate.messageBus.MessageBusBuilder.aMessageBus;
import static com.envimate.messageMate.messageBus.MessageBusType.ASYNCHRONOUS;
import static com.envimate.messageMate.messageBus.MessageBusType.SYNCHRONOUS;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class SerializedMessageBusTestConfig {
    private final MessageBus messageBus;

    public static SerializedMessageBusTestConfig synchronousMessageBusTestConfig() {
        final MessageBus messageBus = aMessageBus().forType(SYNCHRONOUS)
                .build();
        return new SerializedMessageBusTestConfig(messageBus);
    }

    public static SerializedMessageBusTestConfig asynchronousMessageBusTestConfig() {
        final int poolSize = 3;
        final AsynchronousConfiguration asynchronousConfiguration = constantPoolSizeAsynchronousPipeConfiguration(poolSize);
        final MessageBus messageBus = aMessageBus().forType(ASYNCHRONOUS)
                .withAsynchronousConfiguration(asynchronousConfiguration)
                .withExceptionHandler(MessageBusTestExceptionHandler.allExceptionIgnoringExceptionHandler())
                .build();
        return new SerializedMessageBusTestConfig(messageBus);
    }

    public MessageBus getMessageBus() {
        return messageBus;
    }
}
