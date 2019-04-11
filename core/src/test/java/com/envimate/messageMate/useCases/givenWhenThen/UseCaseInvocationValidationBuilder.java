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

package com.envimate.messageMate.useCases.givenWhenThen;

import com.envimate.messageMate.messageFunction.ResponseFuture;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.useCases.shared.TestUseCase;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.RESULT;
import static com.envimate.messageMate.shared.validations.SharedTestValidations.*;
import static com.envimate.messageMate.useCases.givenWhenThen.UseCaseInvocationTestProperties.RETRIEVE_ERROR_FROM_FUTURE;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static lombok.AccessLevel.PRIVATE;
import static org.junit.jupiter.api.Assertions.fail;

@RequiredArgsConstructor(access = PRIVATE)
public final class UseCaseInvocationValidationBuilder {
    private final UseCaseAdapterTestValidation testValidation;

    private static UseCaseInvocationValidationBuilder asValidation(final UseCaseAdapterTestValidation testValidation) {
        return new UseCaseInvocationValidationBuilder(testValidation);
    }

    public static UseCaseInvocationValidationBuilder expectTheUseCaseToBeInvokedOnce() {
        return asValidation((testUseCase, testEnvironment) -> {
            assertNoExceptionThrown(testEnvironment);
            final Object expectedResult = testUseCase.getExpectedResult(testEnvironment);
            assertResultEqualsExpected(testEnvironment, expectedResult);
        });
    }

    public static UseCaseInvocationValidationBuilder expectTheResponseToBeReceivedByTheMessageFunction() {
        return asValidation((testUseCase, testEnvironment) -> {
            assertNoExceptionThrown(testEnvironment);
            final Object expectedResult = testUseCase.getExpectedResult(testEnvironment);
            final ResponseFuture responseFuture = testEnvironment.getPropertyAsType(RESULT, ResponseFuture.class);
            try {
                final Object result;
                final int timeout = 10;
                if (testEnvironment.has(RETRIEVE_ERROR_FROM_FUTURE)) {
                    result = responseFuture.getErrorResponse(timeout, MILLISECONDS);
                } else {
                    result = responseFuture.get(timeout, MILLISECONDS);
                }
                assertEquals(result, expectedResult);
            } catch (final InterruptedException | TimeoutException e) {
                fail(e);
            } catch (final ExecutionException e) {
                final Throwable testException = e.getCause();
                assertEquals(expectedResult, testException);
            }
        });
    }

    public static UseCaseInvocationValidationBuilder expectAExceptionOfType(final Class<?> expectedExceptionClass) {
        return asValidation((testUseCase, testEnvironment) -> {
            assertExceptionThrownOfType(testEnvironment, expectedExceptionClass);
        });
    }

    public static UseCaseInvocationValidationBuilder expectTheUseCaseToBeInvokedByTheUseCaseBus() {
        return asValidation((testUseCase, testEnvironment) -> {
            assertNoExceptionThrown(testEnvironment);
            assertResultAndExpectedResultAreEqual(testEnvironment);
        });
    }

    public UseCaseAdapterTestValidation build() {
        return testValidation;
    }

    interface UseCaseAdapterTestValidation {
        void validate(TestUseCase testUseCase, TestEnvironment testEnvironment);
    }
}
