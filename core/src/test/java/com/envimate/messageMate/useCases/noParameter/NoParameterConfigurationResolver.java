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

package com.envimate.messageMate.useCases.noParameter;

import com.envimate.messageMate.processingContext.EventType;
import com.envimate.messageMate.shared.config.AbstractTestConfigProvider;
import com.envimate.messageMate.useCases.shared.TestUseCase;
import com.envimate.messageMate.useCases.shared.TestUseCaseBuilder;

import java.util.Objects;

import static com.envimate.messageMate.shared.environment.TestEnvironmentProperty.RESULT;
import static com.envimate.messageMate.useCases.shared.UseCaseBusCallBuilder.aUseCasBusCall;
import static com.envimate.messageMate.useCases.noParameter.NoParameterUseCase.NO_PARAMETER_USE_CASE_RETURN_VALUE;
import static com.envimate.messageMate.useCases.useCaseAdapter.UseCaseInvokingResponseEventType.USE_CASE_RESPONSE_EVENT_TYPE;

public class NoParameterConfigurationResolver extends AbstractTestConfigProvider {
    public static final Class<?> USE_CASE_CLASS = NoParameterUseCase.class;
    public static final EventType EVENT_TYPE = EventType.eventTypeFromString("NoParameterUseCase");
    private static final String RETURN_MAP_PROPERTY_NAME = "returnValue";

    @Override
    protected Class<?> forConfigClass() {
        return TestUseCase.class;
    }

    @Override
    protected Object testConfig() {
        return TestUseCaseBuilder.aTestUseCase()
                .forUseCaseClass(USE_CASE_CLASS)
                .forEventType(EVENT_TYPE)
                .withRequestMap(map -> {
                })
                .withAParameterSerialization(String.class, (string, map) -> map.put(RETURN_MAP_PROPERTY_NAME, string))
                .withAUseCaseInvocationRequestSerialization(Objects::isNull, (map, o) -> {
                })
                .withExpectedResponseMap(map -> map.put(RETURN_MAP_PROPERTY_NAME, NO_PARAMETER_USE_CASE_RETURN_VALUE))
                .withAUseCaseInvocationResponseDeserialization(String.class, map -> (String) map.get(RETURN_MAP_PROPERTY_NAME))
                .callingUseCaseWith((useCase, requestMap, responseMap) -> {
                    final NoParameterUseCase noParameterUseCase = (NoParameterUseCase) useCase;
                    final String returnValue = noParameterUseCase.useCaseMethod();
                    responseMap.put(RETURN_MAP_PROPERTY_NAME, returnValue);
                })
                .instantiatingUseCaseWith(NoParameterUseCase::new)
                .withSetup((messageBus, testEnvironment) -> {
                    messageBus.subscribe(USE_CASE_RESPONSE_EVENT_TYPE, s -> testEnvironment.setPropertyIfNotSet(RESULT, s));
                })

                .invokingOnTheUseCaseBusWith(aUseCasBusCall()
                        .withRequestData(null)
                        .withSuccessResponseClass(String.class)
                        .withErrorResponseClass(Void.class)
                        .expectOnlySuccessPayload(NO_PARAMETER_USE_CASE_RETURN_VALUE)
                        .expectedSuccessPayloadNotDeserialized(map -> {
                            map.put(RETURN_MAP_PROPERTY_NAME, NO_PARAMETER_USE_CASE_RETURN_VALUE);
                        }))
                .build();
    }
}
