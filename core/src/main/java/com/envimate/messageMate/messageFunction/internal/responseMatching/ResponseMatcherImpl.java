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

package com.envimate.messageMate.messageFunction.internal.responseMatching;

import com.envimate.messageMate.correlation.CorrelationId;
import com.envimate.messageMate.messageFunction.correlationIdExtracting.CorrelationIdExtraction;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.function.BiFunction;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResponseMatcherImpl implements ResponseMatcher {
    private final BiFunction<Object, Object, Boolean> matchFunction;
    private final boolean isSuccessResponse;

    public static <S> ResponseMatcherImpl responseMatcher(final Class<S> expectedResponseClass,
                                                          final CorrelationId expectedCorrelationId,
                                                          final CorrelationIdExtraction<S> correlationIdExtraction,
                                                          final boolean isSuccessResponse) {
        final BiFunction<Object, Object, Boolean> matchFunction = (request, response) -> {
            if (response.getClass().equals(expectedResponseClass)) {
                @SuppressWarnings("unchecked")
                final S castedResponse = (S) response;
                final CorrelationId correlationId = correlationIdExtraction.extractCorrelationId(castedResponse);
                return correlationId.equals(expectedCorrelationId);
            }
            return false;
        };
        return responseMatcher(matchFunction, isSuccessResponse);
    }

    public static ResponseMatcherImpl responseMatcher(final BiFunction<Object, Object, Boolean> matchFunction,
                                                      final boolean isSuccessResponse) {
        return new ResponseMatcherImpl(matchFunction, isSuccessResponse);
    }

    @Override
    public boolean matches(final Object request, final Object response) {
        return matchFunction.apply(request, response);
    }

    @Override
    public boolean wasSuccessResponse(final Object response) {
        return isSuccessResponse;
    }
}
