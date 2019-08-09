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

package com.envimate.messageMate.useCases.specialInvocations;

import com.envimate.messageMate.configuration.AsynchronousConfiguration;
import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.messageBus.givenWhenThen.MessageBusTestExceptionHandler;
import com.envimate.messageMate.processingContext.EventType;
import com.envimate.messageMate.shared.environment.TestEnvironment;
import com.envimate.messageMate.useCases.useCaseAdapter.UseCaseInvocationBuilder;
import com.envimate.messageMate.useCases.useCaseAdapter.usecaseInstantiating.UseCaseInstantiator;
import com.envimate.messageMate.useCases.useCaseBus.UseCaseBus;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.Objects;

import static com.envimate.messageMate.configuration.AsynchronousConfiguration.constantPoolSizeAsynchronousConfiguration;
import static com.envimate.messageMate.messageBus.MessageBusBuilder.aMessageBus;
import static com.envimate.messageMate.messageBus.MessageBusType.ASYNCHRONOUS;
import static com.envimate.messageMate.processingContext.EventType.eventTypeFromString;
import static com.envimate.messageMate.shared.environment.TestEnvironmentProperty.*;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class SpecialInvocationUseCaseBuilder {
    private final TestEnvironment testEnvironment;

    public static SpecialInvocationUseCaseBuilder aUseCaseAdapter() {
        final TestEnvironment testEnvironment = TestEnvironment.emptyTestEnvironment();
        return new SpecialInvocationUseCaseBuilder(testEnvironment);
    }

    public SpecialInvocationUseCaseBuilder forAnUseCaseThrowingAnExceptionDuringInitialization(
            final RuntimeException exceptionToThrow) {
        final MessageBus messageBus = asynchronousMessageBus();
        final EventType type = eventTypeFromString("Test");
        final UseCaseBus useCaseBus = UseCaseInvocationBuilder.anUseCaseAdapter()
                .invokingUseCase(ExceptionDuringInitializationUseCase.class).forType(type).callingTheSingleUseCaseMethod()
                .obtainingUseCaseInstancesUsing(new UseCaseInstantiator() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public <T> T instantiate(final Class<T> type) {
                        return (T) ExceptionDuringInitializationUseCase.init(exceptionToThrow);
                    }
                })
                .throwAnExceptionByDefaultIfNoParameterMappingCanBeApplied()
                .serializingResponseObjectsThat(Objects::isNull).using(object -> Collections.emptyMap())
                .throwingAnExceptionByDefaultIfNoResponseMappingCanBeApplied()
                .throwingAnExceptionIfNoExceptionMappingCanBeFound()
                .build(messageBus);
        testEnvironment.setPropertyIfNotSet(MOCK, messageBus);
        testEnvironment.setPropertyIfNotSet(SUT, useCaseBus);
        testEnvironment.setPropertyIfNotSet(TEST_OBJECT, type);
        return this;
    }

    public SpecialInvocationUseCaseBuilder forAnUseCaseThrowingAnExceptionDuringStaticInitializer() {
        final MessageBus messageBus = asynchronousMessageBus();
        final EventType type = eventTypeFromString("Test");
        final UseCaseBus useCaseBus = UseCaseInvocationBuilder.anUseCaseAdapter()
                .invokingUseCase(ExceptionInStaticInitializerUseCase.class).forType(type).callingTheSingleUseCaseMethod()
                .obtainingUseCaseInstancesUsingTheZeroArgumentConstructor()
                .throwAnExceptionByDefaultIfNoParameterMappingCanBeApplied()
                .serializingResponseObjectsThat(Objects::isNull).using(object -> Collections.emptyMap())
                .throwingAnExceptionByDefaultIfNoResponseMappingCanBeApplied()
                .throwingAnExceptionIfNoExceptionMappingCanBeFound()
                .build(messageBus);
        testEnvironment.setPropertyIfNotSet(MOCK, messageBus);
        testEnvironment.setPropertyIfNotSet(SUT, useCaseBus);
        testEnvironment.setPropertyIfNotSet(TEST_OBJECT, type);
        return this;
    }

    private MessageBus asynchronousMessageBus() {
        final int poolSize = 3;
        final AsynchronousConfiguration asynchronousConfiguration = AsynchronousConfiguration.constantPoolSizeAsynchronousConfiguration(poolSize);
        return aMessageBus()
                .forType(ASYNCHRONOUS)
                .withAsynchronousConfiguration(asynchronousConfiguration)
                .withExceptionHandler(MessageBusTestExceptionHandler.allExceptionIgnoringExceptionHandler())
                .build();
    }

    public TestEnvironment build() {
        return testEnvironment;
    }
}
