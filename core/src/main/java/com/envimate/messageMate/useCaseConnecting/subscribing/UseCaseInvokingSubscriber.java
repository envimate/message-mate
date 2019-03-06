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

package com.envimate.messageMate.useCaseConnecting.subscribing;

import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.subscribing.AcceptingBehavior;
import com.envimate.messageMate.subscribing.Subscriber;
import com.envimate.messageMate.subscribing.SubscriptionId;
import com.envimate.messageMate.useCaseConnecting.useCase.UseCase;
import com.envimate.messageMate.useCaseConnecting.UseCaseRequest;
import com.envimate.messageMate.useCaseConnecting.UseCaseResponse;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import static com.envimate.messageMate.subscribing.AcceptingBehavior.MESSAGE_ACCEPTED;
import static com.envimate.messageMate.useCaseConnecting.UseCaseResponse.useCaseResponse;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class UseCaseInvokingSubscriber implements Subscriber<UseCaseRequest> {
    private final SubscriptionId subscriptionId = SubscriptionId.newUniqueId();
    private final MessageBus messageBus;
    private final Map<Class<?>, UseCase> useCaseMap;

    public static UseCaseInvokingSubscriber useCaseInvokingSubscriber(final MessageBus messageBus,
                                                                      final Map<Class<?>, UseCase> useCaseMap) {
        final UseCaseInvokingSubscriber useCaseInvokingSubscriber = new UseCaseInvokingSubscriber(messageBus, useCaseMap);
        messageBus.subscribe(UseCaseRequest.class, useCaseInvokingSubscriber);
        return useCaseInvokingSubscriber;
    }

    @Override
    public AcceptingBehavior accept(final UseCaseRequest useCaseRequest) {
        final Object request = useCaseRequest.getRequest();
        final Class<?> requestClass = request.getClass();
        if (useCaseMap.containsKey(requestClass)) {
            final UseCase usecase = useCaseMap.get(requestClass);
            final Object response = usecase.invoke(request);
            final UseCaseResponse useCaseResponse = useCaseResponse(response, useCaseRequest);
            messageBus.send(useCaseResponse);
        }
        return MESSAGE_ACCEPTED;
    }

    @Override
    public SubscriptionId getSubscriptionId() {
        return subscriptionId;
    }

    public void unsubscribe() {
        this.messageBus.unsubcribe(subscriptionId);
    }
}
