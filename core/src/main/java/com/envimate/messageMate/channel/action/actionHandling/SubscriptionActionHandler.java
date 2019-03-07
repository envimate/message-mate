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

package com.envimate.messageMate.channel.action.actionHandling;

import com.envimate.messageMate.channel.ProcessingContext;
import com.envimate.messageMate.channel.action.Subscription;
import com.envimate.messageMate.subscribing.AcceptingBehavior;
import com.envimate.messageMate.subscribing.Subscriber;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class SubscriptionActionHandler<T> implements ActionHandler<Subscription<T>, T> {

    public static <T> SubscriptionActionHandler<T> subscriptionActionHandler() {
        return new SubscriptionActionHandler<>();
    }

    @Override
    public void handle(final Subscription<T> subscription, final ProcessingContext<T> processingContext) {
        final T payload = processingContext.getPayload();
        final List<Subscriber<T>> subscribers = subscription.getSubscribers();
        for (final Subscriber<T> subscriber : subscribers) {
            final AcceptingBehavior acceptingBehavior = subscriber.accept(payload);
            if (!acceptingBehavior.continueDelivery()) {
                return;
            }
        }
    }
}