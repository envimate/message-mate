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
import lombok.RequiredArgsConstructor;

import java.util.Map;

import static com.envimate.messageMate.internal.enforcing.NotNullEnforcer.ensureNotNull;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResponseSerializer {

    private static final Void CHANGEME = null; // TODO

    private final FilterMap<Object, Void, ResponseMapper<Object>> returnValueMappers;

    public static ResponseSerializer responseSerializer(
            final FilterMap<Object, Void, ResponseMapper<Object>> returnValueMappers) {
        ensureNotNull(returnValueMappers, "returnValueMappers");
        return new ResponseSerializer(returnValueMappers);
    }

    public Map<String, Object> serializeReturnValue(final Object returnValue) {
        final ResponseMapper<Object> mapper = returnValueMappers.get(returnValue, CHANGEME);
        return mapper.map(returnValue);
    }
}