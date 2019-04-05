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

package com.envimate.messageMate.useCaseAdapter;

import com.envimate.messageMate.identification.CorrelationId;
import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.processingContext.ProcessingContext;
import com.envimate.messageMate.subscribing.AcceptingBehavior;
import com.envimate.messageMate.subscribing.Subscriber;
import com.envimate.messageMate.subscribing.SubscriptionId;
import com.envimate.messageMate.useCaseAdapter.mapping.RequestDeserializer;
import com.envimate.messageMate.useCaseAdapter.mapping.ResponseSerializer;
import com.envimate.messageMate.useCaseAdapter.usecaseInstantiating.UseCaseInstantiator;
import com.envimate.messageMate.useCaseAdapter.usecaseInvoking.Caller;
import com.envimate.messageMate.useCaseAdapter.usecaseInvoking.UseCaseCallingInformation;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static com.envimate.messageMate.internal.enforcing.NotNullEnforcer.ensureNotNull;
import static com.envimate.messageMate.subscribing.AcceptingBehavior.MESSAGE_ACCEPTED;
import static com.envimate.messageMate.subscribing.SubscriptionId.newUniqueId;
import static com.envimate.messageMate.useCaseAdapter.UseCaseInvokingResponseEventType.USE_CASE_RESPONSE_EVENT_TYPE;
import static lombok.AccessLevel.PRIVATE;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = PRIVATE)
@SuppressWarnings("rawtypes") //TODO: remove
final class UseCaseRequestExecutingSubscriber implements Subscriber<ProcessingContext<Object>> {
    private final MessageBus messageBus;
    private final UseCaseCallingInformation useCaseCallingInformation;
    private final UseCaseInstantiator useCaseInstantiator;
    private final RequestDeserializer requestDeserializer;
    private final ResponseSerializer responseSerializer;
    private final SubscriptionId subscriptionId = newUniqueId();

    public static UseCaseRequestExecutingSubscriber useCaseRequestExecutingSubscriber(
            final MessageBus messageBus,
            final UseCaseCallingInformation useCaseCallingInformation,
            final UseCaseInstantiator useCaseInstantiator,
            final RequestDeserializer requestDeserializer,
            final ResponseSerializer responseSerializer) {
        ensureNotNull(messageBus, "messageBus");
        ensureNotNull(useCaseCallingInformation, "useCaseCallingInformation");
        ensureNotNull(useCaseInstantiator, "useCaseInstantiator");
        ensureNotNull(requestDeserializer, "requestDeserializer");
        ensureNotNull(responseSerializer, "responseSerializer");
        return new UseCaseRequestExecutingSubscriber(messageBus, useCaseCallingInformation, useCaseInstantiator, requestDeserializer, responseSerializer);
    }

    @Override
    public AcceptingBehavior accept(final ProcessingContext<Object> processingContext) {
        final Caller caller = useCaseCallingInformation.getCaller();
        final Class<?> useCaseClass = useCaseCallingInformation.getUseCaseClass();
        final Object useCase = useCaseInstantiator.instantiate(useCaseClass);
        final Object event = processingContext.getPayload();
        @SuppressWarnings("unchecked")
        final Object returnValue = caller.call(useCase, event, requestDeserializer, responseSerializer);
        final CorrelationId correlationId = processingContext.generateCorrelationIdForAnswer();
        messageBus.send(USE_CASE_RESPONSE_EVENT_TYPE, returnValue, correlationId);
        return MESSAGE_ACCEPTED;
    }

    @Override
    public SubscriptionId getSubscriptionId() {
        return subscriptionId;
    }
}
