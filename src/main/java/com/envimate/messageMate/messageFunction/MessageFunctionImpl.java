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

package com.envimate.messageMate.messageFunction;

import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.messageFunction.requestResponseRelation.RequestResponseRelationMap;
import com.envimate.messageMate.messageFunction.responseHandling.ResponseHandlingSubscriber;
import com.envimate.messageMate.messageFunction.responseMatching.ExpectedResponse;
import com.envimate.messageMate.messageFunction.responseMatching.ResponseMatcher;
import lombok.NonNull;

import java.util.List;
import java.util.Set;

final class MessageFunctionImpl<R, S> implements MessageFunction<R, S> {
    private final MessageBus messageBus;
    private final ResponseHandlingSubscriber<S> responseHandlingSubscriber;
    private final RequestResponseRelationMap<R, S> requestResponseRelationMap;

    private MessageFunctionImpl(@NonNull final MessageBus messageBus,
                                @NonNull final ResponseHandlingSubscriber<S> responseHandlingSubscriber,
                                @NonNull final RequestResponseRelationMap<R, S> requestResponseRelationMap) {
        this.messageBus = messageBus;
        this.responseHandlingSubscriber = responseHandlingSubscriber;
        this.requestResponseRelationMap = requestResponseRelationMap;
        final Set<Class<S>> responseClassToSubscribe = requestResponseRelationMap.getAllPossibleResponseClasses();
        for (final Class<S> aClass : responseClassToSubscribe) {
            messageBus.subscribe(aClass, responseHandlingSubscriber);
        }
    }

    static <R, S> MessageFunctionImpl<R, S> messageFunction(
            @NonNull final MessageBus messageBus,
            @NonNull final ResponseHandlingSubscriber<S> responseHandlingSubscriber,
            @NonNull final RequestResponseRelationMap<R, S> requestResponseRelationMap) {
        return new MessageFunctionImpl<>(messageBus, responseHandlingSubscriber, requestResponseRelationMap);
    }

    @Override
    public ResponseFuture<S> request(final R request) {
        final List<ResponseMatcher<S>> responseMatchers = requestResponseRelationMap.responseMatchers(request);
        final ExpectedResponse<S> expectedResponse = ExpectedResponse.forRequest(responseMatchers);
        responseHandlingSubscriber.addResponseMatcher(expectedResponse);
        messageBus.send(request);
        final ResponseFuture<S> responseFuture = expectedResponse.getAssociatedFuture();
        return responseFuture;
    }

    @Override
    public void close() throws Exception {
        messageBus.close();
    }
}
