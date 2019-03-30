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

import com.envimate.messageMate.identification.CorrelationId;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class UseCaseCallResponse {
    @Getter
    private final Object returnValue;
    @Getter
    private final UseCaseCallRequest request;
    @Getter
    private final Exception exception;

    public static UseCaseCallResponse useCaseCallResponseForSuccess(final Object returnValue, final UseCaseCallRequest request) {
        return new UseCaseCallResponse(returnValue, request, null);
    }

    public static UseCaseCallResponse useCaseCallResponseForException(final UseCaseCallRequest request,
                                                                      final Exception exception) {
        return new UseCaseCallResponse(null, request, exception);
    }

    public CorrelationId getCorrelationId() {
        return request.getCorrelationId();
    }
}
