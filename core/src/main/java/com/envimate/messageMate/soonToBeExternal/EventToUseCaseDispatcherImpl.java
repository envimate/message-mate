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

package com.envimate.messageMate.soonToBeExternal;

import com.envimate.messageMate.identification.CorrelationId;
import com.envimate.messageMate.messageFunction.MessageFunction;
import com.envimate.messageMate.messageFunction.ResponseFuture;
import com.envimate.messageMate.useCaseAdapter.Caller;
import com.envimate.messageMate.useCaseAdapter.EventToUseCaseMapping;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.function.Function;

import static com.envimate.messageMate.identification.CorrelationId.newUniqueCorrelationId;
import static com.envimate.messageMate.soonToBeExternal.UseCaseCallRequest.useCaseCallRequest;
import static com.envimate.messageMate.soonToBeExternal.UseCaseResponseFutureImpl.useCaseResponseFuture;
import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
public final class EventToUseCaseDispatcherImpl implements EventToUseCaseDispatcher {
    private final Function<Class, Object> instantiator;
    private final Map<Class<?>, EventToUseCaseMapping> eventToUseCaseMappings;
    private final MessageFunction messageFunction;

    @Override
    public EventFactory eventFactoryFor(final Class<?> eventType) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public UseCaseResponseFuture dispatch(final Object event) {
        final Class<?> eventClass = event.getClass();
        if (eventToUseCaseMappings.containsKey(eventClass)) {
            final EventToUseCaseMapping mapping = eventToUseCaseMappings.get(eventClass);
            final Caller caller = mapping.caller;
            final Object useCase = instantiator.apply(mapping.useCaseClass);
            final CorrelationId correlationId = newUniqueCorrelationId();
            final UseCaseCallRequest request = useCaseCallRequest(useCase, event, caller, correlationId);
            final ResponseFuture responseFuture = messageFunction.request(request);
            return useCaseResponseFuture(responseFuture);
        } else {
            throw new NoUseCaseKnownForEventException(event);
        }
    }
}
