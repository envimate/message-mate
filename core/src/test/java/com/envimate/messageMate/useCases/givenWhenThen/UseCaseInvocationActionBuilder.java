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

import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.messageFunction.MessageFunction;
import com.envimate.messageMate.messageFunction.MessageFunctionBuilder;
import com.envimate.messageMate.messageFunction.ResponseFuture;
import com.envimate.messageMate.processingContext.EventType;
import com.envimate.messageMate.shared.environment.TestEnvironment;
import com.envimate.messageMate.shared.givenWhenThen.TestAction;
import com.envimate.messageMate.useCases.payloadAndErrorPayload.PayloadAndErrorPayload;
import com.envimate.messageMate.useCases.shared.RequestExpectedResultTuple;
import com.envimate.messageMate.useCases.shared.UseCaseInvocationConfiguration;
import com.envimate.messageMate.useCases.useCaseBus.UseCaseBus;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static com.envimate.messageMate.shared.environment.TestEnvironmentProperty.*;
import static com.envimate.messageMate.shared.eventType.TestEventType.testEventType;
import static com.envimate.messageMate.shared.properties.SharedTestProperties.EVENT_TYPE;
import static com.envimate.messageMate.useCases.shared.UseCaseInvocationTestProperties.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class UseCaseInvocationActionBuilder {
    public static final int TIMEOUT_IN_MILLISECONDS = 100;
    private final TestAction<UseCaseInvocationConfiguration> testAction;

    private static UseCaseInvocationActionBuilder asAction(final TestAction<UseCaseInvocationConfiguration> testAction) {
        return new UseCaseInvocationActionBuilder(testAction);
    }

    public static UseCaseInvocationActionBuilder theAssociatedEventIsSend() {
        return asAction((invocationConfiguration, testEnvironment) -> {
            testEnvironment.setProperty(MESSAGE_FUNCTION_USED, false);
            final UseCaseBus useCaseBus = testEnvironment.getPropertyAsType(USE_CASE_BUS, UseCaseBus.class);
            final RequestExpectedResultTuple requestExpResultTuple = invocationConfiguration.createRequestExpectedResultTuple();
            testEnvironment.setPropertyIfNotSet(REQUEST_EXPECTED_RESULT_TUPLE, requestExpResultTuple);
            final EventType eventType = invocationConfiguration.getEventTypeUseCaseIsRegisteredFor();
            final Object requestObject = requestExpResultTuple.getRequestObject();
            final Object expectedResult = requestExpResultTuple.getExpectedResult();
            final Class<?> payloadClass = determinePayloadClass(expectedResult);
            try {
                final Class<?> expectedErrorPayloadClass =
                        determineExpectedErrorPayloadClass(requestExpResultTuple, testEnvironment);
                final PayloadAndErrorPayload<?, ?> result = useCaseBus.invokeAndWait(eventType, requestObject,
                        payloadClass, expectedErrorPayloadClass, TIMEOUT_IN_MILLISECONDS, MILLISECONDS);
                testEnvironment.setPropertyIfNotSet(RESULT, result);
            } catch (final InterruptedException | ExecutionException | TimeoutException e) {
                testEnvironment.setPropertyIfNotSet(EXCEPTION, e);
            }
            return null;
        });
    }

    private static Class<?> determinePayloadClass(final Object expectedResult) {
        if (expectedResult != null) {
            return expectedResult.getClass();
        } else {
            return null;
        }
    }

    private static Class<?> determineExpectedErrorPayloadClass(final RequestExpectedResultTuple requestExpResultTuple,
                                                               final TestEnvironment testEnvironment) {
        final Class<?> expectedErrorPayloadClass;
        if (testEnvironment.has(EXPECTED_ERROR_PAYLOAD_CLASS)) {
            expectedErrorPayloadClass = testEnvironment.getPropertyAsType(EXPECTED_ERROR_PAYLOAD_CLASS, Class.class);
        } else if (requestExpResultTuple.isResultInErrorPayload()) {
            final Object expectedResult = requestExpResultTuple.getExpectedResult();
            expectedErrorPayloadClass = expectedResult.getClass();
        } else {
            expectedErrorPayloadClass = null;
        }
        return expectedErrorPayloadClass;
    }

    public static UseCaseInvocationActionBuilder anEventWithMissingMappingIsSend() {
        return theAssociatedEventIsSend();
    }

    public static UseCaseInvocationActionBuilder theRequestIsExecutedUsingAMessageFunction() {
        return asAction((invocationConfiguration, testEnvironment) -> {
            testEnvironment.setProperty(MESSAGE_FUNCTION_USED, true);
            final MessageBus messageBus = testEnvironment.getPropertyAsType(MOCK, MessageBus.class);
            final MessageFunction messageFunction = MessageFunctionBuilder.aMessageFunction(messageBus);
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
            final RequestExpectedResultTuple requestExpResultTuple =
                    invocationConfiguration.createSerializedRequestExpectedResultTuple();
            final Object requestObject = requestExpResultTuple.getRequestObject();
            final ResponseFuture responseFuture = messageFunction.request(eventType, requestObject);
            testEnvironment.setPropertyIfNotSet(RESULT, responseFuture);
            testEnvironment.setPropertyIfNotSet(REQUEST_EXPECTED_RESULT_TUPLE, requestExpResultTuple);
            return null;
        });
    }

    public TestAction<UseCaseInvocationConfiguration> build() {
        return testAction;
    }

}
