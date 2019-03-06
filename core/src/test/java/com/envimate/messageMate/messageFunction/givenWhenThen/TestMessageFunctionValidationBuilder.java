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

package com.envimate.messageMate.messageFunction.givenWhenThen;

import com.envimate.messageMate.correlation.CorrelationId;
import com.envimate.messageMate.messageFunction.ResponseFuture;
import com.envimate.messageMate.messageFunction.testResponses.ErrorTestResponse;
import com.envimate.messageMate.messageFunction.testResponses.RequestResponseFuturePair;
import com.envimate.messageMate.messageFunction.testResponses.TestRequest;
import com.envimate.messageMate.messageFunction.testResponses.TestResponse;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.qcec.shared.TestValidation;
import com.envimate.messageMate.shared.subscriber.TestException;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.*;
import static com.envimate.messageMate.shared.validations.SharedTestValidations.assertResultAndExpectedResultAreEqual;
import static com.envimate.messageMate.shared.validations.SharedTestValidations.assertResultOfClass;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static lombok.AccessLevel.PRIVATE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.*;

@RequiredArgsConstructor(access = PRIVATE)
public final class TestMessageFunctionValidationBuilder {
    private final TestValidation testValidation;

    public static TestMessageFunctionValidationBuilder expectTheResponseToBeReceived() {
        return new TestMessageFunctionValidationBuilder(testEnvironment -> {
            ensureNoExceptionThrown(testEnvironment);
            final ResponseFuture<?> responseFuture = testEnvironment.getPropertyAsType(RESULT, ResponseFuture.class);
            final TestRequest testRequest = testEnvironment.getPropertyAsType(TEST_OBJECT, TestRequest.class);
            assertResponseForRequest(responseFuture, testRequest);
        });
    }

    private static void assertResponseForRequest(ResponseFuture<?> responseFuture, TestRequest testRequest) {
        assertTrue(responseFuture.wasSuccessful());
        try {
            final Object response = responseFuture.get();
            if (response instanceof TestResponse) {
                final TestResponse testResponse = (TestResponse) response;
                final CorrelationId expectedCorrelationId = testRequest.getCorrelationId();
                assertThat(testResponse.getCorrelationId(), equalTo(expectedCorrelationId));
            } else {
                fail("Unexpected Result in validation found.");
            }
        } catch (InterruptedException | ExecutionException e) {
            fail(e);
        }
    }

    public static TestMessageFunctionValidationBuilder expectCorrectResponseReceivedForEachRequest() {
        return new TestMessageFunctionValidationBuilder(testEnvironment -> {
            ensureNoExceptionThrown(testEnvironment);
            @SuppressWarnings("unchecked")
            final List<RequestResponseFuturePair> requestResponseFuturePairs = (List<RequestResponseFuturePair>) testEnvironment.getProperty(RESULT);
            for (RequestResponseFuturePair requestResponseFuturePair : requestResponseFuturePairs) {
                final ResponseFuture<TestResponse> responseFuture = requestResponseFuturePair.getResponseFuture();
                final TestRequest testRequest = requestResponseFuturePair.getTestRequest();
                assertResponseForRequest(responseFuture, testRequest);
            }
        });
    }

    public static TestMessageFunctionValidationBuilder expectTheErrorResponseToBeReceived() {
        return new TestMessageFunctionValidationBuilder(testEnvironment -> {
            ensureNoExceptionThrown(testEnvironment);
            final ResponseFuture<?> responseFuture = testEnvironment.getPropertyAsType(RESULT, ResponseFuture.class);
            final TestRequest testRequest = testEnvironment.getPropertyAsType(TEST_OBJECT, TestRequest.class);
            assertErrorResponse(responseFuture, testRequest);
        });
    }

    private static void assertErrorResponse(ResponseFuture<?> responseFuture, TestRequest testRequest) {
        assertFalse(responseFuture.wasSuccessful());
        try {
            final Object response = responseFuture.get();
            if (response instanceof ErrorTestResponse) {
                final ErrorTestResponse errorTestResponse = (ErrorTestResponse) response;
                final CorrelationId expectedCorrelationId = testRequest.getCorrelationId();
                assertThat(errorTestResponse.getCorrelationId(), equalTo(expectedCorrelationId));
            } else {
                fail("Unexpected Result in validation found.");
            }
        } catch (InterruptedException | ExecutionException e) {
            fail(e);
        }
    }


    public static TestMessageFunctionValidationBuilder expectTheFollowUpToBeExecuted() {
        return new TestMessageFunctionValidationBuilder(testEnvironment -> {
            ensureNoExceptionThrown(testEnvironment);
            final Object result = testEnvironment.getProperty(RESULT);
            final Object expectedResult = testEnvironment.getProperty(EXPECTED_RESULT);
            assertEquals(expectedResult, result);
        });
    }

