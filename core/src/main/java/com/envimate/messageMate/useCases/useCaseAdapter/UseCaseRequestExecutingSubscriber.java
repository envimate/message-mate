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

package com.envimate.messageMate.useCases.useCaseAdapter;

import com.envimate.messageMate.identification.CorrelationId;
import com.envimate.messageMate.mapping.Deserializer;
import com.envimate.messageMate.mapping.ExceptionSerializer;
import com.envimate.messageMate.mapping.Serializer;
import com.envimate.messageMate.processingContext.EventType;
import com.envimate.messageMate.processingContext.ProcessingContext;
import com.envimate.messageMate.serializedMessageBus.SerializedMessageBus;
import com.envimate.messageMate.subscribing.AcceptingBehavior;
import com.envimate.messageMate.subscribing.Subscriber;
import com.envimate.messageMate.subscribing.SubscriptionId;
import com.envimate.messageMate.useCases.useCaseAdapter.usecaseCalling.Caller;
import com.envimate.messageMate.useCases.useCaseAdapter.usecaseInstantiating.UseCaseInstantiator;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Map;

import static com.envimate.messageMate.internal.enforcing.NotNullEnforcer.ensureNotNull;
import static com.envimate.messageMate.subscribing.AcceptingBehavior.MESSAGE_ACCEPTED;
import static com.envimate.messageMate.subscribing.SubscriptionId.newUniqueId;
import static com.envimate.messageMate.useCases.useCaseAdapter.UseCaseInvokingResponseEventType.USE_CASE_RESPONSE_EVENT_TYPE;
import static lombok.AccessLevel.PRIVATE;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = PRIVATE)
final class UseCaseRequestExecutingSubscriber implements Subscriber<ProcessingContext<Map<String, Object>>> {
    private final UseCaseCallingInformation<?> useCaseCallingInformation;
    private final UseCaseInstantiator useCaseInstantiator;
    private final Deserializer requestDeserializer;
    private final Serializer responseSerializer;
    private final ExceptionSerializer exceptionSerializer;
    private final SubscriptionId subscriptionId = newUniqueId();
    private SerializedMessageBus serializedMessageBus;

    static UseCaseRequestExecutingSubscriber useCaseRequestExecutingSubscriber(
            final UseCaseCallingInformation<?> useCaseCallingInformation,
            final UseCaseInstantiator useCaseInstantiator,
            final Deserializer requestDeserializer,
            final Serializer responseSerializer,
            final ExceptionSerializer exceptionSerializer) {
        ensureNotNull(useCaseCallingInformation, "useCaseCallingInformation");
        ensureNotNull(useCaseInstantiator, "useCaseInstantiator");
        ensureNotNull(requestDeserializer, "requestDeserializer");
        ensureNotNull(responseSerializer, "responseSerializer");
        ensureNotNull(exceptionSerializer, "exceptionSerializer");
        return new UseCaseRequestExecutingSubscriber(useCaseCallingInformation, useCaseInstantiator,
                requestDeserializer, responseSerializer, exceptionSerializer);
    }

    @Override
    public AcceptingBehavior accept(final ProcessingContext<Map<String, Object>> processingContext) {
        @SuppressWarnings("unchecked")
        final Caller<Object> caller = (Caller<Object>) useCaseCallingInformation.getCaller();
        final Class<?> useCaseClass = useCaseCallingInformation.getUseCaseClass();
        final Object useCase = useCaseInstantiator.instantiate(useCaseClass);
        final Map<String, Object> payload = processingContext.getPayload();
        Map<String, Object> serializedReturnValue = null;
        Map<String, Object> serializedException = null;
        try {
            serializedReturnValue = caller.call(useCase, payload, requestDeserializer, responseSerializer);
        } catch (final Exception e) {
            serializedException = exceptionSerializer.serializeException(e);
        }
        final CorrelationId correlationId = processingContext.generateCorrelationIdForAnswer();
        serializedMessageBus.send(USE_CASE_RESPONSE_EVENT_TYPE, serializedReturnValue, serializedException, correlationId);
        return MESSAGE_ACCEPTED;
    }

    @Override
    public SubscriptionId getSubscriptionId() {
        return subscriptionId;
    }

    public void attachTo(final SerializedMessageBus serializedMessageBus) {
        this.serializedMessageBus = serializedMessageBus;
        final EventType eventType = useCaseCallingInformation.getEventType();
        serializedMessageBus.subscribeRaw(eventType, this);
    }
}
