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

import com.envimate.messageMate.shared.givenWhenThen.TestValidation;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.ExecutionException;

import static com.envimate.messageMate.shared.environment.TestEnvironmentProperty.RESULT;
import static com.envimate.messageMate.shared.validations.SharedTestValidations.assertEquals;
import static com.envimate.messageMate.shared.validations.SharedTestValidations.assertResultOfClass;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class SpecialInvocationValidator {
    private final TestValidation testValidation;

    private static SpecialInvocationValidator asValidation(final TestValidation testValidation) {
        return new SpecialInvocationValidator(testValidation);
    }

    public static SpecialInvocationValidator expectExecutionExceptionContaining(final RuntimeException expectedException) {
        return asValidation(testEnvironment -> {
            assertResultOfClass(testEnvironment, ExecutionException.class);
            final ExecutionException exception = testEnvironment.getPropertyAsType(RESULT, ExecutionException.class);
            assertEquals(exception.getCause(), expectedException);
        });
    }

    public static SpecialInvocationValidator expectExecutionExceptionContainingExceptionClass(
            final Class<?> expectedExceptionClass) {
        return asValidation(testEnvironment -> {
            assertResultOfClass(testEnvironment, ExecutionException.class);
            final ExecutionException exception = testEnvironment.getPropertyAsType(RESULT, ExecutionException.class);
            final Throwable cause = exception.getCause();
            assertEquals(cause.getClass(), expectedExceptionClass);
        });
    }

    public TestValidation build() {
        return testValidation;
    }
}
