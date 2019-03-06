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

package com.envimate.messageMate.useCaseConnecting;

import com.envimate.messageMate.error.ExceptionInSubscriberException;
import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.messageFunction.MessageFunction;
import com.envimate.messageMate.messageFunction.ResponseFuture;
import com.envimate.messageMate.useCaseConnecting.subscribing.UseCaseInvokingSubscriber;
import com.envimate.messageMate.useCaseConnecting.useCase.UseCase;

import java.util.Map;
import java.util.function.Consumer;

import static com.envimate.messageMate.useCaseConnecting.UseCaseRequest.useCaseRequest;
import static com.envimate.messageMate.useCaseConnecting.subscribing.UseCaseInvokingSubscriber.useCaseInvokingSubscriber;

public final class UseCaseConnectorImpl implements UseCaseConnector {
    private final MessageFunction<UseCaseRequest, UseCaseResponse> messageFunction;
    private final UseCaseInvokingSubscriber useCaseInvokingSubscriber;
    private boolean closed;

    UseCaseConnectorImpl(final MessageBus messageBus,
                         final Map<Class<?>, UseCase> useCaseMap,
                         final MessageFunction<UseCaseRequest, UseCaseResponse> messageFunction) {
        this.messageFunction = messageFunction;
        this.useCaseInvokingSubscriber = useCaseInvokingSubscriber(messageBus, useCaseMap);
    }

    @Override
    public void send(final Object request, final Consumer<Object> onResponseCallback) {
        if (closed) {
            throw new IllegalStateException(UseCaseConnector.class.getSimpleName() + " is already closed.");
        }
        final UseCaseRequest useCaseRequest = useCaseRequest(request);
        final ResponseFuture<UseCaseResponse> responseFuture = messageFunction.request(useCaseRequest);
        responseFuture.then((useCaseResponse, wasSuccessful, exception) -> {
            if (exception != null) {
                final Object returnedException;
                if (exception instanceof ExceptionInSubscriberException) {
                    returnedException = exception.getCause();
                } else {
                    returnedException = exception;
                }
                onResponseCallback.accept(returnedException);
            } else {
                final Object response = useCaseResponse.getResponse();
                onResponseCallback.accept(response);
            }
        });
    }

    //No cancel of all pending requests right now
    @Override
    public void close() {
        if (!closed) {
            closed = true;
            useCaseInvokingSubscriber.unsubscribe();
            messageFunction.close();
        }
    }
}
