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
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import lombok.RequiredArgsConstructor;

import static com.envimate.messageMate.messageBus.MessageBusBuilder.aMessageBus;
import static com.envimate.messageMate.qcec.shared.TestEnvironment.emptyTestEnvironment;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.CONTROLLABLE_ENV_OBJECT;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.SUT;
import static com.envimate.messageMate.useCaseConnecting.UseCaseConnectorBuilder.aUseCaseConnector;
import static com.envimate.messageMate.useCaseConnecting.useCase.UseCaseInvoker.useCase;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class UseCaseConnectorSetupBuilder {
    private final TestEnvironment testEnvironment;

    public static UseCaseConnectorSetupBuilder aConfiguredUseCaseConnector() {
        final TestEnvironment testEnvironment = emptyTestEnvironment();
        final MessageBus messageBus = aMessageBus()
                .build();
        testEnvironment.setProperty(CONTROLLABLE_ENV_OBJECT, messageBus);
        final UseCaseConnector useCaseConnector = aUseCaseConnector()
                .delivering(SampleRequest.class)
                .to(useCase(SampleUseCase.class))
                .using(messageBus);
        testEnvironment.setProperty(SUT, useCaseConnector);
        return new UseCaseConnectorSetupBuilder(testEnvironment);
    }

    public TestEnvironment build() {
        return testEnvironment;
    }
}
