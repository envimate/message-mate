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

package com.envimate.messageMate.messageFunction.requestResponseRelation;

import com.envimate.messageMate.correlation.CorrelationId;
import com.envimate.messageMate.messageFunction.correlationIdExtracting.CorrelationIdExtraction;
import com.envimate.messageMate.messageFunction.correlationIdExtracting.CorrelationIdExtractor;
import com.envimate.messageMate.messageFunction.responseMatching.ResponseMatcher;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.*;

import static com.envimate.messageMate.messageFunction.responseMatching.ResponseMatcherImpl.responseMatcher;
import static java.util.Collections.emptyList;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class RequestResponseRelationMapImpl<R, S> implements RequestResponseRelationMap<R, S> {
    private final Map<Class<R>, List<Class<S>>> successResponsesMap = new HashMap<>();
    private final Map<Class<R>, List<Class<S>>> errorResponsesMap = new HashMap<>();
    private final List<Class<S>> generalErrorResponses = new LinkedList<>();
    private final CorrelationIdExtractor<R> requestCorrelationIdExtractor;
    private final CorrelationIdExtractor<S> responseCorrelationIdExtractor;

    static <R, S> RequestResponseRelationMap<R, S> requestResponseRelationMap(
            @NonNull final CorrelationIdExtractor<R> requestCorrelationIdExtractor,

            @NonNull final CorrelationIdExtractor<S> responseCorrelationIdExtractor) {
        return new RequestResponseRelationMapImpl<>(requestCorrelationIdExtractor, responseCorrelationIdExtractor);
    }

    @Override
    public <T extends R> List<ResponseMatcher<S>> responseMatchers(final T request) {
        @SuppressWarnings("unchecked")
        final Class<T> requestClass = (Class<T>) request.getClass();
        final CorrelationId requestCorrelationId = requestCorrelationIdExtractor.extract(request);
        final List<ResponseMatcher<S>> responseMatchers = new LinkedList<>();
        collectSuccessResponseMatchers(requestClass, requestCorrelationId, responseMatchers);
        collectErrorResponseMatchers(requestClass, requestCorrelationId, responseMatchers);
        return responseMatchers;
    }

    private <T extends R> void collectSuccessResponseMatchers(final Class<T> requestClass,
                                                              final CorrelationId requestCorrelationId,
                                                              final List<ResponseMatcher<S>> responseMatchers) {
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
                                                            final List<ResponseMatcher<S>> responseMatchers) {
        final Set<Class<S>> redundantFreeErrorResponses = new HashSet<>();
        final List<Class<S>> errorResponses = errorResponsesMap.getOrDefault(requestClass, emptyList());
        redundantFreeErrorResponses.addAll(errorResponses);
        redundantFreeErrorResponses.addAll(generalErrorResponses);
        redundantFreeErrorResponses.stream()
                .map(c -> {
                    final CorrelationIdExtraction<S> correlationIdExtraction = responseCorrelationIdExtractor.extractionFor(c);
                    return responseMatcher(c, requestCorrelationId, correlationIdExtraction, false);
                })
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
    public void addGeneralErrorResponse(final Class<S> responseClass) {
        generalErrorResponses.add(responseClass);
    }

    @Override
    public Set<Class<S>> getAllPossibleResponseClasses() {
        final Set<Class<S>> allPossibleResponseClasses = new HashSet<>();
        successResponsesMap.values().forEach(allPossibleResponseClasses::addAll);
        errorResponsesMap.values().forEach(allPossibleResponseClasses::addAll);
        allPossibleResponseClasses.addAll(generalErrorResponses);
        return allPossibleResponseClasses;
    }
}
