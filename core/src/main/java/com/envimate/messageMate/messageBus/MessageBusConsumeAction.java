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
import com.envimate.messageMate.channel.action.Consume;
import com.envimate.messageMate.identification.CorrelationId;
import com.envimate.messageMate.messageBus.internal.brokering.MessageBusBrokerStrategy;
import com.envimate.messageMate.messageBus.internal.correlationIds.CorrelationBasedSubscriptions;
import com.envimate.messageMate.processingContext.ProcessingContext;
import com.envimate.messageMate.subscribing.Subscriber;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.envimate.messageMate.channel.action.Consume.consumeMessage;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class MessageBusConsumeAction {

    public static Consume<Object> messageBusConsumeAction(final MessageBusBrokerStrategy brokerStrategy,
                                                          final CorrelationBasedSubscriptions correlationBasedSubscriptions) {
        return consumeMessage(objectProcessingContext -> {
            deliveryToEventTypeBasedSubscriber(objectProcessingContext, brokerStrategy);
            deliveryBasedOnCorrelationId(objectProcessingContext, correlationBasedSubscriptions);
        });
    }

    private static void deliveryToEventTypeBasedSubscriber(final ProcessingContext<Object> processingContext,
                                                           final MessageBusBrokerStrategy brokerStrategy) {
        final EventType eventType = processingContext.getEventType();
        final Channel<Object> channel = brokerStrategy.getDeliveringChannelFor(eventType);
        channel.send(processingContext);
    }

    private static void deliveryBasedOnCorrelationId(final ProcessingContext<Object> objectProcessingContext,
                                                     final CorrelationBasedSubscriptions correlationBasedSubscriptions) {
        final CorrelationId correlationId = objectProcessingContext.getCorrelationId();
        final List<Subscriber<ProcessingContext<Object>>> corIdSubscribers = correlationBasedSubscriptions.getSubscribersFor(correlationId);
        for (final Subscriber<ProcessingContext<Object>> correlationSubscriber : corIdSubscribers) {
            correlationSubscriber.accept(objectProcessingContext);
        }
    }
}
