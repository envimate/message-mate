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

import com.envimate.messageMate.configuration.MessageBusConfiguration;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class BrokerStrategyFactory {

    public static BrokerStrategy aBrokerStrategy(final MessageBusConfiguration messageBusConfiguration) {
        final BrokerStrategyType strategyType = messageBusConfiguration.getBrokerStrategyType();
        return aBrokerStrategyForSpecificType(strategyType);
    }

    public static BrokerStrategy aBrokerStrategyForSpecificType(final BrokerStrategyType strategyType) {
        switch (strategyType) {
            case DELIVERY_TO_SAME_CLASS_AS_MESSAGE:
                return new LockingBrokerStrategy(new MessageHashMapBrokerStrategy());
            case DELIVERY_TO_SAME_CLASS_AS_QUERY:
                return new LockingBrokerStrategy(new MessageHashMapBrokerStrategy());
            case DELIVERY_TO_SAME_CLASS_ONLY:
                return new LockingBrokerStrategy(new HashMapBasedBrokerStrategy());
            case DELIVERY_TO_CLASS_AND_DIRECT_INHERITED_INTERFACES:
                return new DirectInheritedInterfaceIncludingBrokerStrategy();
            case QUERY_RESOLVING_STRATEGY:
                return new QueryResolvingBrokerStrategy();
            default:
                throw new UnknownBrokerStrategyTypeException("No broker strategy for type " + strategyType);
        }
    }
}
