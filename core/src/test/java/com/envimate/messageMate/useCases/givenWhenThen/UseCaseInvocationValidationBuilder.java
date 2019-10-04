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

package com.envimate.messageMate.useCases.givenWhenThen;

import com.envimate.messageMate.messageFunction.ResponseFuture;
import com.envimate.messageMate.shared.environment.TestEnvironment;
import com.envimate.messageMate.useCases.payloadAndErrorPayload.PayloadAndErrorPayload;
import com.envimate.messageMate.useCases.shared.RequestExpectedResultTuple;
import com.envimate.messageMate.useCases.shared.UseCaseInvocationConfiguration;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static com.envimate.messageMate.shared.environment.TestEnvironmentProperty.EXCEPTION;
import static com.envimate.messageMate.shared.environment.TestEnvironmentProperty.RESULT;
import static com.envimate.messageMate.shared.validations.SharedTestValidations.*;
import static com.envimate.messageMate.useCases.shared.UseCaseInvocationTestProperties.REQUEST_EXPECTED_RESULT_TUPLE;
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
        return asValidation((invocationConfiguration, testEnvironment) -> {
            assertNoExceptionThrown(testEnvironment);
            assertPayloadAsExpected(testEnvironment);
        });
    }

    public static UseCaseInvocationValidationBuilder expectAnErrorPayloadOfType(final Class<?> expectedClass) {
        return asValidation((testUseCase, testEnvironment) -> {
            assertNoExceptionThrown(testEnvironment);
            assertErrorPayloadOfClass(testEnvironment, expectedClass);
        });
    }

    public static UseCaseInvocationValidationBuilder expectAnExecutionExceptionCauseByExceptionOfType(
            final Class<?> expectedExceptionClass) {
        return asValidation((testUseCase, testEnvironment) -> {
            assertExceptionThrownOfType(testEnvironment, ExecutionException.class);
            final ExecutionException executionException = testEnvironment.getPropertyAsType(EXCEPTION, ExecutionException.class);
            final Exception cause = (Exception) executionException.getCause();
            assertEquals(cause.getClass(), expectedExceptionClass);
        });
    }

    private static void assertPayloadAsExpected(final TestEnvironment testEnvironment) {
        final RequestExpectedResultTuple requestExpectedResultTuple =
                testEnvironment.getPropertyAsType(REQUEST_EXPECTED_RESULT_TUPLE, RequestExpectedResultTuple.class);
        final PayloadAndErrorPayload<?, ?> result = testEnvironment.getPropertyAsType(RESULT, PayloadAndErrorPayload.class);
        final Object payload;
        if (requestExpectedResultTuple.isResultInErrorPayload()) {
            payload = result.getErrorPayload();
        } else {
            payload = result.getPayload();
        }
        final Object expectedResult = requestExpectedResultTuple.getExpectedResult();
        assertEquals(payload, expectedResult);
    }

    private static void assertErrorPayloadOfClass(final TestEnvironment testEnvironment, final Class<?> expectedClass) {
        final PayloadAndErrorPayload<?, ?> result = testEnvironment.getPropertyAsType(RESULT, PayloadAndErrorPayload.class);
        final Object errorPayload = result.getErrorPayload();
        assertEquals(errorPayload.getClass(), expectedClass);
    }

    public static UseCaseInvocationValidationBuilder expectTheResponseToBeReceivedByTheMessageFunction() {
        return asValidation((invocationConfiguration, testEnvironment) -> {
            assertNoExceptionThrown(testEnvironment);
            final ResponseFuture responseFuture = testEnvironment.getPropertyAsType(RESULT, ResponseFuture.class);
            final RequestExpectedResultTuple requestExpectedResultTuple =
                    testEnvironment.getPropertyAsType(REQUEST_EXPECTED_RESULT_TUPLE, RequestExpectedResultTuple.class);
            final Object expectedResult = requestExpectedResultTuple.getExpectedResult();
            try {
                final Object result;
                final int timeout = 50;
                if (requestExpectedResultTuple.isResultInErrorPayload()) {
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

    public UseCaseAdapterTestValidation build() {
        return testValidation;
    }

    interface UseCaseAdapterTestValidation {
        void validate(UseCaseInvocationConfiguration testUseCase, TestEnvironment testEnvironment);
    }
}
