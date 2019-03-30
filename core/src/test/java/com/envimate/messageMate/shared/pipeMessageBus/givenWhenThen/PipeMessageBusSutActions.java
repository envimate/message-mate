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

package com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen;

import com.envimate.messageMate.filtering.Filter;
import com.envimate.messageMate.identification.MessageId;
import com.envimate.messageMate.internal.pipe.statistics.PipeStatistics;
import com.envimate.messageMate.identification.CorrelationId;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import com.envimate.messageMate.subscribing.Subscriber;
import com.envimate.messageMate.subscribing.SubscriptionId;

import java.util.List;
import java.util.concurrent.TimeUnit;

public interface PipeMessageBusSutActions {

    boolean isClosed(final TestEnvironment testEnvironment);

    <R> void subscribe(Class<R> messageClass, Subscriber<R> subscriber);

    void close(boolean finishRemainingTasks);

    boolean awaitTermination(int timeout, TimeUnit timeUnit) throws InterruptedException;

    List<?> getFilter();

    void unsubscribe(SubscriptionId subscriptionId);

    <T extends TestMessage> MessageId send(T message);

    PipeStatistics getMessageStatistics();

    void addFilter(Filter<?> filter);

    void addFilter(Filter<?> filter, int position);

    List<?> getFilter(final TestEnvironment testEnvironment);

    Object removeAFilter();

    List<Subscriber<?>> getAllSubscribers();
}
