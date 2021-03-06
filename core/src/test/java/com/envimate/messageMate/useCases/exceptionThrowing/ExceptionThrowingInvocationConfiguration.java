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

package com.envimate.messageMate.useCases.exceptionThrowing;

import com.envimate.messageMate.processingContext.EventType;
import com.envimate.messageMate.shared.exceptions.TestException;
import com.envimate.messageMate.useCases.building.*;
import com.envimate.messageMate.useCases.givenWhenThen.DeAndSerializationDefinition;
import com.envimate.messageMate.useCases.shared.RequestExpectedResultTuple;
import com.envimate.messageMate.useCases.shared.UseCaseInvocationConfiguration;
import com.envimate.messageMate.useCases.useCaseAdapter.usecaseInstantiating.UseCaseInstantiator;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.UUID;

import static com.envimate.messageMate.processingContext.EventType.eventTypeFromString;
import static com.envimate.messageMate.shared.exceptions.TestException.testException;
import static com.envimate.messageMate.useCases.exceptionThrowing.ExceptionThrowingRequest.exceptionThrowingRequest;
import static com.envimate.messageMate.useCases.shared.RequestExpectedResultTuple.requestExpectedErrorResultTuple;
import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
public class ExceptionThrowingInvocationConfiguration implements UseCaseInvocationConfiguration {
    private static final String PARAMETER_MAP_PROPERTY_NAME = "value";
    private static final String RETURN_MAP_PROPERTY_NAME = "returnValue";

    @Override
    public Class<?> getUseCaseClass() {
        return ExceptionThrowingUseCase.class;
    }

    @Override
    public EventType getEventTypeUseCaseIsRegisteredFor() {
        return eventTypeFromString("ExceptionThrowingUseCase");
    }

    @Override
    public DeAndSerializationDefinition<RequestSerializationStep1Builder> getRequestSerializationDefinitions() {
        return requestSerializationStep1Builder -> {
            requestSerializationStep1Builder.serializingUseCaseRequestOntoTheBusOfType(ExceptionThrowingRequest.class)
                    .using(object -> {
                        final ExceptionThrowingRequest request = (ExceptionThrowingRequest) object;
                        final RuntimeException exception = request.getExceptionToThrow();
                        final String message = exception.getMessage();
                        return Map.of(PARAMETER_MAP_PROPERTY_NAME, message);
                    });
        };
    }

    @Override
    public DeAndSerializationDefinition<RequestDeserializationStep1Builder> getRequestDeserializationDefinitions() {
        return requestDeserializationStep1Builder -> {
            requestDeserializationStep1Builder.deserializingRequestsToUseCaseParametersOfType(ExceptionThrowingRequest.class)
                    .using((targetType, map) -> {
                        final String message = (String) map.get(PARAMETER_MAP_PROPERTY_NAME);
                        final TestException exceptionToThrow = testException(message);
                        return exceptionThrowingRequest(exceptionToThrow);
                    });
        };
    }

    @Override
    public DeAndSerializationDefinition<ResponseSerializationStep1Builder> getResponseSerializationDefinitions() {
        return responseSerializationStep1Builder -> {
            //use case method never terminates normally as always an exception is thrown
        };
    }

    @Override
    public DeAndSerializationDefinition<ExceptionSerializationStep1Builder> getExceptionsSerializationDefinitions() {
        return exceptionSerializationStep1Builder -> {
            exceptionSerializationStep1Builder.serializingExceptionsOfType(TestException.class)
                    .using(e -> Map.of(RETURN_MAP_PROPERTY_NAME, e.getMessage()));
        };
    }

    @Override
    public DeAndSerializationDefinition<ResponseDeserializationStep1Builder> getResponseDeserializationDefinitions() {
        return responseDeserializationStep1Builder -> {
            responseDeserializationStep1Builder.deserializingUseCaseResponsesOfType(TestException.class)
                    .using((targetType, map) -> testException((String) map.get(RETURN_MAP_PROPERTY_NAME)));
        };
    }

    @Override
    public RequestExpectedResultTuple createRequestExpectedResultTuple() {
        final String message = UUID.randomUUID().toString();
        final TestException exception = testException(message);
        final ExceptionThrowingRequest request = exceptionThrowingRequest(exception);
        return requestExpectedErrorResultTuple(request, exception);
    }

    @Override
    public RequestExpectedResultTuple createSerializedRequestExpectedResultTuple() {
        final String message = UUID.randomUUID().toString();
        final Map<String, String> requestObject = Map.of(PARAMETER_MAP_PROPERTY_NAME, message);
        final Map<String, String> expectedResult = Map.of(RETURN_MAP_PROPERTY_NAME, message);
        return requestExpectedErrorResultTuple(requestObject, expectedResult);
    }

    @SuppressWarnings("unchecked")
    @Override
    public InstantiationBuilder applyCustomUseCaseMethodCallingConfiguration(final Step3Builder<?> step3Builder) {
        return step3Builder.callingBy((useCase, event, callingContext) -> {
            final Map<String, String> requestMap = (Map<String, String>) event;
            final String message = requestMap.get(PARAMETER_MAP_PROPERTY_NAME);
            final TestException exception = testException(message);
            final ExceptionThrowingRequest request = exceptionThrowingRequest(exception);

            final ExceptionThrowingUseCase exceptionThrowingUseCase = (ExceptionThrowingUseCase) useCase;
            exceptionThrowingUseCase.useCaseMethod(request);
            return Map.of("INVALID_RESPONSE", "");
        });
    }

    @Override
    public RequestSerializationStep1Builder applyCustomUseCaseInstantiationConfiguration(
            final InstantiationBuilder instantiationBuilder) {
        return instantiationBuilder.obtainingUseCaseInstancesUsing(new UseCaseInstantiator() {
            @SuppressWarnings("unchecked")
            @Override
            public <T> T instantiate(final Class<T> type) {
                return (T) new ExceptionThrowingUseCase();
            }
        });
    }
}
