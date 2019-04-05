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

package com.envimate.messageMate.useCaseAdapter.usecaseInvoking;

import com.envimate.messageMate.messageBus.EventType;
import com.envimate.messageMate.useCaseAdapter.mapping.RequestDeserializer;
import com.envimate.messageMate.useCaseAdapter.methodInvoking.ParameterValueMappings;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static com.envimate.messageMate.internal.enforcing.NotNullEnforcer.ensureNotNull;
import static lombok.AccessLevel.PRIVATE;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = PRIVATE)
public final class UseCaseCallingInformation<USECASE> {
    @Getter
    private final Class<USECASE> useCaseClass;
    @Getter
    private final EventType eventType;
    @Getter
    private final Caller<USECASE> caller;
    @Getter
    private final RequestDeserializer requestDeserializer;

    public static <USECASE> UseCaseCallingInformation<USECASE> useCaseInvocationInformation(
            final Class<USECASE> useCaseClass,
            final EventType eventType,
            final Caller<USECASE> caller,
            final RequestDeserializer requestDeserializer) {
        ensureNotNull(useCaseClass, "useCaseClass");
        ensureNotNull(eventType, "eventType");
        ensureNotNull(caller, "caller");
        ensureNotNull(requestDeserializer, "requestDeserializer");
        return new UseCaseCallingInformation<>(useCaseClass, eventType, caller, requestDeserializer);
    }
}
