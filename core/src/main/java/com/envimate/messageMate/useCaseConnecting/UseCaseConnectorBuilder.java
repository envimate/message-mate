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

package com.envimate.messageMate.useCaseConnecting;

import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.messageFunction.MessageFunction;
import com.envimate.messageMate.useCaseConnecting.building.UseCaseConnectorStep1Builder;
import com.envimate.messageMate.useCaseConnecting.building.UseCaseConnectorStep2Builder;
import com.envimate.messageMate.useCaseConnecting.building.UseCaseConnectorStep3Builder;
import com.envimate.messageMate.useCaseConnecting.useCase.UseCase;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

import static com.envimate.messageMate.messageFunction.MessageFunctionBuilder.aMessageFunction;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class UseCaseConnectorBuilder implements UseCaseConnectorStep1Builder,
        UseCaseConnectorStep2Builder, UseCaseConnectorStep3Builder {
    private final Map<Class<?>, UseCase> useCaseMap = new HashMap<>();
    private Class<?> currentRequestClass;

    public static UseCaseConnectorStep1Builder aUseCaseConnector() {
        return new UseCaseConnectorBuilder();
    }

    public UseCaseConnectorStep2Builder delivering(final Class<?> aClass) {
        currentRequestClass = aClass;
        return this;
    }

    public UseCaseConnectorStep3Builder to(final UseCase usecase) {
        useCaseMap.put(currentRequestClass, usecase);
        return this;
    }

    public UseCaseConnector using(final MessageBus messageBus) {
        /*final MessageFunction<UseCaseRequest, UseCaseResponse> messageFunction = aMessageFunction()
                .forRequestType(UseCaseRequest.class)
                .forResponseType(UseCaseResponse.class)
                .with(UseCaseRequest.class).answeredBy(UseCaseResponse.class)
                .obtainingCorrelationIdsOfRequestsWith(UseCaseRequest::getCorrelationId)
                .obtainingCorrelationIdsOfResponsesWith(UseCaseResponse::getCorrelationId)
                .usingMessageBus(messageBus)
                .build();*/
        return new UseCaseConnectorImpl(messageBus, useCaseMap, null);
    }
}
