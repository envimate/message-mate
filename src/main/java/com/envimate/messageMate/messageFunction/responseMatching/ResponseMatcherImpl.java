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

package com.envimate.messageMate.messageFunction.responseMatching;

import com.envimate.messageMate.correlation.CorrelationId;
import com.envimate.messageMate.messageFunction.correlationIdExtracting.CorrelationIdExtraction;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResponseMatcherImpl<S> implements ResponseMatcher<S> {
    private final Class<S> expectedResponseClass;
    private final CorrelationId expectedCorrelationId;
    private final CorrelationIdExtraction<S> correlationIdExtraction;
    private final boolean isSuccessResponse;

    public static <S> ResponseMatcherImpl<S> responseMatcher(@NonNull final Class<S> expectedResponseClass,
                                                             @NonNull final CorrelationId correlationId,
                                                             @NonNull final CorrelationIdExtraction<S> correlationIdExtraction,
                                                             @NonNull final boolean isSuccessResponse) {
        return new ResponseMatcherImpl<>(expectedResponseClass, correlationId, correlationIdExtraction, isSuccessResponse);
    }

    @Override
    public boolean matches(final S response) {
        if (response.getClass().equals(expectedResponseClass)) {
            final CorrelationId correlationId = correlationIdExtraction.extractCorrelationId(response);
            return correlationId.equals(expectedCorrelationId);
        }
        return false;
    }

    @Override
    public boolean wasSuccessResponse(final S response) {
        return isSuccessResponse;
    }
}
