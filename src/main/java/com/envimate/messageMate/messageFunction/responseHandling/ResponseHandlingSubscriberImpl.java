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

package com.envimate.messageMate.messageFunction.responseHandling;

import com.envimate.messageMate.channel.Channel;
import com.envimate.messageMate.channel.ProcessingContext;
import com.envimate.messageMate.messageFunction.responseMatching.ExpectedResponse;
import com.envimate.messageMate.subscribing.AcceptingBehavior;
import com.envimate.messageMate.subscribing.SubscriptionId;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.envimate.messageMate.subscribing.AcceptingBehavior.MESSAGE_ACCEPTED;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class ResponseHandlingSubscriberImpl implements ResponseHandlingSubscriber {
    private final SubscriptionId subscriptionId = SubscriptionId.newUniqueId();
    private final List<ExpectedResponse<?>> expectedResponses = new CopyOnWriteArrayList<>();

    @Override
    public AcceptingBehavior accept(final Object response) {
        for (final ExpectedResponse<?> expectedResponse : expectedResponses) {
            //synchronise on response to block concurrently fulfilling/removing Threads
            if (expectedResponse.matchesResponse(response) || expectedResponse.isDone()) {
                handle(expectedResponse, response);
            }
        }
        return MESSAGE_ACCEPTED;
    }

    private void handle(final ExpectedResponse<?> expectedResponse, final Object response) {
        synchronized (expectedResponse) {
            //first check that not fulfilled in meantime (CopyOnWriteList would allow that)
            if (expectedResponse.isDone()) {
                expectedResponses.remove(expectedResponse);
                expectedResponse.onCleanup();
            } else if (expectedResponse.matchesResponse(response)) {
                expectedResponse.fulfillFuture(response);
                expectedResponses.remove(expectedResponse);
                expectedResponse.onCleanup();
            }
        }
    }

    @Override
    public void addResponseMatcher(final ExpectedResponse<?> expectedResponse) {
        this.expectedResponses.add(expectedResponse);
    }

    @Override
    public SubscriptionId getSubscriptionId() {
        return subscriptionId;
    }

    @Override
    public boolean shouldDeliveryChannelErrorBeHandledAndDeliveryAborted(final ProcessingContext<?> message, final Exception e,
                                                                         final Channel<?> channel) {
        return true;
    }

    @Override
    public void handleDeliveryChannelException(final ProcessingContext<?> processingContext, final Exception exception,
                                               final Channel<?> channel) {
        final Object request = processingContext.getPayload();
        for (final ExpectedResponse<?> expectedResponse : expectedResponses) {
            if (expectedResponse.matchesRequest(request)) {
                expectedResponse.fulfillFutureWithException(exception);
                expectedResponses.remove(expectedResponse);
            }
        }
    }

    @Override
    public void handleFilterException(final ProcessingContext<?> processingContext, final Exception exception,
                                      final Channel<?> channel) {
        final Object request = processingContext.getPayload();
        for (final ExpectedResponse<?> expectedResponse : expectedResponses) {
            if (expectedResponse.matchesRequest(request)) {
                expectedResponse.fulfillFutureWithException(exception);
                expectedResponses.remove(expectedResponse);
            }
        }
    }
}
