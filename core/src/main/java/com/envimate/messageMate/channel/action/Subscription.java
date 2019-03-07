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

package com.envimate.messageMate.channel.action;

import com.envimate.messageMate.subscribing.ConsumerSubscriber;
import com.envimate.messageMate.subscribing.Subscriber;
import com.envimate.messageMate.subscribing.SubscriptionId;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import static com.envimate.messageMate.subscribing.ConsumerSubscriber.consumerSubscriber;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class Subscription<T> implements Action<T> {
    @Getter
    private final List<Subscriber<T>> subscribers;

    public static <T> Subscription<T> subscription() {
        /* Use CopyOnWriteArrayList, because
         - concurrent collection
         - can call unsubscribe inside of subscriber and still maintaining order (DocumentBus.until relies on that property)
         */
        final List<Subscriber<T>> linkedList = new CopyOnWriteArrayList<>();
        return new Subscription<>(linkedList);
    }

    public SubscriptionId addSubscriber(final Consumer<T> consumer) {
        final ConsumerSubscriber<T> subscriber = consumerSubscriber(consumer);
        subscribers.add(subscriber);
        return subscriber.getSubscriptionId();
    }

    public SubscriptionId addSubscriber(final Subscriber<T> subscriber) {
        subscribers.add(subscriber);
        return subscriber.getSubscriptionId();
    }

    public boolean hasSubscriber() {
        return !subscribers.isEmpty();
    }

    public void removeSubscriber(final Subscriber<T> subscriber) {
        subscribers.remove(subscriber);
    }

    public void removeSubscriber(final SubscriptionId subscriptionId) {
        subscribers.removeIf(subscriber -> subscriber.getSubscriptionId().equals(subscriptionId));
    }
}