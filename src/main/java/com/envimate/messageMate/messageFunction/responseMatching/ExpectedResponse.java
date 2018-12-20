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

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExpectedResponse<S> {
    private final List<ResponseMatcher<S>> responseMatchers;
    private final ExpectedResponseFuture<S> future = ExpectedResponseFuture.expectedResponseFuture();
    private MatchResult<S> lastMatchResult;

    public static <S> ExpectedResponse<S> forRequest(final List<ResponseMatcher<S>> responseMatchers) {
        return new ExpectedResponse<>(responseMatchers);
    }

    public boolean matches(final S response) {
        if (future.isCancelled()) {
            return false;
        } else {
            final MatchResult<S> matchResult = matchResult(response);
            return matchResult.didMatch(response);
        }
    }

    public ResponseFuture<S> getAssociatedFuture() {
        return future;
    }

    public void fulfillFuture(final S response) {
        final MatchResult<S> matchResult = matchResult(response);
        if (matchResult.didMatch(response)) {
            final boolean wasSuccessful = matchResult.wasSuccessful();
            future.fullFill(response, wasSuccessful);
        } else {
            throw new CannotFulfillFutureWithNotMatchingResponseException("Response " + response + " was no expected.");
        }
    }

    private MatchResult<S> matchResult(final S response) {
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

    public boolean isCancelled() {
        return future.isCancelled();
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class MatchResult<S> {
        private final S matchedResponse;
        private final ResponseMatcher<S> responseMatcher;

        static <S> MatchResult<S> lastMatchResult(@NonNull final S response, @NonNull final ResponseMatcher<S> responseMatcher) {
            return new MatchResult<>(response, responseMatcher);
        }

        static <S> MatchResult<S> noMatchResult() {
            return new MatchResult<>(null, null);
        }

        boolean wasMatchedAgainst(final S response) {
            return matchedResponse.equals(response);
        }

        boolean didMatch(final S response) {
            return matchedResponse.equals(response);
        }

        boolean wasSuccessful() {
            return responseMatcher.wasSuccessResponse(matchedResponse);
        }
    }

}
