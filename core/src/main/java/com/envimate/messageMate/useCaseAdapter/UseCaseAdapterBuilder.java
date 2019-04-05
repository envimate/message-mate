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
import com.envimate.messageMate.useCaseAdapter.building.UseCaseAdapterStep1Builder;
import com.envimate.messageMate.useCaseAdapter.building.UseCaseAdapterStep2Builder;
import com.envimate.messageMate.useCaseAdapter.building.UseCaseAdapterStep3Builder;
import com.envimate.messageMate.useCaseAdapter.methodInvoking.ParameterValueMappings;
import com.envimate.messageMate.useCaseAdapter.usecaseInstantiating.UseCaseInstantiator;
import com.envimate.messageMate.useCaseAdapter.usecaseInvoking.Caller;
import com.envimate.messageMate.useCaseAdapter.usecaseInvoking.UseCaseCallingInformation;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import static com.envimate.messageMate.useCaseAdapter.UseCaseAdapterImpl.useCaseAdapterImpl;
import static com.envimate.messageMate.useCaseAdapter.methodInvoking.ParameterValueMappings.emptyParameterValueMappings;
import static com.envimate.messageMate.useCaseAdapter.usecaseInvoking.UseCaseCallingInformation.useCaseInvocationInformation;
import static lombok.AccessLevel.PRIVATE;

@SuppressWarnings("rawtypes") //TODO: remove
public class UseCaseAdapterBuilder implements UseCaseAdapterStep1Builder {
    private final List<UseCaseCallingInformation> useCaseCallingInformations = new LinkedList<>();

    public static UseCaseAdapterStep1Builder anUseCaseAdapter() {
        return new UseCaseAdapterBuilder();
    }

    //TODO: registerUseCase + alles andere optional

    @Override
    public <U> UseCaseAdapterStep2Builder<U> invokingUseCase(final Class<U> useCaseClass) {
        return new UseCaseAdapterStep2Builder<U>() {

            @Override
            public UseCaseAdapterStep3Builder<U> forType(final EventType eventType) {
                return new MappingBuilder<>(UseCaseAdapterBuilder.this, useCaseClass, eventType);
            }

        };
    }

    @Override
    public UseCaseAdapter obtainingUseCaseInstancesUsing(final UseCaseInstantiator useCaseInstantiator) {
        return useCaseAdapterImpl(useCaseCallingInformations, useCaseInstantiator);
    }

    @RequiredArgsConstructor(access = PRIVATE)
    private final class MappingBuilder<U> implements UseCaseAdapterStep3Builder<U> {
        private final ParameterValueMappings parameterValueMappings = emptyParameterValueMappings();
        private final UseCaseAdapterBuilder wrappingBuilder;
        private final Class<U> useCaseClass;
        private final EventType eventType;

        @Override
        public <P> UseCaseAdapterStep3Builder<U> mappingEventToParameter(final Class<P> paramClass,
                                                                         final Function<Object, Object> mapping) {
            parameterValueMappings.registerMapping(paramClass, mapping::apply);
            return this;
        }

        @Override
        public UseCaseAdapterStep1Builder callingBy(final Caller<U> caller) {
            final UseCaseCallingInformation<U> invocationInformation =
                    useCaseInvocationInformation(useCaseClass, eventType, caller, parameterValueMappings);
            wrappingBuilder.useCaseCallingInformations.add(invocationInformation);
            return wrappingBuilder;
        }
    }
}
