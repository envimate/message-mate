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

package com.envimate.messageMate.useCaseAdapter.mapping;

import com.envimate.messageMate.useCaseAdapter.mapping.filtermap.FilterMap;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Map;

import static com.envimate.messageMate.internal.enforcing.NotNullEnforcer.ensureNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class RequestDeserializer { // TODO rename to EventDeserializer
    private final FilterMap<Class<?>, Map<String, Object>, RequestMapper<?>> requestMappers;

    public static RequestDeserializer requestDeserializer(
            final FilterMap<Class<?>, Map<String, Object>, RequestMapper<?>> requestMappers) {
        ensureNotNull(requestMappers, "requestMappers");
        return new RequestDeserializer(requestMappers);
    }

    @SuppressWarnings("unchecked")
    public <T> T deserializeRequest(final Class<T> type,
                                    final Map<String, Object> map) {
        final RequestMapper<T> mapper = (RequestMapper<T>) requestMappers.get(type, map);
        return mapper.map(type, map);
    }
}