    public static TestMessageFunctionValidationBuilder expectAExceptionToBeThrown() {
        return new TestMessageFunctionValidationBuilder(testEnvironment -> {
            final Exception exception = testEnvironment.getPropertyAsType(EXCEPTION, Exception.class);
            if (testEnvironment.has(EXPECTED_EXCEPTION_MESSAGE)) {
                final String expectedExceptionMessage = testEnvironment.getPropertyAsType(EXPECTED_EXCEPTION_MESSAGE, String.class);
                assertEquals(exception.getMessage(), expectedExceptionMessage);
            }
        });
    }

    public static TestMessageFunctionValidationBuilder expectAExceptionToBeThrownOfType(Class<?> expectedClass) {
        return new TestMessageFunctionValidationBuilder(testEnvironment -> {
            final Exception exception = testEnvironment.getPropertyAsType(EXCEPTION, Exception.class);
            assertEquals(exception.getClass(), expectedClass);
            if (testEnvironment.has(EXPECTED_EXCEPTION_MESSAGE)) {
                final String expectedExceptionMessage = testEnvironment.getPropertyAsType(EXPECTED_EXCEPTION_MESSAGE, String.class);
                assertEquals(exception.getMessage(), expectedExceptionMessage);
            }
        });
    }

    public static TestMessageFunctionValidationBuilder expectAFutureToBeFinishedWithException(Class<?> expectedClass) {
        return new TestMessageFunctionValidationBuilder(testEnvironment -> {
            final Exception exception = testEnvironment.getPropertyAsType(EXCEPTION, Exception.class);
            assertEquals(exception.getClass(), expectedClass);
            final ResponseFuture<?> responseFuture = testEnvironment.getPropertyAsType(RESULT, ResponseFuture.class);
            assertTrue(responseFuture.isDone());
            assertFalse(responseFuture.wasSuccessful());
            assertFalse(responseFuture.isCancelled());

            responseFuture.cancel(true);
            assertFalse(responseFuture.isCancelled());
            try {
                responseFuture.get();
            } catch (Exception e) {
                if (!(e instanceof ExecutionException)) {
                    fail("Unexpected Exception.", e);
                }
            }
        });
    }

    public static TestMessageFunctionValidationBuilder expectTheTimeoutToBeTriggered() {
        return new TestMessageFunctionValidationBuilder(testEnvironment -> {
            ensureNoExceptionThrown(testEnvironment);
            final long timeoutInMillis = testEnvironment.getPropertyAsType(RESULT, long.class);
            final long expectedTimeoutInMillis = testEnvironment.getPropertyAsType(EXPECTED_RESULT, long.class);
            final long difference = Math.abs(timeoutInMillis - expectedTimeoutInMillis);
            final long maximumAcceptedDifference = MILLISECONDS.toMillis(1);
            assertThat(difference, lessThanOrEqualTo(maximumAcceptedDifference));
        });
    }

    public static TestMessageFunctionValidationBuilder expectTheRequestToBeCancelledAndNoFollowUpActionToBeExecuted() {
        return new TestMessageFunctionValidationBuilder(testEnvironment -> {
            ensureNoExceptionThrown(testEnvironment);
            final ResponseFuture<?> responseFuture = testEnvironment.getPropertyAsType(RESULT, ResponseFuture.class);
            assertTrue(responseFuture.isCancelled());
            assertTrue(responseFuture.isDone());
            assertFalse(responseFuture.wasSuccessful());
            boolean cancellationExceptionThrownForGet = false;
            try {
                responseFuture.get();
            } catch (InterruptedException | ExecutionException e) {
                fail(e);
            } catch (CancellationException e) {
                cancellationExceptionThrownForGet = true;
            }
            assertTrue(cancellationExceptionThrownForGet);


            boolean cancellationExceptionThrownForGetWithTimeout = false;
            try {
                responseFuture.get(1000, SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                fail(e);
            } catch (CancellationException e) {
                cancellationExceptionThrownForGetWithTimeout = true;
            }
            assertTrue(cancellationExceptionThrownForGetWithTimeout);
        });
    }

    public static TestMessageFunctionValidationBuilder expectTheFutureToBeFulFilledOnlyOnce() {
        return new TestMessageFunctionValidationBuilder(testEnvironment -> {
            ensureNoExceptionThrown(testEnvironment);
            Object result = testEnvironment.getProperty(RESULT);
            if (result instanceof Exception) {
                assertResultOfClass(testEnvironment, TestException.class);
            } else {
                assertResultAndExpectedResultAreEqual(testEnvironment);
            }
        });
    }

    private static void ensureNoExceptionThrown(final TestEnvironment testEnvironment) {
        if (testEnvironment.has(EXCEPTION)) {
            final Exception exception = testEnvironment.getPropertyAsType(EXCEPTION, Exception.class);
            fail("Unexpected exception was thrown.", exception);
        }
    }

    public TestValidation build() {
        return testValidation;
    }
}
