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

package com.envimate.messageMate.serializedMessageBus.givenWhenThen;

import com.envimate.messageMate.shared.givenWhenThen.TestAction;
import com.envimate.messageMate.shared.environment.TestEnvironment;
import com.envimate.messageMate.shared.givenWhenThen.TestValidation;
import com.envimate.messageMate.serializedMessageBus.SerializedMessageBus;
import lombok.RequiredArgsConstructor;

import static com.envimate.messageMate.shared.environment.TestEnvironmentProperty.EXCEPTION;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
public final class Then {
    private final SerializedMessageBusSetupBuilder setupBuilder;
    private final SerializedMessageBusActionBuilder actionBuilder;

    public void then(final SerializedMessageBusValidationBuilder validationBuilder) {
        final SerializedMessageBusSetupBuilder.SerializedMessageBusSetup setup = setupBuilder.build();
        final SerializedMessageBus serializedMessageBus = setup.getSerializedMessageBus();
        final TestEnvironment testEnvironment = setup.getTestEnvironment();

        final TestAction<SerializedMessageBus> action = actionBuilder.build();
        try {
            action.execute(serializedMessageBus, testEnvironment);
        } catch (final Exception e) {
            testEnvironment.setPropertyIfNotSet(EXCEPTION, e);
        }
        final TestValidation testValidation = validationBuilder.build();
        testValidation.validate(testEnvironment);
    }
}
