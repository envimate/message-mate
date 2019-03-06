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
import com.envimate.messageMate.messageFunction.MessageFunction;
import com.envimate.messageMate.messageFunction.ResponseFuture;
import com.envimate.messageMate.messageFunction.testResponses.*;
import com.envimate.messageMate.qcec.shared.TestAction;
import com.envimate.messageMate.qcec.shared.TestEnvironmentProperty;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import static com.envimate.messageMate.messageFunction.testResponses.RequestResponseFuturePair.requestResponseFuturePair;
import static com.envimate.messageMate.messageFunction.testResponses.SimpleTestRequest.testRequest;
import static com.envimate.messageMate.messageFunction.testResponses.SimpleTestResponse.testResponse;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class TestMessageFunctionActionBuilder {
    private final TestAction<MessageFunction<TestRequest, TestResponse>> testAction;

    public static TestMessageFunctionActionBuilder aRequestIsSend() {
        return new TestMessageFunctionActionBuilder((messageFunction, testEnvironment) -> {
            final SimpleTestRequest testRequest = testRequest();
            testEnvironment.setProperty(TEST_OBJECT, testRequest);
            return messageFunction.request(testRequest);
        });
    }

    public static TestMessageFunctionActionBuilder severalRequestsAreSend() {
        return new TestMessageFunctionActionBuilder((messageFunction, testEnvironment) -> {
            final List<RequestResponseFuturePair> requestResponsePairs = new LinkedList<>();
            final int numberOfRequests = 5;
            for (int i = 0; i < numberOfRequests; i++) {
                final SimpleTestRequest testRequest = testRequest();
                final ResponseFuture<TestResponse> responseFuture = messageFunction.request(testRequest);
                final RequestResponseFuturePair requestResponseFuturePair = requestResponseFuturePair(testRequest, responseFuture);
                requestResponsePairs.add(requestResponseFuturePair);
            }
            return requestResponsePairs;
        });
    }

    public static TestMessageFunctionActionBuilder twoRequestsAreSendThatWithOneOfEachResponsesAnswered() {
        return new TestMessageFunctionActionBuilder((messageFunction, testEnvironment) -> {
            final ExpectedRequestResponsePair expectedRequestResponsePair1 = ExpectedRequestResponsePair.generateNewPair();
            final ExpectedRequestResponsePair expectedRequestResponsePair2 = ExpectedRequestResponsePair.generateNewPairWithAlternativeResponse();
            final List<ExpectedRequestResponsePair> expectedResponsePairs = Arrays.asList(
                    expectedRequestResponsePair1,
                    expectedRequestResponsePair2);
            final MessageBus messageBus = testEnvironment.getPropertyAsType(MOCK, MessageBus.class);
            messageBus.subscribe(SimpleTestRequest.class, new ExpectedResponseSubscriber<>(expectedResponsePairs, messageBus));
            final List<RequestResponseFuturePair> requestResponseFuturePairs = new ArrayList<>();
            for (final ExpectedRequestResponsePair expectedResponsePair : expectedResponsePairs) {
                final TestRequest request = expectedResponsePair.request;
                final ResponseFuture<TestResponse> responseFuture = messageFunction.request(request);
                final RequestResponseFuturePair requestResponseFuturePair = RequestResponseFuturePair.requestResponseFuturePair(request, responseFuture);
                requestResponseFuturePairs.add(requestResponseFuturePair);
            }
            return requestResponseFuturePairs;
        });
    }

    public static TestMessageFunctionActionBuilder aRequestResultingInErrorIsSend() {
        return new TestMessageFunctionActionBuilder((messageFunction, testEnvironment) -> {
            final SimpleTestRequest testRequest = testRequest();
            testEnvironment.setProperty(TEST_OBJECT, testRequest);
            return messageFunction.request(testRequest);
        });
    }

    public static TestMessageFunctionActionBuilder aMatchingAndOneNotMatchingGeneralErrorResponseIsSend() {
        return new TestMessageFunctionActionBuilder((messageFunction, testEnvironment) -> {
            final SimpleTestRequest testRequest = testRequest();
            testEnvironment.setProperty(TEST_OBJECT, testRequest);
            return messageFunction.request(testRequest);
        });
    }

    public static TestMessageFunctionActionBuilder aFollowUpActionIsAddedBeforeSend() {
        return new TestMessageFunctionActionBuilder((messageFunction, testEnvironment) -> {
            final SimpleTestRequest testRequest = testRequest();
            testEnvironment.setProperty(TEST_OBJECT, testRequest);
            final String expectedResult = "success";
            testEnvironment.setProperty(EXPECTED_RESULT, expectedResult);
            final ResponseFuture<TestResponse> responseFuture = messageFunction.request(testRequest);
            responseFuture.then((testResponse, wasSuccessful, exception) -> testEnvironment.setProperty(RESULT, expectedResult));
            final MessageBus messageBus = testEnvironment.getPropertyAsType(TestEnvironmentProperty.MOCK, MessageBus.class);
            final SimpleTestResponse testResponse = testResponse(testRequest.getCorrelationId());
            messageBus.send(testResponse);
            return null;
        });
    }

    public static TestMessageFunctionActionBuilder aFollowUpActionExecutingOnlyOnceIsAddedBeforeRequest() {
        return new TestMessageFunctionActionBuilder((messageFunction, testEnvironment) -> {
            final SimpleTestRequest testRequest = SimpleTestRequest.testRequest();
            final ResponseFuture<TestResponse> request = messageFunction.request(testRequest);

            request.then((response, wasSuccessful, exception) -> {
                if (!testEnvironment.has(RESULT)) {
                    if (exception != null) {
                        testEnvironment.setProperty(RESULT, exception);
                    } else {
                        testEnvironment.setProperty(RESULT, response);
                    }
                } else {
                    testEnvironment.setProperty(EXCEPTION, new RuntimeException("FollowUp called twice"));
                }
            });
            return null;
        });
    }

    public static TestMessageFunctionActionBuilder aFollowUpActionForAnExceptionIsAdded() {
        return new TestMessageFunctionActionBuilder((messageFunction, testEnvironment) -> {
            final SimpleTestRequest testRequest = testRequest();
            testEnvironment.setProperty(TEST_OBJECT, testRequest);
            final ResponseFuture<TestResponse> responseFuture = messageFunction.request(testRequest);
            responseFuture.then((testResponse, wasSuccessful, exception) -> {
                testEnvironment.setProperty(EXCEPTION, exception);
            });
            return null;
        });
    }

    public static TestMessageFunctionActionBuilder aFollowUpActionWithExceptionIsAddedBeforeSend() {
        return new TestMessageFunctionActionBuilder((messageFunction, testEnvironment) -> {
            final SimpleTestRequest testRequest = testRequest();
            final String expectedExceptionMessage = "Expected exception message.";
            testEnvironment.setProperty(EXPECTED_EXCEPTION_MESSAGE, expectedExceptionMessage);
            final ResponseFuture<TestResponse> responseFuture = messageFunction.request(testRequest);
            try {
                responseFuture.then((testResponse, wasSuccessful, exception) -> {
                    throw new RuntimeException(expectedExceptionMessage);
                });
            } catch (final Exception e) {
                testEnvironment.setProperty(EXCEPTION, e);
            }
            return null;
        });
    }

    public static TestMessageFunctionActionBuilder aRequestIsSendThatCausesADeliveryFailedMessage() {
        return new TestMessageFunctionActionBuilder((messageFunction, testEnvironment) -> {
            final ResponseFuture<TestResponse> responseFuture = messageFunction.request(SimpleTestRequest.testRequest());
            try {
                responseFuture.get();
            } catch (final Exception e) {
                testEnvironment.setProperty(EXCEPTION, e);
                return responseFuture;
            }
            return responseFuture;
        });
    }

    public static TestMessageFunctionActionBuilder forTheResponseIsWaitedASpecificTime() {
        return new TestMessageFunctionActionBuilder((messageFunction, testEnvironment) -> {
            final SimpleTestRequest testRequest = testRequest();
            final long expectedTimeout = MILLISECONDS.toMillis(100);
            testEnvironment.setProperty(EXPECTED_RESULT, expectedTimeout);
            final ResponseFuture<TestResponse> responseFuture = messageFunction.request(testRequest);
            final long timeoutStart = System.currentTimeMillis();
            try {
                responseFuture.get(100, MILLISECONDS);
                throw new RuntimeException("Future should not return a value");
            } catch (final InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            } catch (final TimeoutException e) {
                final long timeoutEnd = System.currentTimeMillis();
                final long duration = timeoutEnd - timeoutStart;
                return duration;
            }
        });
    }

    public static TestMessageFunctionActionBuilder aRequestIsCancelled() {
        return callCancelXTimes(1);
    }

    public static TestMessageFunctionActionBuilder aRequestCanBeCancelledMoreThanOnce() {
        return callCancelXTimes(5);
    }

    private static TestMessageFunctionActionBuilder callCancelXTimes(final int cancelCalls) {
        return new TestMessageFunctionActionBuilder((messageFunction, testEnvironment) -> {
            final SimpleTestRequest testRequest = SimpleTestRequest.testRequest();
            final ResponseFuture<TestResponse> responseFuture = messageFunction.request(testRequest);
            responseFuture.then((testResponse, wasSuccessful, exception) -> {
                throw new RuntimeException("This FollowUpActionShouldNotBeCalled");
            });
            for (int i = 0; i < cancelCalls; i++) {
                responseFuture.cancel(true);
            }
            final MessageBus messageBus = testEnvironment.getPropertyAsType(MOCK, MessageBus.class);
            messageBus.send(testResponse(testRequest.getCorrelationId()));
            return responseFuture;
        });
    }

    public static TestMessageFunctionActionBuilder theResultOfACancelledRequestIsTaken() {
        return new TestMessageFunctionActionBuilder((messageFunction, testEnvironment) -> {
            final SimpleTestRequest testRequest = SimpleTestRequest.testRequest();
            final ResponseFuture<TestResponse> responseFuture = messageFunction.request(testRequest);
            responseFuture.cancel(true);
            try {
                responseFuture.get();
            } catch (final Exception e) {
                testEnvironment.setProperty(EXCEPTION, e);
            }
            return responseFuture;
        });
    }

    public static TestMessageFunctionActionBuilder aRequestsIsCancelledWhileOtherThreadsWait() {
        return new TestMessageFunctionActionBuilder((messageFunction, testEnvironment) -> {
            final SimpleTestRequest testRequest = SimpleTestRequest.testRequest();
            final ResponseFuture<TestResponse> responseFuture = messageFunction.request(testRequest);
            final Semaphore waitingSemaphoreGet = new Semaphore(0);
            final Semaphore waitingSemaphoreGetTimeout = new Semaphore(0);
            new Thread(() -> {
                boolean interruptedExceptionCalled = false;
                try {
                    responseFuture.get();
                    final RuntimeException exception = new RuntimeException("Future should not return result;");
                    testEnvironment.setProperty(EXCEPTION, exception);
                } catch (InterruptedException e) {
                    interruptedExceptionCalled = true;
                } catch (ExecutionException e) {
                    testEnvironment.setProperty(EXCEPTION, e);
                }
                if (!interruptedExceptionCalled) {
                    final RuntimeException exception = new RuntimeException("Future should wake waiting threads with InterruptedException");
                    testEnvironment.setProperty(EXCEPTION, exception);
                }
                waitingSemaphoreGet.release();
            }).start();

            new Thread(() -> {
                boolean interruptedExceptionCalled = false;
                try {
                    responseFuture.get(1000, SECONDS);
                    final RuntimeException exception = new RuntimeException("Future should not return result;");
                    testEnvironment.setProperty(EXCEPTION, exception);
                } catch (InterruptedException e) {
                    interruptedExceptionCalled = true;
                } catch (ExecutionException | TimeoutException e) {
                    testEnvironment.setProperty(EXCEPTION, e);
                }
                if (!interruptedExceptionCalled) {
                    final RuntimeException exception = new RuntimeException("Future should wake waiting threads with InterruptedException");
                    testEnvironment.setProperty(EXCEPTION, exception);
                }
                waitingSemaphoreGetTimeout.release();
            }).start();
            try {
                MILLISECONDS.sleep(20);
            } catch (final InterruptedException e) {
                throw new RuntimeException(e);
            }
            responseFuture.cancel(true);
            try {
                waitingSemaphoreGet.acquire();
                waitingSemaphoreGetTimeout.acquire();
            } catch (final InterruptedException e) {
                throw new RuntimeException(e);
            }
            return responseFuture;
        });
    }


    public TestAction<MessageFunction<TestRequest, TestResponse>> build() {
        return testAction;
    }

    @RequiredArgsConstructor(access = PRIVATE)
    private static final class ExpectedResponseSubscriber<T> implements Consumer<T> {
        private final List<ExpectedRequestResponsePair> expectedRequestResponsePairs;
        private final MessageBus messageBus;

        @Override
        public void accept(final T o) {
            for (final ExpectedRequestResponsePair expectedResponsePair : expectedRequestResponsePairs) {
                if (o.equals(expectedResponsePair.request)) {
                    messageBus.send(expectedResponsePair.response);
                }
            }
        }
    }
}
