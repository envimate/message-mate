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
import com.envimate.messageMate.subscribing.SubscriptionId;
import lombok.NonNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

final class MessageFunctionImpl<R, S> implements MessageFunction<R, S> {
    private final MessageBus messageBus;
    private final ResponseHandlingSubscriber responseHandlingSubscriber;
    private final RequestResponseRelationMap<R, S> requestResponseRelationMap;
    private boolean closed;

    private MessageFunctionImpl(@NonNull final MessageBus messageBus,
                                @NonNull final ResponseHandlingSubscriber responseHandlingSubscriber,
                                @NonNull final RequestResponseRelationMap<R, S> requestResponseRelationMap) {
        this.messageBus = messageBus;
        this.responseHandlingSubscriber = responseHandlingSubscriber;
        this.requestResponseRelationMap = requestResponseRelationMap;
        final Set<Class<?>> responseClassToSubscribe = requestResponseRelationMap.getAllPossibleResponseClasses();
        for (final Class<?> aClass : responseClassToSubscribe) {
            @SuppressWarnings("unchecked")
            final Class<Object> castedClass = (Class) aClass;
            messageBus.subscribe(castedClass, responseHandlingSubscriber);
        }

    }

    static <R, S> MessageFunctionImpl<R, S> messageFunction(
            @NonNull final MessageBus messageBus,
            @NonNull final ResponseHandlingSubscriber responseHandlingSubscriber,
            @NonNull final RequestResponseRelationMap<R, S> requestResponseRelationMap) {
        return new MessageFunctionImpl<>(messageBus, responseHandlingSubscriber, requestResponseRelationMap);
    }

    @Override
    public ResponseFuture<S> request(final R request) {
        if (closed) {
            return null;
        }
        final List<ResponseMatcher> responseMatchers = requestResponseRelationMap.responseMatchers(request);
        final ExpectedResponse<S> expectedResponse = ExpectedResponse.forRequest(request, responseMatchers);
        responseHandlingSubscriber.addResponseMatcher(expectedResponse);
        registerErrorListener(request, expectedResponse);
        messageBus.send(request);
        final ResponseFuture<S> responseFuture = expectedResponse.getAssociatedFuture();
        return responseFuture;
    }

    private void registerErrorListener(final R request, final ExpectedResponse<S> expectedResponse) {
        final Set<Class<?>> classesToListenForErrorsOn = requestResponseRelationMap.getAllPossibleResponseClasses();
        final LinkedList<Class<?>> classes = new LinkedList<>(classesToListenForErrorsOn);
        classes.add(request.getClass());
        final SubscriptionId subscriptionId = messageBus.onError(classes, (t, e) -> {
            synchronized (expectedResponse) {
                if (!expectedResponse.isDone()) {
                    if (expectedResponse.matchesRequest(t) || expectedResponse.matchesResponse(t)) {
                        expectedResponse.fulfillFutureWithException(e);
                    }
                }
            }
        });
        expectedResponse.addCleanUp(() -> messageBus.unregisterErrorHandler(subscriptionId));
    }

    //No automatic cancel right now
    @Override
    public void close() {
        closed = true;
        final SubscriptionId subscriptionId = responseHandlingSubscriber.getSubscriptionId();
        messageBus.unsubcribe(subscriptionId);
    }
}
