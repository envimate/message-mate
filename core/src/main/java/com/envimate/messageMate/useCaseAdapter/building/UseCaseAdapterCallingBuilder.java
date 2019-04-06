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

package com.envimate.messageMate.useCaseAdapter.building;

import com.envimate.messageMate.useCaseAdapter.usecaseCalling.Caller;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import static java.util.Collections.emptyMap;

public interface UseCaseAdapterCallingBuilder<U> {

    default UseCaseAdapterStep1Builder calling(final BiFunction<U, Object, Map<String, Object>> caller) {
        return callingBy((useCase, event, requestDeserializer, responseSerializer) -> {
            final Map<String, Object> responseMap = caller.apply(useCase, event);
            return responseMap;
        });
    }

    default UseCaseAdapterStep1Builder callingVoid(final BiConsumer<U, Object> caller) {
        return callingBy((usecase, event, requestDeserializer, responseSerializer) -> {
            caller.accept(usecase, event);
            return emptyMap();
        });
    }

    UseCaseAdapterStep1Builder callingTheSingleUseCaseMethod();

    UseCaseAdapterStep1Builder callingBy(Caller<U> caller);
}
