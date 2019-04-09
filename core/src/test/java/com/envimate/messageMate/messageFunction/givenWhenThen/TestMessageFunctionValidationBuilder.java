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

import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.messageBus.MessageBusStatusInformation;
import com.envimate.messageMate.messageBus.exception.MessageBusExceptionListener;
import com.envimate.messageMate.messageFunction.ResponseFuture;
import com.envimate.messageMate.messageFunction.testResponses.RequestResponseFuturePair;
import com.envimate.messageMate.messageFunction.testResponses.SimpleErrorResponse;
import com.envimate.messageMate.messageFunction.testResponses.TestRequest;
import com.envimate.messageMate.messageFunction.testResponses.TestResponse;
import com.envimate.messageMate.processingContext.ProcessingContext;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.qcec.shared.TestValidation;
import com.envimate.messageMate.shared.subscriber.TestException;
import com.envimate.messageMate.subscribing.Subscriber;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static com.envimate.messageMate.messageFunction.givenWhenThen.MessageFunctionTestProperties.*;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.*;
import static com.envimate.messageMate.shared.validations.SharedTestValidations.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static lombok.AccessLevel.PRIVATE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.*;

@RequiredArgsConstructor(access = PRIVATE)
public final class TestMessageFunctionValidationBuilder {
    private final TestValidation testValidation;

    public static TestMessageFunctionValidationBuilder expectTheResponseToBeReceived() {
        return new TestMessageFunctionValidationBuilder(testEnvironment -> {
            ensureNoExceptionThrown(testEnvironment);
            final ResponseFuture responseFuture = testEnvironment.getPropertyAsType(RESULT, ResponseFuture.class);
            final TestRequest testRequest = testEnvironment.getPropertyAsType(TEST_OBJECT, TestRequest.class);
            assertResponseForRequest(responseFuture, testRequest);
        });
    }

    public static TestMessageFunctionValidationBuilder expectCorrectTheResponseToBeReceived() {
        return expectTheResponseToBeReceived();
    }

    public static TestMessageFunctionValidationBuilder expectTheFutureToHaveAccessToTheErrorResponse() {
        return new TestMessageFunctionValidationBuilder(testEnvironment -> {
            ensureNoExceptionThrown(testEnvironment);
            final ResponseFuture responseFuture = testEnvironment.getPropertyAsType(RESULT, ResponseFuture.class);
            final TestRequest testRequest = testEnvironment.getPropertyAsType(TEST_OBJECT, TestRequest.class);
            assertErrorResponseForRequest(responseFuture, testRequest);
        });
    }

    public static TestMessageFunctionValidationBuilder expectTheProcessingContextToBeReceived() {
        return new TestMessageFunctionValidationBuilder(testEnvironment -> {
            ensureNoExceptionThrown(testEnvironment);
            final ResponseFuture responseFuture = testEnvironment.getPropertyAsType(RESULT, ResponseFuture.class);
            assertCorrectResponseProcessingContext(responseFuture, testEnvironment);
        });
    }

    private static void assertResponseForRequest(final ResponseFuture responseFuture, final TestRequest testRequest) {
        assertTrue(responseFuture.wasSuccessful());
        try {
            final Object response = responseFuture.get();
            if (response instanceof TestResponse) {
                final TestResponse testResponse = (TestResponse) response;
                final Object request = testResponse.getCorrelatedRequest();
                assertThat(request, equalTo(testRequest));
            } else {
                fail("Unexpected Result in validation found.");
            }
        } catch (final InterruptedException | ExecutionException e) {
            fail(e);
        }
    }

    private static void assertErrorResponseForRequest(final ResponseFuture responseFuture, final TestRequest testRequest) {
        assertFalse(responseFuture.wasSuccessful());
        try {
            final Object response = responseFuture.getErrorResponse();
            if (response instanceof SimpleErrorResponse) {
                final TestResponse testResponse = (TestResponse) response;
                final Object request = testResponse.getCorrelatedRequest();
                assertThat(request, equalTo(testRequest));
            } else {
                fail("Unexpected Result in validation found.");
            }
        } catch (final InterruptedException | ExecutionException e) {
            fail(e);
        }
    }

