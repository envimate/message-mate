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
import com.envimate.messageMate.channel.action.Consume;
import com.envimate.messageMate.messageBus.internal.brokering.MessageBusBrokerStrategy;
import lombok.RequiredArgsConstructor;

import java.util.Set;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class MessageBusConsumeAction {

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Consume<Object> messageBusConsumeAction(final MessageBusBrokerStrategy brokerStrategy) {
        return Consume.consumeMessage(objectProcessingContext -> {
            final Object message = objectProcessingContext.getPayload();
            final Class<?> messageClass = message.getClass();
            final Set<Channel<?>> channels = brokerStrategy.getDeliveringChannelsFor(messageClass);
            for (final Channel<?> channel : channels) {
                final ProcessingContext tProcessingContext = ProcessingContext.processingContext(message);
                channel.send(tProcessingContext);
            }
        });
    }
}
