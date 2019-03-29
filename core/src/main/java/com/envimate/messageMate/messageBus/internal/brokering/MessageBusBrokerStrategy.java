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

package com.envimate.messageMate.messageBus.internal.brokering;

import com.envimate.messageMate.channel.Channel;
import com.envimate.messageMate.channel.ProcessingContext;
import com.envimate.messageMate.messageFunction.correlation.CorrelationId;
import com.envimate.messageMate.subscribing.Subscriber;
import com.envimate.messageMate.subscribing.SubscriptionId;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface MessageBusBrokerStrategy {

    Set<Channel<?>> getDeliveringChannelsFor(Class<?> messageClass);

    <T> void addSubscriber(Class<T> tClass, Subscriber<T> subscriber);

    <T> void addRawSubscriber(Class<T> tClass, Subscriber<ProcessingContext<T>> subscriber);

    void removeSubscriber(SubscriptionId subscriptionId);

    List<Subscriber<?>> getAllSubscribers();

    Map<Class<?>, List<Subscriber<?>>> getSubscribersPerType();

    Channel<?> getClassSpecificChannel(Class<?> messageClass);
}