    private static void assertCorrectResponseProcessingContext(final ResponseFuture responseFuture, final TestEnvironment testEnvironment) {
        @SuppressWarnings("unchecked")
        final ProcessingContext<Object> expectedProcessingContext = (ProcessingContext<Object>) testEnvironment.getProperty(RESPONSE_PROCESSING_CONTEXT);
        final Object errorPayload = expectedProcessingContext.getErrorPayload();
        final boolean wasSuccessfull = errorPayload == null;
        assertThat(responseFuture.wasSuccessful(), equalTo(wasSuccessfull));
        try {
            final Object response = responseFuture.getRaw();
            assertThat(response, equalTo(expectedProcessingContext));
        } catch (final InterruptedException | ExecutionException e) {
            fail(e);
        }
    }


    public static TestMessageFunctionValidationBuilder expectCorrectResponseReceivedForEachRequest() {
        return new TestMessageFunctionValidationBuilder(testEnvironment -> {
            ensureNoExceptionThrown(testEnvironment);
            @SuppressWarnings("unchecked")
            final List<RequestResponseFuturePair> requestResponseFuturePairs = (List<RequestResponseFuturePair>) testEnvironment.getProperty(RESULT);
            for (final RequestResponseFuturePair requestResponseFuturePair : requestResponseFuturePairs) {
                final ResponseFuture responseFuture = requestResponseFuturePair.getResponseFuture();
                final TestRequest testRequest = requestResponseFuturePair.getTestRequest();
                assertResponseForRequest(responseFuture, testRequest);
            }
        });
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

    public static TestMessageFunctionValidationBuilder expectAExceptionToBeThrownOfType(final Class<?> expectedClass) {
        return new TestMessageFunctionValidationBuilder(testEnvironment -> {
            final Exception exception = testEnvironment.getPropertyAsType(EXCEPTION, Exception.class);
            assertEquals(exception.getClass(), expectedClass);
            if (testEnvironment.has(EXPECTED_EXCEPTION_MESSAGE)) {
                final String expectedExceptionMessage = testEnvironment.getPropertyAsType(EXPECTED_EXCEPTION_MESSAGE, String.class);
                assertEquals(exception.getMessage(), expectedExceptionMessage);
            }
        });
    }

    public static TestMessageFunctionValidationBuilder expectAFutureToBeFinishedWithException(final Class<?> expectedClass) {
        return new TestMessageFunctionValidationBuilder(testEnvironment -> {
            final Exception exception = testEnvironment.getPropertyAsType(EXCEPTION, Exception.class);
            assertEquals(exception.getClass(), expectedClass);
            final ResponseFuture responseFuture = testEnvironment.getPropertyAsType(RESULT, ResponseFuture.class);
            assertTrue(responseFuture.isDone());
            assertFalse(responseFuture.wasSuccessful());
            assertFalse(responseFuture.isCancelled());

            responseFuture.cancel(true);
            assertFalse(responseFuture.isCancelled());
            try {
                responseFuture.get();
            } catch (final Exception e) {
                if (!(e instanceof ExecutionException)) {
                    fail("Unexpected Exception.", e);
                }
            }
        });
    }

    public static TestMessageFunctionValidationBuilder expectTheTimeoutToBeTriggeredAtTheCorrectTime() {
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
            final ResponseFuture responseFuture = testEnvironment.getPropertyAsType(RESULT, ResponseFuture.class);
            assertTrue(responseFuture.isCancelled());
            assertTrue(responseFuture.isDone());
            assertFalse(responseFuture.wasSuccessful());
            assertFutureAllowsNotGettingValue(responseFuture);
        });
    }

    public static TestMessageFunctionValidationBuilder expectAllCancellationsToHaveReturnedTheSameResult() {
        return new TestMessageFunctionValidationBuilder(testEnvironment -> {
            ensureNoExceptionThrown(testEnvironment);
            final ResponseFuture responseFuture = testEnvironment.getPropertyAsType(RESULT, ResponseFuture.class);
            assertCancellationRequestsSucceeded(testEnvironment);
            assertTrue(responseFuture.isCancelled());
            assertTrue(responseFuture.isDone());
            assertFalse(responseFuture.wasSuccessful());
            assertFutureAllowsNotGettingValue(responseFuture);
        });
    }

    public static TestMessageFunctionValidationBuilder expectTheCancellationToFailed() {
        return new TestMessageFunctionValidationBuilder(testEnvironment -> {
            ensureNoExceptionThrown(testEnvironment);
            final ResponseFuture responseFuture = testEnvironment.getPropertyAsType(RESULT, ResponseFuture.class);
            final Boolean cancelResults = testEnvironment.getPropertyAsType(CANCEL_RESULTS, Boolean.class);
            assertFalse(cancelResults);
            assertFalse(responseFuture.isCancelled());
            assertTrue(responseFuture.isDone());
            assertTrue(responseFuture.wasSuccessful());
        });
    }

    @SuppressWarnings("unchecked")
    private static void assertCancellationRequestsSucceeded(final TestEnvironment testEnvironment) {
        final List<Boolean> cancelResults = (List<Boolean>) testEnvironment.getProperty(CANCEL_RESULTS);
        for (final Boolean cancelResult : cancelResults) {
            assertTrue(cancelResult);
        }
    }

    private static void assertFutureAllowsNotGettingValue(final ResponseFuture responseFuture) {
        boolean cancellationExceptionThrownForGet = false;
        try {
            responseFuture.get();
        } catch (final InterruptedException | ExecutionException e) {
            fail(e);
        } catch (final CancellationException e) {
            cancellationExceptionThrownForGet = true;
        }
        assertTrue(cancellationExceptionThrownForGet);


        boolean cancellationExceptionThrownForGetWithTimeout = false;
        try {
            responseFuture.get(1000, SECONDS);
        } catch (final InterruptedException | ExecutionException | TimeoutException e) {
            fail(e);
        } catch (final CancellationException e) {
            cancellationExceptionThrownForGetWithTimeout = true;
        }
        assertTrue(cancellationExceptionThrownForGetWithTimeout);
    }

    public static TestMessageFunctionValidationBuilder expectTheFutureToBeFulFilledOnlyOnce() {
        return new TestMessageFunctionValidationBuilder(testEnvironment -> {
            ensureNoExceptionThrown(testEnvironment);
            final Object result = testEnvironment.getProperty(RESULT);
            if (result instanceof Exception) {
                assertResultOfClass(testEnvironment, TestException.class);
            } else {
                assertResultAndExpectedResultAreEqual(testEnvironment);
            }
        });
    }

    public static TestMessageFunctionValidationBuilder expectTheExceptionToBeSetOnlyDuringByFuture() {
        return new TestMessageFunctionValidationBuilder(testEnvironment -> {
            expectAExceptionToBeThrownOfType(TestException.class);
            assertPropertyFalseOrUnset(testEnvironment, EXCEPTION_OCCURRED_DURING_SEND);
            assertPropertyTrue(testEnvironment, EXCEPTION_OCCURRED_DURING_FOLLOW_UP);
        });
    }

    public static TestMessageFunctionValidationBuilder expectNoUnecssarySubscribersOnTheMessageBus() {
        return new TestMessageFunctionValidationBuilder(testEnvironment -> {
            final MessageBus messageBus = testEnvironment.getPropertyAsType(MOCK, MessageBus.class);
            final MessageBusStatusInformation statusInformation = messageBus.getStatusInformation();
            final List<Subscriber<?>> allSubscribers = statusInformation.getAllSubscribers();
            final int initialResponseSubscriber = 1;
            assertCollectionOfSize(allSubscribers, initialResponseSubscriber);
            final List<MessageBusExceptionListener<?>> listener = statusInformation.getAllExceptionListener();
            final int noRemainingErrorListener = 0;
            assertCollectionOfSize(listener, noRemainingErrorListener);
        });
    }

    public static TestMessageFunctionValidationBuilder expectNullReceived() {
        return new TestMessageFunctionValidationBuilder(testEnvironment -> {
            ensureNoExceptionThrown(testEnvironment);
            try {
                final ResponseFuture responseFuture = testEnvironment.getPropertyAsType(RESULT, ResponseFuture.class);
                final Object response = responseFuture.get();
                assertThat(response, nullValue());
            } catch (final InterruptedException | ExecutionException e) {
                fail(e);
            }
        });
    }


    private static void ensureNoExceptionThrown(final TestEnvironment testEnvironment) {
        if (testEnvironment.has(EXCEPTION)) {
            final Exception exception = testEnvironment.getPropertyAsType(EXCEPTION, Exception.class);
            fail("Unexpected exception was thrown.", exception);
        }
    }

    public TestMessageFunctionValidationBuilder and(final TestMessageFunctionValidationBuilder other) {
        return new TestMessageFunctionValidationBuilder(testValidation -> {
            this.testValidation.validate(testValidation);
            other.testValidation.validate(testValidation);
        });
    }

    public TestValidation build() {
        return testValidation;
    }

}
