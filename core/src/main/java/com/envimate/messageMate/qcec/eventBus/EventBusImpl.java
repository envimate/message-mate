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

package com.envimate.messageMate.qcec.eventBus;

import com.envimate.messageMate.processingContext.EventType;
import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.subscribing.SubscriptionId;
import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;

import static com.envimate.messageMate.processingContext.EventType.eventTypeFromClass;
import static com.envimate.messageMate.processingContext.EventType.eventTypeFromObjectClass;
import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
public class EventBusImpl implements EventBus {
    private final MessageBus messageBus;

    @Override
    public void publish(final Object event) {
        final EventType eventType = eventTypeFromObjectClass(event);
        messageBus.send(eventType, event);
    }

    @Override
    public <T> SubscriptionId reactTo(final Class<T> tClass, final Consumer<T> consumer) {
        final EventType eventType = eventTypeFromClass(tClass);
        return messageBus.subscribe(eventType, o -> {
            @SuppressWarnings("unchecked")
            final T t = (T) o;
            consumer.accept(t);
        });
    }

    @Override
    public void unsubscribe(final SubscriptionId subscriptionId) {
        messageBus.unsubcribe(subscriptionId);
    }
}
