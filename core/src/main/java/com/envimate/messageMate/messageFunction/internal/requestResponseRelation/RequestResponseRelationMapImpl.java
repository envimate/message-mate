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

package com.envimate.messageMate.messageFunction.internal.requestResponseRelation;

import com.envimate.messageMate.correlation.CorrelationId;
import com.envimate.messageMate.messageFunction.correlationIdExtracting.CorrelationIdExtraction;
import com.envimate.messageMate.messageFunction.correlationIdExtracting.CorrelationIdExtractor;
import com.envimate.messageMate.messageFunction.internal.responseMatching.ResponseMatcher;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.function.BiFunction;

import static com.envimate.messageMate.messageFunction.internal.responseMatching.ResponseMatcherImpl.responseMatcher;
import static java.util.Collections.emptyList;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
final class RequestResponseRelationMapImpl<R, S> implements RequestResponseRelationMap<R, S> {
    private final Map<Class<R>, List<Class<S>>> successResponsesMap = new HashMap<>();
    private final Map<Class<R>, List<Class<S>>> errorResponsesMap = new HashMap<>();
    private final List<GeneralErrorResponse<?>> generalErrorResponses = new LinkedList<>();
    private final CorrelationIdExtractor<R> requestCorrelationIdExtractor;
    private final CorrelationIdExtractor<S> responseCorrelationIdExtractor;

    static <R, S> RequestResponseRelationMap<R, S> requestResponseRelationMap(
            @NonNull final CorrelationIdExtractor<R> requestCorrelationIdExtractor,

            @NonNull final CorrelationIdExtractor<S> responseCorrelationIdExtractor) {
        return new RequestResponseRelationMapImpl<>(requestCorrelationIdExtractor, responseCorrelationIdExtractor);
    }

    @Override
    public <T extends R> List<ResponseMatcher> responseMatchers(final T request) {
        @SuppressWarnings("unchecked")
        final Class<T> requestClass = (Class<T>) request.getClass();
        final CorrelationId requestCorrelationId = requestCorrelationIdExtractor.extract(request);
        final List<ResponseMatcher> responseMatchers = new LinkedList<>();
        collectSuccessResponseMatchers(requestClass, requestCorrelationId, responseMatchers);
        collectErrorResponseMatchers(requestClass, requestCorrelationId, responseMatchers);
        return responseMatchers;
    }

    private <T extends R> void collectSuccessResponseMatchers(final Class<T> requestClass,
                                                              final CorrelationId requestCorrelationId,
                                                              final List<ResponseMatcher> responseMatchers) {
        final List<Class<S>> successResponses = successResponsesMap.getOrDefault(requestClass, emptyList());
        successResponses.stream()
                .map(c -> {
                    final CorrelationIdExtraction<S> correlationIdExtraction = responseCorrelationIdExtractor.extractionFor(c);
                    return responseMatcher(c, requestCorrelationId, correlationIdExtraction, true);
                })
                .forEach(responseMatchers::add);
    }

    private <T extends R> void collectErrorResponseMatchers(final Class<T> requestClass,
                                                            final CorrelationId requestCorrelationId,
                                                            final List<ResponseMatcher> responseMatchers) {
        final List<Class<S>> errorResponses = errorResponsesMap.getOrDefault(requestClass, emptyList());
        errorResponses.stream()
                .map(c -> {
                    final CorrelationIdExtraction<S> correlationIdExtraction = responseCorrelationIdExtractor.extractionFor(c);
                    return responseMatcher(c, requestCorrelationId, correlationIdExtraction, false);
                })
                .forEach(responseMatchers::add);
        generalErrorResponses.stream()
                .filter(generalErrorResponse -> !errorResponses.contains(generalErrorResponse.responseClass))
                .map(g -> responseMatcher(g.createMatchingFunction(), false))
                .forEach(responseMatchers::add);
    }

    @Override
    public void addSuccessResponse(final Class<R> requestClass, final Class<S> responseClass) {
        final List<Class<S>> responseClasses = successResponsesMap.getOrDefault(requestClass, new LinkedList<>());
        responseClasses.add(responseClass);
        successResponsesMap.put(requestClass, responseClasses);
    }

    @Override
    public void addErrorResponse(final Class<R> requestClass, final Class<S> responseClass) {
        final List<Class<S>> responseClasses = errorResponsesMap.getOrDefault(requestClass, new LinkedList<>());
        responseClasses.add(responseClass);
        errorResponsesMap.put(requestClass, responseClasses);
    }

    @Override
    public void addGeneralErrorResponse(final Class<?> responseClass) {
        addGeneralErrorResponse(responseClass, (o, r) -> true);
    }

    @Override
    public <T> void addGeneralErrorResponse(final Class<T> responseClass, final BiFunction<T, R, Boolean> conditional) {
        final GeneralErrorResponse<?> generalErrorResponse = new GeneralErrorResponse<>(responseClass, conditional);
        generalErrorResponses.add(generalErrorResponse);
    }

    @Override
    public Set<Class<?>> getAllPossibleResponseClasses() {
        final Set<Class<?>> allPossibleResponseClasses = new HashSet<>();
        successResponsesMap.values().forEach(allPossibleResponseClasses::addAll);
        errorResponsesMap.values().forEach(allPossibleResponseClasses::addAll);
        generalErrorResponses.stream()
                .map(GeneralErrorResponse::getResponseClass)
                .forEach(allPossibleResponseClasses::add);
        return allPossibleResponseClasses;
    }

    @RequiredArgsConstructor(access = PRIVATE)
    private final class GeneralErrorResponse<T> {
        @Getter
        private final Class<T> responseClass;
        private final BiFunction<T, R, Boolean> conditional;

        public BiFunction<Object, Object, Boolean> createMatchingFunction() {
            final BiFunction<Object, Object, Boolean> matchFunction = (request, response) -> {
                if (response.getClass().equals(responseClass)) {
                    @SuppressWarnings("unchecked")
                    final T castedResponse = (T) response;
                    @SuppressWarnings("unchecked")
                    final R castedRequest = (R) request;
                    return conditional.apply(castedResponse, castedRequest);
                }
                return false;
            };
            return matchFunction;
        }
    }
}
