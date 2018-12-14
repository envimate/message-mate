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

package com.envimate.messageMate.qcec.domainBus;

import com.envimate.messageMate.qcec.queryresolving.Query;
import com.envimate.messageMate.subscribing.SubscriptionId;

import java.util.Optional;

public interface DocumentBus {

    <T extends Query> AnswerStep1Builder<T> answer(Class<T> queryClass);

    <T> AnswerStep1Builder<T> ensure(Class<T> constraintClass);

    <T> AnswerStep1Builder<T> reactTo(Class<T> event);

    <T> Optional<T> query(Query<T> query);

    <T> T queryRequired(Query<T> query);

    void enforce(Object constraint);

    void publish(Object event);

    void unsubscribe(SubscriptionId subscriptionId);
}
