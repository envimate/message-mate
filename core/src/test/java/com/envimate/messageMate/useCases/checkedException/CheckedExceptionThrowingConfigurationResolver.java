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

package com.envimate.messageMate.useCases.checkedException;

import com.envimate.messageMate.channel.Channel;
import com.envimate.messageMate.messageBus.exception.MessageBusExceptionHandler;
import com.envimate.messageMate.processingContext.EventType;
import com.envimate.messageMate.processingContext.ProcessingContext;
import com.envimate.messageMate.shared.config.AbstractTestConfigProvider;
import com.envimate.messageMate.useCases.shared.TestUseCase;
import com.envimate.messageMate.useCases.shared.TestUseCaseBuilder;
import lombok.RequiredArgsConstructor;

import static com.envimate.messageMate.mapping.ExceptionMapifier.DEFAULT_EXCEPTION_MAPIFIER_KEY;
import static com.envimate.messageMate.shared.environment.TestEnvironmentProperty.RESULT;
import static com.envimate.messageMate.useCases.givenWhenThen.UseCaseInvocationTestProperties.RETRIEVE_ERROR_FROM_FUTURE;
import static com.envimate.messageMate.useCases.shared.UseCaseBusCallBuilder.aUseCasBusCall;
import static com.envimate.messageMate.useCases.useCaseAdapter.UseCaseInvokingResponseEventType.USE_CASE_RESPONSE_EVENT_TYPE;
import static lombok.AccessLevel.PRIVATE;

public class CheckedExceptionThrowingConfigurationResolver extends AbstractTestConfigProvider {
    public static final Class<?> USE_CASE_CLASS = CheckedExceptionThrowingUseCase.class;
    public static final EventType EVENT_TYPE = EventType.eventTypeFromString("CheckedExceptionThrowingUseCase");
    private static final String PARAMETER_MAP_PROPERTY_NAME = "Exception";

    @Override
    protected Class<?> forConfigClass() {
        return TestUseCase.class;
    }

    @Override
    protected Object testConfig() {
        final CheckedTestException expectedException = new CheckedTestException();
        return TestUseCaseBuilder.aTestUseCase()
                .forUseCaseClass(USE_CASE_CLASS)
                .forEventType(EVENT_TYPE)
                .withRequestMap(map -> map.put(PARAMETER_MAP_PROPERTY_NAME, expectedException))
                .withAUseCaseInvocationRequestSerialization(CheckedExceptionThrowingRequest.class, (e, map) -> {
                    map.put(PARAMETER_MAP_PROPERTY_NAME, e.getExceptionToThrow());
                })
                .withExpectedResponseMap(map -> map.put(PARAMETER_MAP_PROPERTY_NAME, expectedException))
                .withParameterDeserialization(CheckedExceptionThrowingRequest.class, map -> {
                    return new CheckedExceptionThrowingRequest((CheckedTestException) map.get(PARAMETER_MAP_PROPERTY_NAME));
                })
                .withAUseCaseInvocationResponseDeserialization(CheckedTestException.class, map -> {
                    return (CheckedTestException) map.get(DEFAULT_EXCEPTION_MAPIFIER_KEY);
                })
                .callingUseCaseWith((useCase, requestMap, responseMap) -> {
                    final CheckedExceptionThrowingUseCase exceptionUseCase = (CheckedExceptionThrowingUseCase) useCase;
                    final CheckedTestException exception = (CheckedTestException) requestMap.get(PARAMETER_MAP_PROPERTY_NAME);
                    final CheckedExceptionThrowingRequest request = new CheckedExceptionThrowingRequest(exception);
                    exceptionUseCase.useCaseMethod(request);
                })
                .instantiatingUseCaseWith(CheckedExceptionThrowingUseCase::new)
                .withSetup((messageBus, testEnvironment) -> {
                    messageBus.subscribeRaw(USE_CASE_RESPONSE_EVENT_TYPE, processingContext -> {
                        testEnvironment.setPropertyIfNotSet(RESULT, processingContext.getErrorPayload());
                    });
                })
                .withMessageBusEnhancer((messageBusBuilder, testEnvironment) -> {
                    testEnvironment.setPropertyIfNotSet(RETRIEVE_ERROR_FROM_FUTURE, true);
                    messageBusBuilder
                            .withExceptionHandler(new ExpectedExceptionAllowMessageBusExceptionHandler(expectedException));
                })
                .invokingOnTheUseCaseBusWith(aUseCasBusCall()
                        .withRequestData(new CheckedExceptionThrowingRequest(expectedException))
                        .withSuccessResponseClass(Void.class)
                        .withErrorResponseClass(CheckedTestException.class)
                        .expectOnlyErrorPayload(expectedException)
                        .expectedErrorPayloadNotDeserialized(map -> map.put(DEFAULT_EXCEPTION_MAPIFIER_KEY, expectedException)))
                .build();
    }

    @RequiredArgsConstructor(access = PRIVATE)
    private static final class ExpectedExceptionAllowMessageBusExceptionHandler implements MessageBusExceptionHandler {
        private final Exception expectedException;

        @Override
        public boolean shouldDeliveryChannelErrorBeHandledAndDeliveryAborted(final ProcessingContext<Object> message,
                                                                             final Exception e,
                                                                             final Channel<Object> channel) {
            return true;
        }

        @Override
        public void handleDeliveryChannelException(final ProcessingContext<Object> message,
                                                   final Exception e,
                                                   final Channel<Object> channel) {
            rethrowUnexpectedException(e);
        }

        @Override
        public void handleFilterException(final ProcessingContext<Object> message,
                                          final Exception e,
                                          final Channel<Object> channel) {
            rethrowUnexpectedException(e);
        }

        private void rethrowUnexpectedException(final Exception e) {
            if (e != expectedException) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                } else {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
