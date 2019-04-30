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

package com.envimate.messageMate.useCases.singleEventParameter;

import com.envimate.messageMate.processingContext.EventType;
import com.envimate.messageMate.shared.config.AbstractTestConfigProvider;
import com.envimate.messageMate.useCases.shared.TestUseCase;
import com.envimate.messageMate.useCases.shared.TestUseCaseBuilder;

import static com.envimate.messageMate.shared.environment.TestEnvironmentProperty.RESULT;
import static com.envimate.messageMate.useCases.shared.UseCaseBusCallBuilder.aUseCasBusCall;
import static com.envimate.messageMate.useCases.singleEventParameter.SingleParameterEvent.singleParameterEvent;
import static com.envimate.messageMate.useCases.useCaseAdapter.UseCaseInvokingResponseEventType.USE_CASE_RESPONSE_EVENT_TYPE;

public class SingleEventParameterConfigurationResolver extends AbstractTestConfigProvider {
    private static final Class<SingleEventParameterUseCase> USE_CASE_CLASS = SingleEventParameterUseCase.class;
    private static final EventType EVENT_TYPE = EventType.eventTypeFromString("ExceptionThrowingUseCase");
    private static final String PARAMETER_MAP_PROPERTY_NAME = "int";
    private static final String RETURN_MAP_PROPERTY_NAME = "returnValue";

    @Override
    protected Class<?> forConfigClass() {
        return TestUseCase.class;
    }

    @Override
    protected Object testConfig() {
        final String expectedContent = "expected Response";
        return TestUseCaseBuilder.aTestUseCase()
                .forUseCaseClass(USE_CASE_CLASS)
                .forEventType(EVENT_TYPE)
                .withRequestMap(map -> map.put(PARAMETER_MAP_PROPERTY_NAME, expectedContent))
                .withAParameterSerialization(String.class, (string, map) -> map.put(RETURN_MAP_PROPERTY_NAME, string))
                .withAUseCaseInvocationRequestSerialization(SingleParameterEvent.class, (e, map) -> {
                    map.put(PARAMETER_MAP_PROPERTY_NAME, e.getMessage());
                })
                .withExpectedResponseMap(map -> map.put(RETURN_MAP_PROPERTY_NAME, expectedContent))
                .withParameterDeserialization(SingleParameterEvent.class, map -> {
                    return singleParameterEvent((String) map.get(PARAMETER_MAP_PROPERTY_NAME));
                })
                .withAUseCaseInvocationResponseDeserialization(String.class, map -> (String) map.get(RETURN_MAP_PROPERTY_NAME))
                .callingUseCaseWith((useCase, requestMap, responseMap) -> {
                    final SingleEventParameterUseCase singleEventParameterUseCase = (SingleEventParameterUseCase) useCase;
                    final String message = (String) requestMap.get(PARAMETER_MAP_PROPERTY_NAME);
                    final SingleParameterEvent request = singleParameterEvent(message);
                    final String returnValue = singleEventParameterUseCase.useCaseMethod(request);
                    responseMap.put(RETURN_MAP_PROPERTY_NAME, returnValue);
                })
                .instantiatingUseCaseWith(SingleEventParameterUseCase::new)
                .withSetup((messageBus, testEnvironment) -> {
                    messageBus.subscribe(USE_CASE_RESPONSE_EVENT_TYPE, s -> testEnvironment.setPropertyIfNotSet(RESULT, s));
                })
                .invokingOnTheUseCaseBusWith(aUseCasBusCall()
                        .withRequestData(singleParameterEvent(expectedContent))
                        .withSuccessResponseClass(String.class)
                        .withErrorResponseClass(Void.class)
                        .expectOnlySuccessPayload(expectedContent)
                        .expectedSuccessPayloadNotDeserialized(map -> map.put(RETURN_MAP_PROPERTY_NAME, expectedContent)))
                .build();
    }
}
