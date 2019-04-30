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

package com.envimate.messageMate.useCases.primitiveReturnType;

import com.envimate.messageMate.processingContext.EventType;
import com.envimate.messageMate.shared.config.AbstractTestConfigProvider;
import com.envimate.messageMate.useCases.shared.TestUseCase;
import com.envimate.messageMate.useCases.shared.TestUseCaseBuilder;

import static com.envimate.messageMate.shared.environment.TestEnvironmentProperty.RESULT;
import static com.envimate.messageMate.useCases.shared.UseCaseBusCallBuilder.aUseCasBusCall;
import static com.envimate.messageMate.useCases.useCaseAdapter.UseCaseInvokingResponseEventType.USE_CASE_RESPONSE_EVENT_TYPE;

public class PrimitiveReturnTypeConfigurationResolver extends AbstractTestConfigProvider {
    private static final Class<PrimitiveReturnTypeUseCase> USE_CASE_CLASS = PrimitiveReturnTypeUseCase.class;
    private static final EventType EVENT_TYPE = EventType.eventTypeFromString("PrimitiveReturnTypeUseCase");
    private static final String PARAMETER_MAP_PROPERTY_NAME = "int";
    private static final String RETURN_MAP_PROPERTY_NAME = "returnValue";

    @Override
    protected Class<?> forConfigClass() {
        return TestUseCase.class;
    }

    @Override
    protected Object testConfig() {
        final int expectedIntValue = 5;
        return TestUseCaseBuilder.aTestUseCase()
                .forUseCaseClass(USE_CASE_CLASS)
                .forEventType(EVENT_TYPE)
                .withRequestMap(map -> map.put(PARAMETER_MAP_PROPERTY_NAME, expectedIntValue))
                .withAParameterSerialization(Integer.class, (intValue, map) -> map.put(RETURN_MAP_PROPERTY_NAME, intValue))
                .withAUseCaseInvocationRequestSerialization(PrimitiveReturnTypeRequest.class, (r, map) -> {
                    map.put(PARAMETER_MAP_PROPERTY_NAME, r.getValue());
                })
                .withExpectedResponseMap(map -> map.put(RETURN_MAP_PROPERTY_NAME, expectedIntValue))
                .withParameterDeserialization(PrimitiveReturnTypeRequest.class, map -> {
                    return new PrimitiveReturnTypeRequest((int) map.get(PARAMETER_MAP_PROPERTY_NAME));
                })
                .withAUseCaseInvocationResponseDeserialization(Integer.class, map -> (Integer) map.get(RETURN_MAP_PROPERTY_NAME))
                .callingUseCaseWith((useCase, requestMap, responseMap) -> {
                    final PrimitiveReturnTypeUseCase primitiveReturnTypeUseCase = (PrimitiveReturnTypeUseCase) useCase;
                    final int intParameter = (int) requestMap.get(PARAMETER_MAP_PROPERTY_NAME);
                    final PrimitiveReturnTypeRequest request = new PrimitiveReturnTypeRequest(intParameter);
                    final int returnValue = primitiveReturnTypeUseCase.useCaseMethod(request);
                    responseMap.put(RETURN_MAP_PROPERTY_NAME, returnValue);
                })
                .instantiatingUseCaseWith(PrimitiveReturnTypeUseCase::new)
                .withSetup((messageBus, testEnvironment) -> {
                    messageBus.subscribe(USE_CASE_RESPONSE_EVENT_TYPE, v -> testEnvironment.setPropertyIfNotSet(RESULT, v));
                })
                .invokingOnTheUseCaseBusWith(aUseCasBusCall()
                        .withRequestData(new PrimitiveReturnTypeRequest(expectedIntValue))
                        .withSuccessResponseClass(Integer.class)
                        .withErrorResponseClass(Void.class)
                        .expectOnlySuccessPayload(expectedIntValue)
                        .expectedSuccessPayloadNotDeserialized(map -> map.put(RETURN_MAP_PROPERTY_NAME, expectedIntValue)))
                .build();
    }
}
