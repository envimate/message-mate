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

import com.envimate.messageMate.useCaseAdapter.building.UseCaseAdapterDeserializationStep1Builder;
import com.envimate.messageMate.useCaseAdapter.building.UseCaseAdapterDeserializationStep2Builder;
import com.envimate.messageMate.useCaseAdapter.building.UseCaseAdapterStep1Builder;
import com.envimate.messageMate.useCaseAdapter.building.UseCaseAdapterStep2Builder;
import com.envimate.messageMate.useCaseAdapter.mapping.RequestDeserializer;
import com.envimate.messageMate.useCaseAdapter.mapping.RequestMapper;
import com.envimate.messageMate.useCaseAdapter.mapping.filtermap.FilterMap;
import com.envimate.messageMate.useCaseAdapter.mapping.filtermap.FilterMapBuilder;
import com.envimate.messageMate.useCaseAdapter.usecaseInstantiating.UseCaseInstantiator;
import com.envimate.messageMate.useCaseAdapter.usecaseInvoking.UseCaseCallingInformation;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

import static com.envimate.messageMate.useCaseAdapter.UseCaseAdapterImpl.useCaseAdapterImpl;
import static com.envimate.messageMate.useCaseAdapter.mapping.filtermap.FilterMapBuilder.filterMapBuilder;
import static com.envimate.messageMate.useCaseAdapter.usecaseInvoking.UseCaseCallingInformation.useCaseInvocationInformation;

public class UseCaseAdapterBuilder implements UseCaseAdapterStep1Builder, UseCaseAdapterDeserializationStep1Builder {
    private final List<UseCaseCallingInformation> useCaseCallingInformationList = new LinkedList<>();
    private final FilterMapBuilder<Class<?>, Map<String, Object>, RequestMapper<?>> filterMapBuilder = filterMapBuilder();
    private UseCaseInstantiator useCaseInstantiator;

    public static UseCaseAdapterStep1Builder anUseCaseAdapter() {
        return new UseCaseAdapterBuilder();
    }

    //TODO: registerUseCase + alles andere optional

    @Override
    public <USECASE> UseCaseAdapterStep2Builder<USECASE> invokingUseCase(final Class<USECASE> useCaseClass) {
        return eventType -> caller -> {
            final UseCaseCallingInformation<USECASE> invocationInformation = useCaseInvocationInformation(useCaseClass, eventType, caller);
            useCaseCallingInformationList.add(invocationInformation);
            return this;
        };
    }

    @Override
    public UseCaseAdapterDeserializationStep1Builder obtainingUseCaseInstancesUsing(final UseCaseInstantiator useCaseInstantiator) {
        this.useCaseInstantiator = useCaseInstantiator;
        return this;
    }

    @Override
    public <T> UseCaseAdapterDeserializationStep2Builder<T> mappingRequestsToUseCaseParametersThat(BiPredicate<Class<?>, Map<String, Object>> filter) {
        return requestMapper -> {
            filterMapBuilder.put(filter, requestMapper);
            return this;
        };
    }

    @Override
    public UseCaseAdapter mappingRequestsToUseCaseParametersByDefaultUsing(final RequestMapper<Object> mapper) {
        filterMapBuilder.setDefaultValue(mapper);
        final FilterMap<Class<?>, Map<String, Object>, RequestMapper<?>> filterMap = filterMapBuilder.build();
        final RequestDeserializer requestDeserializer = RequestDeserializer.requestDeserializer(filterMap);
        return useCaseAdapterImpl(useCaseCallingInformationList, useCaseInstantiator, requestDeserializer);
    }

}
