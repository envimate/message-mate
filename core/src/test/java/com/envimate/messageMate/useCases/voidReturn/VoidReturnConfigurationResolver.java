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

package com.envimate.messageMate.useCases.voidReturn;

import com.envimate.messageMate.processingContext.EventType;
import com.envimate.messageMate.shared.config.AbstractTestConfigProvider;
import com.envimate.messageMate.shared.polling.PollingUtils;
import com.envimate.messageMate.useCases.shared.TestUseCase;
import com.envimate.messageMate.useCases.shared.TestUseCaseBuilder;

import java.util.Collections;
import java.util.Objects;
import java.util.function.Consumer;

import static com.envimate.messageMate.shared.environment.TestEnvironmentProperty.EXPECTED_RESULT;
import static com.envimate.messageMate.shared.environment.TestEnvironmentProperty.RESULT;
import static com.envimate.messageMate.useCases.givenWhenThen.UseCaseInvocationTestProperties.MESSAGE_FUNCTION_USED;
import static com.envimate.messageMate.useCases.shared.UseCaseBusCallBuilder.aUseCasBusCall;
import static com.envimate.messageMate.useCases.voidReturn.CallbackTestRequest.callbackTestRequest;

public class VoidReturnConfigurationResolver extends AbstractTestConfigProvider {
    public static final Class<?> USE_CASE_CLASS = VoidReturnUseCase.class;
    public static final EventType EVENT_TYPE = EventType.eventTypeFromString("ExceptionThrowingUseCase");
    private static final String PARAMETER_MAP_PROPERTY_NAME = "consumer";

    @Override
    protected Class<?> forConfigClass() {
        return TestUseCase.class;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Object testConfig() {
        return TestUseCaseBuilder.aTestUseCase()
                .forUseCaseClass(USE_CASE_CLASS)
                .forEventType(EVENT_TYPE)
                .withRequestProvider((testEnvironment, map) -> {
                    final String expectedResponse = "expected Response";
                    final Consumer<Object> consumer = o -> {
                        if (!testEnvironment.getPropertyAsType(MESSAGE_FUNCTION_USED, Boolean.class)) {
                            testEnvironment.setPropertyIfNotSet(EXPECTED_RESULT, expectedResponse);
                            testEnvironment.setPropertyIfNotSet(RESULT, expectedResponse);
                        }
                    };
                    map.put(PARAMETER_MAP_PROPERTY_NAME, consumer);
                    return map;
                })
                .withAParameterSerialization(Objects::isNull, (string, map) -> {
                })
                .withAParameterSerialization(CallbackTestRequest.class, (string, map) -> {
                })
                .withExpectedResponse((testEnvironment, map) -> {
                    if (testEnvironment.getPropertyAsType(MESSAGE_FUNCTION_USED, Boolean.class)) {
                        return Collections.emptyMap();
                    } else {
                        PollingUtils.pollUntil(() -> testEnvironment.has(EXPECTED_RESULT));
                        return testEnvironment.getProperty(EXPECTED_RESULT);
                    }
                })
                .withParameterDeserialization(CallbackTestRequest.class, map -> {
                    return callbackTestRequest((Consumer<Object>) map.get(PARAMETER_MAP_PROPERTY_NAME));
                })
                .withAUseCaseInvocationResponseDeserialization((aClass, map) -> aClass.equals(Void.class), map -> null)
                .callingUseCaseWith((useCase, requestMap, responseMap) -> {
                    final VoidReturnUseCase voidReturnUseCase = (VoidReturnUseCase) useCase;
                    final Consumer<Object> consumer = (Consumer<Object>) requestMap.get(PARAMETER_MAP_PROPERTY_NAME);
                    final CallbackTestRequest request = callbackTestRequest(consumer);
                    voidReturnUseCase.useCaseMethod(request);
                })
                .instantiatingUseCaseWith(VoidReturnUseCase::new)
                .withSetup((messageBus, testEnvironment) -> {
                })
                .invokingOnTheUseCaseBusWith(aUseCasBusCall()
                        .withRequestData(callbackTestRequest(null))
                        .withSuccessResponseClass(Void.class)
                        .withErrorResponseClass(Void.class)
                        .expectOnlySuccessPayload(null)
                        .expectedSuccessPayloadNotDeserialized(map -> {
                        }))
                .build();
    }

}
