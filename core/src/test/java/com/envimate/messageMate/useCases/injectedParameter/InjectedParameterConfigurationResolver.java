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

package com.envimate.messageMate.useCases.injectedParameter;

import com.envimate.messageMate.processingContext.EventType;
import com.envimate.messageMate.shared.config.AbstractTestConfigProvider;
import com.envimate.messageMate.useCases.shared.TestUseCase;
import com.envimate.messageMate.useCases.shared.TestUseCaseBuilder;
import com.envimate.messageMate.useCases.singleEventParameter.SingleEventParameterUseCase;
import com.envimate.messageMate.useCases.singleEventParameter.SingleParameterEvent;

import static com.envimate.messageMate.shared.environment.TestEnvironmentProperty.RESULT;
import static com.envimate.messageMate.useCases.injectedParameter.InjectedParameter.injectedParameter;
import static com.envimate.messageMate.useCases.injectedParameter.NormalParameter.normalParameter;
import static com.envimate.messageMate.useCases.shared.UseCaseBusCallBuilder.aUseCasBusCall;
import static com.envimate.messageMate.useCases.singleEventParameter.SingleParameterEvent.singleParameterEvent;
import static com.envimate.messageMate.useCases.useCaseAdapter.UseCaseInvokingResponseEventType.USE_CASE_RESPONSE_EVENT_TYPE;

public class InjectedParameterConfigurationResolver extends AbstractTestConfigProvider {
    private static final Class<InjectedParameterUseCase> USE_CASE_CLASS = InjectedParameterUseCase.class;
    private static final EventType EVENT_TYPE = EventType.eventTypeFromString("InjectedParameterUseCase");
    private static final String PARAMETER_MAP_PROPERTY_NAME = "value";
    private static final String RETURN_MAP_PROPERTY_NAME = "returnValue";

    @Override
    protected Class<?> forConfigClass() {
        return TestUseCase.class;
    }

    @Override
    protected Object testConfig() {
        final String normalMessage = "normal Message";
        final String injectedMessage = "injected Message";
        final String expectedContent = normalMessage + injectedMessage;
        return TestUseCaseBuilder.aTestUseCase()
                .forUseCaseClass(USE_CASE_CLASS)
                .forEventType(EVENT_TYPE)
                .withRequestMap(map -> map.put(PARAMETER_MAP_PROPERTY_NAME, normalMessage))
                .withAParameterSerialization(String.class, (string, map) -> map.put(RETURN_MAP_PROPERTY_NAME, string))
                .withAUseCaseInvocationRequestSerialization(NormalParameter.class, (e, map) -> {
                    map.put(PARAMETER_MAP_PROPERTY_NAME, e.getMessage());
                })
                .withExpectedResponseMap(map -> map.put(RETURN_MAP_PROPERTY_NAME, expectedContent))
                .withParameterDeserialization(NormalParameter.class, map -> {
                    return normalParameter((String) map.get(PARAMETER_MAP_PROPERTY_NAME));
                })
                .withAUseCaseInvocationResponseDeserialization(String.class, map -> (String) map.get(RETURN_MAP_PROPERTY_NAME))
                .callingUseCaseWith((useCase, requestMap, responseMap) -> {
                    final InjectedParameterUseCase singleEventParameterUseCase = (InjectedParameterUseCase) useCase;
                    final String message = (String) requestMap.get(PARAMETER_MAP_PROPERTY_NAME);
                    final NormalParameter normalParameter = normalParameter(message);
                    final InjectedParameter injectedParameter = injectedParameter(injectedMessage);
                    final String returnValue = singleEventParameterUseCase.useCaseMethod(normalParameter, injectedParameter);
                    responseMap.put(RETURN_MAP_PROPERTY_NAME, returnValue);
                })
                .instantiatingUseCaseWith(InjectedParameterUseCase::new)
                .withSetup((messageBus, testEnvironment) -> {
                    messageBus.subscribe(USE_CASE_RESPONSE_EVENT_TYPE, s -> testEnvironment.setPropertyIfNotSet(RESULT, s));
                })
                .injectingParameter(injectionStepBuilder -> {
                    injectionStepBuilder.injectParameterForClass(InjectedParameter.class, stringStringMap -> {
                        return InjectedParameter.injectedParameter(injectedMessage);
                    });
                })
                .invokingOnTheUseCaseBusWith(aUseCasBusCall()
                        .withRequestData(normalParameter(normalMessage))
                        .withSuccessResponseClass(String.class)
                        .withErrorResponseClass(Void.class)
                        .expectOnlySuccessPayload(expectedContent)
                        .expectedSuccessPayloadNotDeserialized(map -> map.put(RETURN_MAP_PROPERTY_NAME, expectedContent)))
                .build();
    }
}
