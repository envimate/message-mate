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

import com.envimate.messageMate.internal.collections.filtermap.FilterMapBuilder;
import com.envimate.messageMate.mapping.*;
import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.serializedMessageBus.SerializedMessageBus;
import com.envimate.messageMate.useCases.building.*;
import com.envimate.messageMate.useCases.useCaseAdapter.usecaseCalling.Caller;
import com.envimate.messageMate.useCases.useCaseAdapter.usecaseCalling.SinglePublicUseCaseMethodCaller;
import com.envimate.messageMate.useCases.useCaseAdapter.usecaseCalling.UseCaseCallingInformation;
import com.envimate.messageMate.useCases.useCaseAdapter.usecaseInstantiating.UseCaseInstantiator;
import com.envimate.messageMate.useCases.useCaseBus.UseCaseBus;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import static com.envimate.messageMate.internal.collections.filtermap.FilterMapBuilder.filterMapBuilder;
import static com.envimate.messageMate.mapping.Deserializer.requestDeserializer;
import static com.envimate.messageMate.mapping.Serializer.responseSerializer;
import static com.envimate.messageMate.useCases.useCaseAdapter.UseCaseAdapterImpl.useCaseAdapterImpl;
import static com.envimate.messageMate.useCases.useCaseAdapter.usecaseCalling.SinglePublicUseCaseMethodCaller.singlePublicUseCaseMethodCaller;
import static com.envimate.messageMate.useCases.useCaseAdapter.usecaseCalling.UseCaseCallingInformation.useCaseInvocationInformation;

public class UseCaseInvocationBuilder implements Step1Builder, DeserializationStep1Builder,
        ResponseSerializationStep1Builder, ExceptionSerializationStep1Builder, BuilderStepBuilder {
    private final List<UseCaseCallingInformation<?>> useCaseCallingInformationList = new LinkedList<>();
    private final FilterMapBuilder<Class<?>, Map<String, Object>, Demapifier<?>> deserializers = filterMapBuilder();
    private final FilterMapBuilder<Object, Void, Mapifier<Object>> responseSerializers = filterMapBuilder();
    private final FilterMapBuilder<Exception, Void, Mapifier<Exception>> exceptionSerializers = filterMapBuilder();
    private UseCaseInstantiator useCaseInstantiator;

    public static Step1Builder anUseCaseAdapter() {
        return new UseCaseInvocationBuilder();
    }

    @Override
    public <U> Step2Builder<U> invokingUseCase(final Class<U> useCaseClass) {
        return eventType -> new Step3Builder<U>() {
            @Override
            public Step1Builder callingTheSingleUseCaseMethod() {
                final SinglePublicUseCaseMethodCaller<U> caller = singlePublicUseCaseMethodCaller(useCaseClass);
                return callingBy(caller);
            }

            @Override
            public Step1Builder callingBy(final Caller<U> caller) {
                final UseCaseCallingInformation<U> invocationInformation = useCaseInvocationInformation(useCaseClass,
                        eventType, caller);
                useCaseCallingInformationList.add(invocationInformation);
                return UseCaseInvocationBuilder.this;
            }
        };
    }

    @Override
    public DeserializationStep1Builder obtainingUseCaseInstancesUsing(final UseCaseInstantiator useCaseInstantiator) {
        this.useCaseInstantiator = useCaseInstantiator;
        return this;
    }

    @Override
    public <T> DeserializationStep2Builder<T> mappingRequestsToUseCaseParametersThat(final BiPredicate<Class<?>,
            Map<String, Object>> filter) {
        return requestMapper -> {
            deserializers.put(filter, requestMapper);
            return this;
        };
    }

    @Override
    public ResponseSerializationStep1Builder mappingRequestsToUseCaseParametersByDefaultUsing(final Demapifier<Object> mapper) {
        deserializers.setDefaultValue(mapper);
        return this;
    }

    @Override
    public ResponseSerializationStep2Builder<Object> serializingResponseObjectsThat(final Predicate<Object> filter) {
        return mapper -> {
            final BiPredicate<Object, Void> biPredicate = (object, aVoid) -> filter.test(object); // TODO
            responseSerializers.put(biPredicate, mapper);
            return this;
        };
    }

    @Override
    public ExceptionSerializationStep1Builder serializingResponseObjectsByDefaultUsing(final Mapifier<Object> mapper) {
        responseSerializers.setDefaultValue(mapper);
        return this;
    }

    @Override
    public ExceptionSerializationStep2Builder<Exception> serializingExceptionsThat(final Predicate<Exception> filter) {
        return mapper -> {
            final BiPredicate<Exception, Void> biPredicate = (object, aVoid) -> filter.test(object); // TODO
            exceptionSerializers.put(biPredicate, mapper);
            return this;
        };
    }

    @Override
    public BuilderStepBuilder serializingExceptionsByDefaultUsing(final Mapifier<Exception> mapper) {
        exceptionSerializers.setDefaultValue(mapper);
        return this;
    }

    @Override
    public UseCaseBus build(final SerializedMessageBus serializedMessageBus) {
        final UseCaseAdapter useCaseAdapter = buildAsStandaloneAdapter();
        useCaseAdapter.attachTo(serializedMessageBus);
        return UseCaseBus.useCaseBus(serializedMessageBus);
    }

    @Override
    public UseCaseBus build(final MessageBus messageBus) {
        final UseCaseAdapter useCaseAdapter = buildAsStandaloneAdapter();
        final SerializedMessageBus serializedMessageBus = useCaseAdapter.attachAndEnhance(messageBus);
        return UseCaseBus.useCaseBus(serializedMessageBus);
    }

    @Override
    public UseCaseAdapter buildAsStandaloneAdapter() {
        final Deserializer requestDeserializer = requestDeserializer(deserializers.build());
        final Serializer responseSerializer = responseSerializer(responseSerializers.build());
        final ExceptionSerializer exceptionSerializer = ExceptionSerializer.exceptionSerializer(exceptionSerializers.build());
        return useCaseAdapterImpl(useCaseCallingInformationList, useCaseInstantiator, requestDeserializer, responseSerializer,
                exceptionSerializer);
    }
}
