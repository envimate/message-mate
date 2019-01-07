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

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class LockingBrokerStrategy implements BrokerStrategy {

    private final BrokerStrategy brokerStrategy;

    @Override
    public synchronized List<Subscriber<Object>> calculateReceivingSubscriber(final Object message) {
        return brokerStrategy.calculateReceivingSubscriber(message);
    }

    @Override
    public synchronized SubscriptionId add(final Class<?> messageClass, final Subscriber<Object> subscriber) {
        return brokerStrategy.add(messageClass, subscriber);
    }

    @Override
    public synchronized void remove(final SubscriptionId subscriptionId) {
        brokerStrategy.remove(subscriptionId);
    }

    @Override
    public synchronized List<Subscriber<Object>> getAllSubscribers() {
        return brokerStrategy.getAllSubscribers();
    }

    @Override
    public synchronized Map<Object, List<Subscriber<Object>>> getSubscribersPerType() {
        return brokerStrategy.getSubscribersPerType();
    }
}
