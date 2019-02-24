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

import com.envimate.messageMate.messageFunction.ResponseFuture;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static com.envimate.messageMate.messageFunction.responseMatching.ExpectedResponseFuture.expectedResponseFuture;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExpectedResponse<S> {
    private final Object request;
    private final List<ResponseMatcher<S>> responseMatchers;
    private final ExpectedResponseFuture<S> future = expectedResponseFuture();
    private final List<Runnable> cleanUps = new ArrayList<>();
    private volatile MatchResult lastMatchResult;

    public static <S> ExpectedResponse<S> forRequest(final Object request, final List<ResponseMatcher<S>> responseMatchers) {
        return new ExpectedResponse<>(request, responseMatchers);
    }

    public synchronized boolean matchesResponse(final Object response) {
        if (future.isCancelled()) {
            return false;
        } else {
            final MatchResult matchResult = matchResult(response);
            return matchResult.didMatch(response);
        }
    }

    public boolean matchesRequest(final Object request) {
        return this.request.equals(request);
    }

    public ResponseFuture<S> getAssociatedFuture() {
        return future;
    }

    public synchronized void fulfillFuture(final S response) {
        final MatchResult matchResult = matchResult(response);
        if (matchResult.didMatch(response)) {
            final boolean wasSuccessful = matchResult.wasSuccessful();
            future.fullFill(response, wasSuccessful);
        } else {
            throw new CannotFulfillFutureWithNotMatchingResponseException("Response " + response + " was no expected.");
        }
    }

    public synchronized void fulfillFutureWithException(final Exception e) {
        future.fullFillWithException(e);
    }

    private MatchResult matchResult(final Object response) {
        if (lastMatchResult != null && lastMatchResult.wasMatchedAgainst(response)) {
            return lastMatchResult;
        } else {
            final Optional<ResponseMatcher<S>> matcherOptional = responseMatchers.stream()
                    .filter(responseMatcher -> responseMatcher.matches(response))
                    .findAny();
            if (matcherOptional.isPresent()) {
                final ResponseMatcher<S> responseMatcher = matcherOptional.get();
                lastMatchResult = MatchResult.lastMatchResult(response, responseMatcher);
            } else {
                lastMatchResult = MatchResult.noMatchResult();
            }
            return lastMatchResult;
        }
    }

    public boolean isDone() {
        return future.isDone();
    }

    public void addCleanUp(final Runnable cleanUp) {
        this.cleanUps.add(cleanUp);
    }

    public void onCleanup() {
        cleanUps.forEach(Runnable::run);
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class MatchResult {
        private final Object matchedResponse;
        private final ResponseMatcher<?> responseMatcher;

        static MatchResult lastMatchResult(@NonNull final Object response, @NonNull final ResponseMatcher<?> responseMatcher) {
            return new MatchResult(response, responseMatcher);
        }

        static MatchResult noMatchResult() {
            return new MatchResult(null, null);
        }

        boolean wasMatchedAgainst(final Object response) {
            return response.equals(matchedResponse);
        }

        boolean didMatch(final Object response) {
            return response.equals(matchedResponse);
        }

        boolean wasSuccessful() {
            return responseMatcher.wasSuccessResponse(matchedResponse);
        }
    }

}
