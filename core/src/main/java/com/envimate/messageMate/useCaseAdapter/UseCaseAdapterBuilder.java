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

import com.envimate.messageMate.messageBus.EventType;
import com.envimate.messageMate.useCaseAdapter.building.*;
import com.envimate.messageMate.useCaseAdapter.mapping.RequestDeserializer;
import com.envimate.messageMate.useCaseAdapter.mapping.RequestMapper;
import com.envimate.messageMate.useCaseAdapter.mapping.filtermap.FilterMapBuilder;
import com.envimate.messageMate.useCaseAdapter.methodInvoking.ParameterValueMappings;
import com.envimate.messageMate.useCaseAdapter.usecaseInstantiating.UseCaseInstantiator;
import com.envimate.messageMate.useCaseAdapter.usecaseInvoking.Caller;
import com.envimate.messageMate.useCaseAdapter.usecaseInvoking.UseCaseCallingInformation;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;

import static com.envimate.messageMate.useCaseAdapter.UseCaseAdapterImpl.useCaseAdapterImpl;
import static com.envimate.messageMate.useCaseAdapter.mapping.filtermap.FilterMapBuilder.filterMapBuilder;
import static com.envimate.messageMate.useCaseAdapter.methodInvoking.ParameterValueMappings.emptyParameterValueMappings;
import static com.envimate.messageMate.useCaseAdapter.usecaseInvoking.UseCaseCallingInformation.useCaseInvocationInformation;
import static lombok.AccessLevel.PRIVATE;

public class UseCaseAdapterBuilder implements UseCaseAdapterStep1Builder {
    private final List<UseCaseCallingInformation> useCaseCallingInformations = new LinkedList<>();

    public static UseCaseAdapterStep1Builder anUseCaseAdapter() {
        return new UseCaseAdapterBuilder();
    }

    //TODO: registerUseCase + alles andere optional

    @Override
    public <USECASE> UseCaseAdapterStep2Builder<USECASE> invokingUseCase(final Class<USECASE> useCaseClass) {
        return eventType -> new MappingBuilder<>(UseCaseAdapterBuilder.this, useCaseClass, eventType);
    }

    @Override
    public UseCaseAdapter obtainingUseCaseInstancesUsing(final UseCaseInstantiator useCaseInstantiator) {
        return useCaseAdapterImpl(useCaseCallingInformations, useCaseInstantiator);
    }

    @RequiredArgsConstructor(access = PRIVATE)
    private final class MappingBuilder<USECASE> implements UseCaseAdapterStep3Builder<USECASE>, UseCaseAdapterCallingBuilder<USECASE> {
        private final FilterMapBuilder<Class<?>, Map<String, Object>, RequestMapper<?>> requestMappers = filterMapBuilder();
        private final UseCaseAdapterBuilder wrappingBuilder;
        private final Class<USECASE> useCaseClass;
        private final EventType eventType;

        @Override
        public <X> Using<DeserializationStage<UseCaseAdapterCallingBuilder<USECASE>>, RequestMapper<X>> mappingRequestsToUseCaseParametersThat(final BiPredicate<Class<?>, Map<String, Object>> filter) {
            return mapper -> {
                requestMappers.put(filter, mapper);
                return this;
            };
        }

        @Override
        public UseCaseAdapterCallingBuilder<USECASE> mappingRequestsToUseCaseParametersByDefaultUsing(final RequestMapper<Object> mapper) {
            requestMappers.setDefaultValue(mapper);
            return this;
        }

        @Override
        public UseCaseAdapterStep1Builder callingBy(Caller<USECASE> caller) {
            final RequestDeserializer requestDeserializer = RequestDeserializer.requestDeserializer(requestMappers.build());
            final UseCaseCallingInformation<USECASE> invocationInformation = useCaseInvocationInformation(useCaseClass, eventType, caller, requestDeserializer);
            wrappingBuilder.useCaseCallingInformations.add(invocationInformation);
            return wrappingBuilder;
        }
    }
}
