/*
 * Copyright (c) 2019 envimate GmbH - https://envimate.com/.
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

package com.envimate.messageMate.channel.action;

import com.envimate.messageMate.processingContext.ProcessingContext;
import com.envimate.messageMate.subscribing.AcceptingBehavior;
import com.envimate.messageMate.subscribing.Subscriber;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;

/**
 * The {@code ActionHandler} implementation for the {@code Subscription} {@code Action}.
 *
 * @param <T>the type of messages of the {@code Channel}
 * @see <a href="https://github.com/envimate/message-mate#subscription">Message Mate Documentation</a>
 */
@RequiredArgsConstructor(access = PRIVATE)
public final class SubscriptionActionHandler<T> implements ActionHandler<Subscription<T>, T> {

    /**
     * Factory method for a new {@code SubscriptionActionHandler}.
     *
     * @param <T> the type of messages of the {@code Channel}
     * @return a new {@code SubscriptionActionHandler}
     */
    public static <T> SubscriptionActionHandler<T> subscriptionActionHandler() {
        return new SubscriptionActionHandler<>();
    }

    /**
     * Takes the message and delivers it to all {@code Subscribers}.
     *
     * @param subscription      the {@code Subscription} {@code Action}
     * @param processingContext the message
     */
    @Override
    public void handle(final Subscription<T> subscription, final ProcessingContext<T> processingContext) {
        final List<Subscriber<ProcessingContext<T>>> subscribers = subscription.getRealSubscribers();
        for (final Subscriber<ProcessingContext<T>> subscriber : subscribers) {
            final AcceptingBehavior acceptingBehavior = subscriber.accept(processingContext);
            if (!acceptingBehavior.continueDelivery()) {
                return;
            }
        }
    }
}
