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

import com.envimate.messageMate.soonToBeExternal.building.EventToUseCaseDispatcherStep1Builder;
import com.envimate.messageMate.soonToBeExternal.building.EventToUseCaseDispatcherStep2Builder;
import com.envimate.messageMate.soonToBeExternal.building.EventToUseCaseDispatcherStep3Builder;
import com.envimate.messageMate.soonToBeExternal.building.EventToUseCaseDispatcherStepCallingBuilder;
import com.envimate.messageMate.soonToBeExternal.neww.UseCaseAdapter;
import com.envimate.messageMate.soonToBeExternal.neww.UseCaseInstantiator;
import com.envimate.messageMate.useCaseAdapter.EventToUseCaseMapping;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;

import static com.envimate.messageMate.useCaseAdapter.EventToUseCaseMapping.eventToUseCaseMapping;
import static com.envimate.messageMate.soonToBeExternal.neww.UseCaseAdapterImpl.useCaseAdapterImpl;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public class EventToUseCaseDispatcherBuilder implements EventToUseCaseDispatcherStep1Builder,
        EventToUseCaseDispatcherStep3Builder<UseCaseAdapter> {
    private final List<EventToUseCaseMapping> eventToUseCaseMappings = new LinkedList<>();

    public static EventToUseCaseDispatcherStep1Builder anEventToUseCaseDispatcher() {
        return new EventToUseCaseDispatcherBuilder();
    }

    @Override
    public <USECASE> EventToUseCaseDispatcherStep2Builder<USECASE> invokingUseCase(final Class<USECASE> useCaseClass) {
        return new EventToUseCaseDispatcherStep2Builder<USECASE>() {
            @Override
            public <EVENT> EventToUseCaseDispatcherStepCallingBuilder<USECASE, EVENT> forEvent(Class<EVENT> eventClass) {
                return caller -> {
                    eventToUseCaseMappings.add(eventToUseCaseMapping(useCaseClass, eventClass, caller));
                    return EventToUseCaseDispatcherBuilder.this;
                };
            }
        };
    }

    @Override
    public UseCaseAdapter obtainingUseCaseInstancesUsing(final UseCaseInstantiator useCaseInstantiator) {
        return useCaseAdapterImpl(eventToUseCaseMappings, useCaseInstantiator);
    }
}
