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

package com.envimate.messageMate.qcec.querying.config;

import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.qcec.queryresolving.Query;
import com.envimate.messageMate.qcec.queryresolving.QueryResolver;
import com.envimate.messageMate.qcec.queryresolving.QueryResolverFactory;
import com.envimate.messageMate.subscribing.SubscriptionId;

import java.util.Optional;
import java.util.function.Consumer;

import static com.envimate.messageMate.messageBus.MessageBusBuilder.aMessageBus;
import static com.envimate.messageMate.messageBus.MessageBusType.SYNCHRONOUS;

public final class QueryResolverTestQueryResolver extends TestQueryResolver {
    private final QueryResolver queryResolver;

    private QueryResolverTestQueryResolver() {
        final MessageBus messageBus = aMessageBus()
                .forType(SYNCHRONOUS)
                .build();
        queryResolver = QueryResolverFactory.aQueryResolver(messageBus);
    }

    public static QueryResolverTestQueryResolver queryResolverTestQueryResolver() {
        return new QueryResolverTestQueryResolver();
    }

    @Override
    public <R> Optional<R> executeQuery(final Query<R> query) {
        return queryResolver.query(query);
    }

    @Override
    public <R> R executeRequiredQuery(final Query<R> query) {
        return queryResolver.queryRequired(query);
    }

    @Override
    public <T extends Query<?>> SubscriptionId subscribing(final Class<T> queryClass, final Consumer<T> consumer) {
        final SubscriptionId subscriptionId = queryResolver.answer(queryClass, consumer);
        return subscriptionId;
    }

    @Override
    public <T extends Query<?>> TestQueryResolver withASubscriber(final Class<T> queryClass, final Consumer<T> consumer) {
        subscribing(queryClass, consumer);
        return this;
    }

    @Override
    public void unsubscribe(final SubscriptionId subscriptionId) {
        queryResolver.unsubscribe(subscriptionId);
    }
}
