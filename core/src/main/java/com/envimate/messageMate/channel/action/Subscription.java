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

/**
 * A {@code Subscription} object manages a list of {@code Subscribers}. Each message is distributed to each {@code Subscriber},
 * if no exception occurred.
 *
 * @param <T> the type of messages of the {@code Channel}
 *
 * @see <a href="https://github.com/envimate/message-mate#subscription">Message Mate Documentation</a>
 */
@RequiredArgsConstructor(access = PRIVATE)
public final class Subscription<T> implements Action<T> {
    @Getter
    private final List<Subscriber<T>> subscribers;

    /**
     * Creates a new {@code Subscription} object.
     *
     * @param <T> the type of messages of the {@code Channel}
     *
     * @return a new {@code Subscription} object
     */
    public static <T> Subscription<T> subscription() {
        /* Use CopyOnWriteArrayList, because
         - concurrent collection
         - can call unsubscribe inside of subscriber and still maintaining order (DocumentBus.until relies on that property)
         */
        final List<Subscriber<T>> linkedList = new CopyOnWriteArrayList<>();
        return new Subscription<>(linkedList);
    }

    /**
     * Adds a the consumer wrapped in a {@code Subscriber} object.
     *
     * @param consumer the consuming {@code Subscriber} to be added
     * @return the wrapping {@code Subscriber's} {@code SubscriptionId}
     */
    public SubscriptionId addSubscriber(final Consumer<T> consumer) {
        final ConsumerSubscriber<T> subscriber = consumerSubscriber(consumer);
        subscribers.add(subscriber);
        return subscriber.getSubscriptionId();
    }

    /**
     * Adds a {@code Subscriber}.
     *
     * @param subscriber the {@code Subscriber} to be added
     * @return the wrapping {@code Subscriber's} {@code SubscriptionId}
     */
    public SubscriptionId addSubscriber(final Subscriber<T> subscriber) {
        subscribers.add(subscriber);
        return subscriber.getSubscriptionId();
    }

    /**
     * Returns if at least one subscriber exists.
     *
     * @return {@code true} if at least one {@code Subscriber} exists, {@code false} otherwise
     */
    public boolean hasSubscribers() {
        return !subscribers.isEmpty();
    }

    /**
     * Removes the given {@code Subscriber}.
     *
     * @param subscriber the {@code Subscriber} to be removed
     */

    public void removeSubscriber(final Subscriber<T> subscriber) {
        subscribers.remove(subscriber);
    }


    /**
     * Removes all {@code Subscribers} that match the given {@code SubscriptionId}.
     *
     * @param subscriptionId the {@code SubscriptionId}, for which all {@code Subscribers} should be removed.
     */
    public void removeSubscriber(final SubscriptionId subscriptionId) {
        subscribers.removeIf(subscriber -> subscriber.getSubscriptionId().equals(subscriptionId));
    }
}
