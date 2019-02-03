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

package com.envimate.messageMate.messageBus;

import com.envimate.messageMate.autoclosable.NoErrorAutoClosable;
import com.envimate.messageMate.filtering.Filter;
import com.envimate.messageMate.subscribing.Subscriber;
import com.envimate.messageMate.subscribing.SubscriptionId;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public interface MessageBus extends NoErrorAutoClosable {

    void send(Object message);

    <T> SubscriptionId subscribe(Class<T> messageClass, Consumer<T> consumer);

    <T> SubscriptionId subscribe(Class<T> messageClass, Subscriber<T> subscriber);

    void unsubcribe(SubscriptionId subscriptionId);

    void add(Filter<Object> filter);

    void add(Filter<Object> filter, int position);

    List<Filter<Object>> getFilter();

    void remove(Filter<Object> filter);

    void close(boolean finishRemainingTasks);

    boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException;

    boolean isShutdown();

    MessageBusStatusInformation getStatusInformation();
}
