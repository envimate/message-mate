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

package com.envimate.messageMate.useCases.useCaseAdapter.usecaseCalling;

import com.envimate.messageMate.mapping.Deserializer;
import com.envimate.messageMate.mapping.Serializer;
import com.envimate.messageMate.useCases.useCaseAdapter.parameterInjecting.ParameterInjector;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class CallingContext {
    @Getter
    private final Serializer serializer;
    @Getter
    private final Deserializer deserializer;
    @Getter
    private final ParameterInjector parameterInjector;

    public static CallingContext callingContext(final Serializer serializer,
                                                final Deserializer deserializer,
                                                final ParameterInjector parameterInjector) {
        return new CallingContext(serializer, deserializer, parameterInjector);
    }
}
