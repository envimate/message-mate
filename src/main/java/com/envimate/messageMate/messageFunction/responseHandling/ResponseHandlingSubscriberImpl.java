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

import com.envimate.messageMate.messageFunction.responseMatching.ExpectedResponse;
import com.envimate.messageMate.messages.DeliveryFailedMessage;
import com.envimate.messageMate.subscribing.Subscriber;
import com.envimate.messageMate.subscribing.SubscriptionId;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class ResponseHandlingSubscriberImpl<S> implements ResponseHandlingSubscriber<S> {
    private final SubscriptionId subscriptionId = SubscriptionId.newUniqueId();
    private final List<ExpectedResponse<S>> expectedResponses = new LinkedList<>();

    @Override
    public boolean accept(final S response) {
        for (final ExpectedResponse<S> expectedResponse : expectedResponses) {
            if (expectedResponse.matchesResponse(response)) {
                expectedResponse.fulfillFuture(response);
                expectedResponses.remove(expectedResponse);
            }
            if (expectedResponse.isCancelled()) {
                expectedResponses.remove(expectedResponse);
            }
        }
        return true;
    }

    @Override
    public void addResponseMatcher(final ExpectedResponse<S> expectedResponse) {
        this.expectedResponses.add(expectedResponse);
    }

    @Override
    public SubscriptionId getSubscriptionId() {
        return subscriptionId;
    }

    @Override
    public Subscriber<DeliveryFailedMessage> getDeliveryFailedHandler() {
        return new Subscriber<>() {
            @Override
            public boolean accept(final DeliveryFailedMessage message) {
                for (ExpectedResponse<S> expectedResponse : expectedResponses) {
                    final Object request = message.getOriginalMessage();
                    if (expectedResponse.matchesRequest(request)) {
                        final Exception exception = message.getCause();
                        expectedResponse.fulfillFutureWithException(exception);
                        expectedResponses.remove(expectedResponse);
                    }
                }
                return true;
            }

            @Override
            public SubscriptionId getSubscriptionId() {
                return subscriptionId;
            }
        };
    }
}
