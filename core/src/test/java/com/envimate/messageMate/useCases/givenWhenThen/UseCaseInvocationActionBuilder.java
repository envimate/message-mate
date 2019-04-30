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
import com.envimate.messageMate.shared.givenWhenThen.TestAction;
import com.envimate.messageMate.shared.environment.TestEnvironment;
import com.envimate.messageMate.useCases.payloadAndErrorPayload.PayloadAndErrorPayload;
import com.envimate.messageMate.useCases.shared.TestUseCase;
import com.envimate.messageMate.useCases.shared.UseCaseBusCall;
import com.envimate.messageMate.useCases.useCaseAdapter.UseCaseInvokingResponseEventType;
import com.envimate.messageMate.useCases.useCaseBus.UseCaseBus;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static com.envimate.messageMate.shared.environment.TestEnvironmentProperty.*;
import static com.envimate.messageMate.shared.eventType.TestEventType.testEventType;
import static com.envimate.messageMate.useCases.givenWhenThen.UseCaseInvocationTestProperties.EVENT_TYPE;
import static com.envimate.messageMate.useCases.givenWhenThen.UseCaseInvocationTestProperties.MESSAGE_FUNCTION_USED;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class UseCaseInvocationActionBuilder {
    private final TestAction<TestUseCase> testAction;

    private static UseCaseInvocationActionBuilder asAction(final TestAction<TestUseCase> testAction) {
        return new UseCaseInvocationActionBuilder(testAction);
    }

    public static UseCaseInvocationActionBuilder theAssociatedEventIsSend() {
        return asAction((testUseCase, testEnvironment) -> {
            testEnvironment.setProperty(MESSAGE_FUNCTION_USED, false);
            final MessageBus messageBus = testEnvironment.getPropertyAsType(MOCK, MessageBus.class);
            testUseCase.performNecessaryResultSubscriptionsOn(messageBus, testEnvironment);
            final Object requestObject = testUseCase.getRequestObject(testEnvironment);
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
            messageBus.send(eventType, requestObject);
            return null;
        });
    }

    public static UseCaseInvocationActionBuilder anEventWithMissingMappingIsSend() {
        return asAction((testUseCase, testEnvironment) -> {
            testEnvironment.setProperty(MESSAGE_FUNCTION_USED, false);
            final MessageBus messageBus = testEnvironment.getPropertyAsType(MOCK, MessageBus.class);
            final Object requestObject = testUseCase.getRequestObject(testEnvironment);
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
            messageBus.subscribeRaw(UseCaseInvokingResponseEventType.USE_CASE_RESPONSE_EVENT_TYPE, processingContext -> {
                @SuppressWarnings("unchecked")
                final Map<String, Object> map = (Map<String, Object>) processingContext.getErrorPayload();
                testEnvironment.setPropertyIfNotSet(EXCEPTION, map.get("Exception"));
            });
            messageBus.send(eventType, requestObject);
            return null;
        });
    }

    public static UseCaseInvocationActionBuilder theRequestIsExecutedUsingAMessageFunction() {
        return asAction((testUseCase, testEnvironment) -> {
            testEnvironment.setProperty(MESSAGE_FUNCTION_USED, true);
            final MessageBus messageBus = testEnvironment.getPropertyAsType(MOCK, MessageBus.class);
            final Object requestObject = testUseCase.getRequestObject(testEnvironment);
            final MessageFunction messageFunction = MessageFunctionBuilder.aMessageFunction(messageBus);
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
            final ResponseFuture responseFuture = messageFunction.request(eventType, requestObject);
            testEnvironment.setProperty(RESULT, responseFuture);
            return null;
        });
    }

    public static UseCaseInvocationActionBuilder theRequestIsInvokedOnTheUseCaseBus() {
        return invokeOnTheUseCaseFullySerialized(UseCaseBus::invokeAndWait);
    }

    public static UseCaseInvocationActionBuilder theRequestIsInvokedOnTheUseCaseBusWithTimeout() {
        return invokeOnTheUseCaseFullySerialized((useCaseBus, eventType, data, payloadClass, errorPayloadClass) -> {
            final int timeout = 10;
            return useCaseBus.invokeAndWait(eventType, data, payloadClass, errorPayloadClass, timeout, MILLISECONDS);
        });
    }

    public static UseCaseInvocationActionBuilder theRequestIsInvokedOnTheUseCaseBusNotDeserialized() {
        return invokeOnTheUseCaseNotDeserialized((useCaseBus, eventType, data, payloadClass, errorPayloadClass) -> {
            return useCaseBus.invokeAndWaitNotDeserialized(eventType, data);
        });
    }

    public static UseCaseInvocationActionBuilder theRequestIsInvokedOnTheUseCaseBusNotDeserializedWithTimeout() {
        return invokeOnTheUseCaseNotDeserialized((useCaseBus, eventType, data, payloadClass, errorPayloadClass) -> {
            final int timeout = 10;
            return useCaseBus.invokeAndWaitNotDeserialized(eventType, data, timeout, MILLISECONDS);
        });
    }

    public static UseCaseInvocationActionBuilder invokeOnTheUseCaseFullySerialized(final UseCaseBusInvocation call) {
        return asAction((testUseCase, testEnvironment) -> {
            final UseCaseBusCall useCaseBusCall = testUseCase.getUseCaseBusCall();
            final PayloadAndErrorPayload<?, ?> expectedResult = useCaseBusCall.getExpectedResult();
            testEnvironment.setPropertyIfNotSet(EXPECTED_RESULT, expectedResult);
            invokeOnTheUseCase(testEnvironment, testUseCase, call);
            return null;
        });
    }

    public static UseCaseInvocationActionBuilder invokeOnTheUseCaseNotDeserialized(final UseCaseBusInvocation call) {
        return asAction((testUseCase, testEnvironment) -> {
            final UseCaseBusCall useCaseBusCall = testUseCase.getUseCaseBusCall();
            final PayloadAndErrorPayload<?, ?> expectedResult = useCaseBusCall.getNotDeserializedExpectedResult();
            testEnvironment.setPropertyIfNotSet(EXPECTED_RESULT, expectedResult);
            invokeOnTheUseCase(testEnvironment, testUseCase, call);
            return null;
        });
    }

    public static void invokeOnTheUseCase(final TestEnvironment testEnvironment,
                                          final TestUseCase testUseCase,
                                          final UseCaseBusInvocation call) {
        final UseCaseBus useCaseBus = testEnvironment.getPropertyAsType(SUT, UseCaseBus.class);
        final UseCaseBusCall useCaseBusCall = testUseCase.getUseCaseBusCall();
        final EventType eventType = useCaseBusCall.getEventType();
        final Object data = useCaseBusCall.getData();
        final Class<?> payloadClass = useCaseBusCall.getPayloadClass();
        final Class<?> errorPayloadClass = useCaseBusCall.getErrorPayloadClass();
        final PayloadAndErrorPayload<?, ?> result;
        try {
            result = call.invoke(useCaseBus, eventType, data, payloadClass, errorPayloadClass);
            testEnvironment.setPropertyIfNotSet(RESULT, result);

        } catch (final InterruptedException | ExecutionException | TimeoutException e) {
            testEnvironment.setPropertyIfNotSet(EXCEPTION, e);
        }
    }

    public TestAction<TestUseCase> build() {
        return testAction;
    }

    private interface UseCaseBusInvocation {
        PayloadAndErrorPayload<?, ?> invoke(
                UseCaseBus useCaseBus,
                EventType eventType,
                Object data, Class<?> payloadClass,
                Class<?> errorPayloadClass) throws InterruptedException, ExecutionException, TimeoutException;
    }
}
