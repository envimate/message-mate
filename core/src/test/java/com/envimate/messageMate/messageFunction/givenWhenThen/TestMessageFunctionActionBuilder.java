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


import com.envimate.messageMate.identification.CorrelationId;
import com.envimate.messageMate.messageBus.EventType;
import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.messageFunction.MessageFunction;
import com.envimate.messageMate.messageFunction.ResponseFuture;
import com.envimate.messageMate.messageFunction.testResponses.RequestResponseFuturePair;
import com.envimate.messageMate.messageFunction.testResponses.SimpleTestRequest;
import com.envimate.messageMate.messageFunction.testResponses.SimpleTestResponse;
import com.envimate.messageMate.messageFunction.testResponses.TestRequest;
import com.envimate.messageMate.qcec.shared.TestAction;
import com.envimate.messageMate.qcec.shared.TestEnvironmentProperty;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeoutException;

import static com.envimate.messageMate.messageBus.givenWhenThen.MessageBusTestProperties.EVENT_TYPE;
import static com.envimate.messageMate.messageFunction.givenWhenThen.MessageFunctionTestProperties.CANCEL_RESULTS;
import static com.envimate.messageMate.messageFunction.testResponses.RequestResponseFuturePair.requestResponseFuturePair;
import static com.envimate.messageMate.messageFunction.testResponses.SimpleTestRequest.testRequest;
import static com.envimate.messageMate.messageFunction.testResponses.SimpleTestResponse.testResponse;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.*;
import static com.envimate.messageMate.shared.TestEventType.differentTestEventType;
import static com.envimate.messageMate.shared.TestEventType.testEventType;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class TestMessageFunctionActionBuilder {
    private final TestAction<MessageFunction> testAction;

    public static TestMessageFunctionActionBuilder aRequestIsSend() {
        return new TestMessageFunctionActionBuilder((messageFunction, testEnvironment) -> {
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
            final SimpleTestRequest testRequest = testRequest();
            testEnvironment.setProperty(TEST_OBJECT, testRequest);
            return messageFunction.request(eventType, testRequest);
        });
    }

    public static TestMessageFunctionActionBuilder severalRequestsAreSend() {
        return new TestMessageFunctionActionBuilder((messageFunction, testEnvironment) -> {
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
            final List<RequestResponseFuturePair> requestResponsePairs = new LinkedList<>();
            final int numberOfRequests = 5;
            for (int i = 0; i < numberOfRequests; i++) {
                final SimpleTestRequest testRequest = testRequest();
                final ResponseFuture responseFuture = messageFunction.request(eventType, testRequest);
                final RequestResponseFuturePair requestResponseFuturePair = requestResponseFuturePair(testRequest, responseFuture);
                requestResponsePairs.add(requestResponseFuturePair);
            }
            return requestResponsePairs;
        });
    }

    public static TestMessageFunctionActionBuilder twoRequestsAreSendThatWithOneOfEachResponsesAnswered() {
        return new TestMessageFunctionActionBuilder((messageFunction, testEnvironment) -> {
            final MessageBus messageBus = testEnvironment.getPropertyAsType(MOCK, MessageBus.class);

            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
            final EventType answerEventType = differentTestEventType();
            messageBus.subscribeRaw(eventType, processingContext -> {
                final CorrelationId wrongCorrelationId = CorrelationId.newUniqueCorrelationId();
                final SimpleTestResponse wrongResponse = testResponse(null);
                messageBus.send(answerEventType, wrongResponse, wrongCorrelationId);

                final CorrelationId correlationId = processingContext.generateCorrelationIdForAnswer();
                final TestRequest payload = (TestRequest) processingContext.getPayload();
                final SimpleTestResponse testResponse = testResponse(payload);
                messageBus.send(answerEventType, testResponse, correlationId);
            });

            final SimpleTestRequest testRequest = testRequest();
            testEnvironment.setProperty(TEST_OBJECT, testRequest);
            return messageFunction.request(eventType, testRequest);
        });
    }

    public static TestMessageFunctionActionBuilder aFollowUpActionIsAddedBeforeSend() {
        return new TestMessageFunctionActionBuilder((messageFunction, testEnvironment) -> {
            final SimpleTestRequest testRequest = testRequest();
            testEnvironment.setProperty(TEST_OBJECT, testRequest);
            final String expectedResult = "success";
            testEnvironment.setProperty(EXPECTED_RESULT, expectedResult);
            final MessageBus messageBus = testEnvironment.getPropertyAsType(TestEnvironmentProperty.MOCK, MessageBus.class);
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
            messageBus.subscribeRaw(eventType, processingContext -> {
                final SimpleTestResponse testResponse = testResponse(testRequest);
                final CorrelationId correlationId = processingContext.generateCorrelationIdForAnswer();
                final EventType answerEventType = differentTestEventType();
                messageBus.send(answerEventType, testResponse, correlationId);
            });
            final ResponseFuture responseFuture = messageFunction.request(eventType, testRequest);
            responseFuture.then((testResponse, exception) -> testEnvironment.setProperty(RESULT, expectedResult));
            return null;
        });
    }

    public static TestMessageFunctionActionBuilder aFollowUpActionExecutingOnlyOnceIsAddedBeforeRequest() {
        return new TestMessageFunctionActionBuilder((messageFunction, testEnvironment) -> {
            final SimpleTestRequest testRequest = SimpleTestRequest.testRequest();
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
            final ResponseFuture request = messageFunction.request(eventType, testRequest);

            request.then((response, exception) -> {
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
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
            final ResponseFuture responseFuture = messageFunction.request(eventType, testRequest);
            responseFuture.then((testResponse, exception) -> {
                testEnvironment.setProperty(EXCEPTION, exception);
            });
            return null;
        });
    }

    public static TestMessageFunctionActionBuilder aRequestIsSendThatCausesADeliveryFailedMessage() {
        return new TestMessageFunctionActionBuilder((messageFunction, testEnvironment) -> {
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
            final SimpleTestRequest request = SimpleTestRequest.testRequest();
            final ResponseFuture responseFuture = messageFunction.request(eventType, request);
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
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
            final ResponseFuture responseFuture = messageFunction.request(eventType, testRequest);
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

    public static TestMessageFunctionActionBuilder aRequestIsCancelledSeveralTimes() {
        return callCancelXTimes(5);
    }

    private static TestMessageFunctionActionBuilder callCancelXTimes(final int cancelCalls) {
        return new TestMessageFunctionActionBuilder((messageFunction, testEnvironment) -> {
            final SimpleTestRequest testRequest = SimpleTestRequest.testRequest();
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
            final ResponseFuture responseFuture = messageFunction.request(eventType, testRequest);
            responseFuture.then((testResponse, exception) -> {
                throw new RuntimeException("This FollowUpActionShouldNotBeCalled");
            });
            for (int i = 0; i < cancelCalls; i++) {
                final boolean cancelResult = responseFuture.cancel(true);
                testEnvironment.addToListProperty(CANCEL_RESULTS, cancelResult);
            }
            final MessageBus messageBus = testEnvironment.getPropertyAsType(MOCK, MessageBus.class);
            messageBus.send(eventType, testResponse(testRequest));
            return responseFuture;
        });
    }

    public static TestMessageFunctionActionBuilder theFutureIsFulfilledAndThenCancelled() {
        return new TestMessageFunctionActionBuilder((messageFunction, testEnvironment) -> {
            final SimpleTestRequest testRequest = SimpleTestRequest.testRequest();
            final MessageBus messageBus = testEnvironment.getPropertyAsType(MOCK, MessageBus.class);
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
            messageBus.subscribeRaw(eventType, processingContext -> {
                final SimpleTestResponse response = testResponse(testRequest);
                final CorrelationId correlationId = processingContext.generateCorrelationIdForAnswer();
                final EventType answerEventType = differentTestEventType();
                messageBus.send(answerEventType, response, correlationId);
            });
            final ResponseFuture responseFuture = messageFunction.request(eventType, testRequest);
            try {
                MILLISECONDS.sleep(10);
            } catch (final InterruptedException e) {
                testEnvironment.setProperty(EXCEPTION, e);
            }
            final boolean cancelResult = responseFuture.cancel(true);
            testEnvironment.setProperty(CANCEL_RESULTS, cancelResult);
            return responseFuture;
        });
    }

    public static TestMessageFunctionActionBuilder aResponseToACancelledRequestDoesNotExecuteFollowUpAction() {
        return new TestMessageFunctionActionBuilder((messageFunction, testEnvironment) -> {
            final SimpleTestRequest testRequest = SimpleTestRequest.testRequest();
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
            final ResponseFuture responseFuture = messageFunction.request(eventType, testRequest);
            responseFuture.then((testResponse, exception) -> {
                throw new RuntimeException("This FollowUpActionShouldNotBeCalled");
            });
            final boolean cancelResult = responseFuture.cancel(true);
            testEnvironment.addToListProperty(CANCEL_RESULTS, cancelResult);
            final MessageBus messageBus = testEnvironment.getPropertyAsType(MOCK, MessageBus.class);
            final SimpleTestResponse response = testResponse(testRequest);
            final EventType answerEventType = differentTestEventType();
            messageBus.send(answerEventType, response);
            try {
                MILLISECONDS.sleep(10);
            } catch (final InterruptedException e) {
                testEnvironment.setProperty(EXCEPTION, e);
            }
            return responseFuture;
        });
    }

    public static TestMessageFunctionActionBuilder theResultOfACancelledRequestIsTaken() {
        return new TestMessageFunctionActionBuilder((messageFunction, testEnvironment) -> {
            final SimpleTestRequest testRequest = SimpleTestRequest.testRequest();
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
            final ResponseFuture responseFuture = messageFunction.request(eventType, testRequest);
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
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
            final ResponseFuture responseFuture = messageFunction.request(eventType, testRequest);
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

    public static TestMessageFunctionActionBuilder aFollowUpActionIsAddedToACancelledFuture() {
        return new TestMessageFunctionActionBuilder((messageFunction, testEnvironment) -> {
            final SimpleTestRequest testRequest = SimpleTestRequest.testRequest();
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
            final ResponseFuture responseFuture = messageFunction.request(eventType, testRequest);
            responseFuture.cancel(true);
            try {
                responseFuture.then((response, exception) -> {
                    throw new UnsupportedOperationException();
                });
            } catch (final Exception e) {
                testEnvironment.setProperty(EXCEPTION, e);
            }
            return responseFuture;
        });
    }

    public TestAction<MessageFunction> build() {
        return testAction;
    }

}
