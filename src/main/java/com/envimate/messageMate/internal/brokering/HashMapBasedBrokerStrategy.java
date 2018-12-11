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

package com.envimate.messageMate.internal.brokering;

import com.envimate.messageMate.subscribing.Subscriber;
import com.envimate.messageMate.subscribing.SubscriptionId;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

//Not Threadsafe
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class HashMapBasedBrokerStrategy implements BrokerStrategy {

    private final Map<Object, List<Subscriber<Object>>> receiverMap = new HashMap<>();
    private final Map<SubscriptionId, List<Class>> reverseLookupMap = new HashMap<>();

    @Override
    public List<Subscriber<Object>> calculateReceivingSubscriber(final Object messageClass) {
        return receiverMap.getOrDefault(messageClass, new LinkedList<>());
    }

    @Override
    public SubscriptionId add(final Class messageClass, final Subscriber<Object> subscriber) {
        final List<Subscriber<Object>> receiver = receiverMap.getOrDefault(messageClass, new LinkedList<>());
        receiver.add(subscriber);
        receiverMap.put(messageClass, receiver);

        final SubscriptionId subscriptionId = subscriber.getSubscriptionId();
        final List<Class> classesForSubscriptionId = reverseLookupMap.getOrDefault(subscriptionId, new LinkedList<>());
        classesForSubscriptionId.add(messageClass);
        reverseLookupMap.put(subscriptionId, classesForSubscriptionId);
        return subscriptionId;
    }

    @Override
    public void remove(final SubscriptionId subscriptionId) {
        if (reverseLookupMap.containsKey(subscriptionId)) {
            final List<Class> classes = reverseLookupMap.get(subscriptionId);
            for (final Class messageClass : classes) {
                final List<Subscriber<Object>> receivers = receiverMap.get(messageClass);
                receivers.removeIf(subscriber -> subscriber.getSubscriptionId().equals(subscriptionId));
            }
        }
    }

    @Override
    public List<Subscriber<Object>> getAllSubscribers() {
        return receiverMap.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public Map<Object, List<Subscriber<Object>>> getSubscribersPerType() {
        return new HashMap<>(receiverMap);
    }
}
