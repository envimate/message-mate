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

package com.envimate.messageMate.useCases.specialInvocations;

import com.envimate.messageMate.processingContext.EventType;
import com.envimate.messageMate.shared.givenWhenThen.TestAction;
import com.envimate.messageMate.useCases.payloadAndErrorPayload.PayloadAndErrorPayload;
import com.envimate.messageMate.useCases.useCaseBus.UseCaseBus;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static com.envimate.messageMate.shared.environment.TestEnvironmentProperty.RESULT;
import static com.envimate.messageMate.shared.environment.TestEnvironmentProperty.TEST_OBJECT;
import static java.util.concurrent.TimeUnit.SECONDS;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class SpecialInvocationUseCaseInvoker {
    private final TestAction<UseCaseBus> testAction;

    public static SpecialInvocationUseCaseInvoker whenTheUSeCaseIsInvoked() {
        return new SpecialInvocationUseCaseInvoker((useCaseBus, testEnvironment) -> {
            final EventType eventType = testEnvironment.getPropertyAsType(TEST_OBJECT, EventType.class);
            try {
                final PayloadAndErrorPayload<Object, Object> payload =
                        useCaseBus.invokeAndWait(eventType, null, null, null, 1, SECONDS);
                testEnvironment.setPropertyIfNotSet(RESULT, payload);
            } catch (final ExecutionException e) {
                testEnvironment.setPropertyIfNotSet(RESULT, e);
            } catch (final InterruptedException | TimeoutException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }

    public TestAction<UseCaseBus> build() {
        return testAction;
    }
}
