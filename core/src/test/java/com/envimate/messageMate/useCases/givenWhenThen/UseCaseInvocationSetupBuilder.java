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

import com.envimate.messageMate.configuration.AsynchronousConfiguration;
import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.messageBus.MessageBusBuilder;
import com.envimate.messageMate.processingContext.EventType;
import com.envimate.messageMate.shared.environment.TestEnvironment;
import com.envimate.messageMate.useCases.building.*;
import com.envimate.messageMate.useCases.shared.TestUseCase;
import com.envimate.messageMate.useCases.useCaseAdapter.UseCaseAdapter;
import com.envimate.messageMate.useCases.useCaseAdapter.UseCaseInvocationBuilder;
import com.envimate.messageMate.useCases.useCaseAdapter.usecaseInstantiating.UseCaseInstantiator;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.envimate.messageMate.configuration.AsynchronousConfiguration.constantPoolSizeAsynchronousConfiguration;
import static com.envimate.messageMate.messageBus.MessageBusBuilder.aMessageBus;
import static com.envimate.messageMate.messageBus.MessageBusType.ASYNCHRONOUS;
import static com.envimate.messageMate.messageBus.givenWhenThen.MessageBusTestExceptionHandler.allExceptionHandlingTestExceptionHandler;
import static com.envimate.messageMate.shared.environment.TestEnvironment.emptyTestEnvironment;
import static com.envimate.messageMate.shared.environment.TestEnvironmentProperty.*;

public final class UseCaseInvocationSetupBuilder {
    private final TestUseCase testUseCase;
    private final TestEnvironment testEnvironment;
    private final Step1Builder useCaseAdapterBuilder;
    private final MessageBusBuilder messageBusBuilder = aMessageBus();
    private final BiFunction<BuilderStepBuilder, MessageBus, Object> sutBuildingFunction;
    private Function<Step1Builder, DeserializationStep1Builder> instantiationFunction;
    private FinalStepBuilder finalStepBuilder;

    public UseCaseInvocationSetupBuilder(final TestUseCase testUseCase,
                                         final BiFunction<BuilderStepBuilder, MessageBus, Object> sutBuildingFunction) {
        this.testUseCase = testUseCase;
        this.sutBuildingFunction = sutBuildingFunction;
        this.testEnvironment = emptyTestEnvironment();
        this.useCaseAdapterBuilder = UseCaseInvocationBuilder.anUseCaseAdapter();
        this.instantiationFunction = InstantiationBuilder::obtainingUseCaseInstancesUsingTheZeroArgumentConstructor;
    }

    public static UseCaseInvocationSetupBuilder aUseCaseAdapterFor(final TestUseCase testUseCase) {
        final BiFunction<BuilderStepBuilder, MessageBus, Object> sutBuildingFunction = (builder, messageBus) -> {
            final UseCaseAdapter useCaseAdapter = builder.buildAsStandaloneAdapter();
            useCaseAdapter.attachAndEnhance(messageBus);
            return useCaseAdapter;
        };
        return new UseCaseInvocationSetupBuilder(testUseCase, sutBuildingFunction);
    }

    public static UseCaseInvocationSetupBuilder aUseCaseBusFor(final TestUseCase testUseCase) {
        final BiFunction<BuilderStepBuilder, MessageBus, Object> sutBuildingFunction = BuilderStepBuilder::build;
        return new UseCaseInvocationSetupBuilder(testUseCase, sutBuildingFunction);
    }

    public UseCaseInvocationSetupBuilder invokingTheUseCaseUsingTheSingleUseCaseMethod() {
        final Class<?> useCaseClass = testUseCase.getUseCaseClass();
        final EventType eventType = testUseCase.getEventType();
        final Step1Builder useCaseInvokingBuilder = useCaseAdapterBuilder.invokingUseCase(useCaseClass)
                .forType(eventType)
                .callingTheSingleUseCaseMethod();
        final DeserializationStep1Builder deserializationBuilder = instantiationFunction.apply(useCaseInvokingBuilder);
        testUseCase.defineDeserialization(deserializationBuilder);
        final ResponseSerializationStep1Builder serializationStep1Builder = deserializationBuilder
                .throwAnExceptionByDefaultIfNoParameterMappingCanBeApplied();
        testUseCase.defineSerialization(serializationStep1Builder);
        finalStepBuilder = serializationStep1Builder.throwingAnExceptionByDefaultIfNoResponseMappingCanBeApplied()
                .puttingExceptionObjectNamedAsExceptionIntoResponseMapByDefault();
        return this;
    }

    public UseCaseInvocationSetupBuilder invokingTheUseCaseUsingTheDefinedMapping() {
        final Class<?> useCaseClass = testUseCase.getUseCaseClass();
        final EventType eventType = testUseCase.getEventType();
        final Step3Builder<?> callingBuilder = useCaseAdapterBuilder.invokingUseCase(useCaseClass)
                .forType(eventType);
        testUseCase.useCustomInvocationLogic(callingBuilder);
        finalStepBuilder = useCaseAdapterBuilder.obtainingUseCaseInstancesUsingTheZeroArgumentConstructor()
                .throwAnExceptionByDefaultIfNoParameterMappingCanBeApplied()
                .throwingAnExceptionByDefaultIfNoResponseMappingCanBeApplied()
                .puttingExceptionObjectNamedAsExceptionIntoResponseMapByDefault();
        return this;
    }

    public UseCaseInvocationSetupBuilder invokingTheUseCaseUsingAMissingDeserializationParameter() {
        final Class<?> useCaseClass = testUseCase.getUseCaseClass();
        final EventType eventType = testUseCase.getEventType();
        final Step1Builder useCaseInvokingBuilder = useCaseAdapterBuilder.invokingUseCase(useCaseClass)
                .forType(eventType)
                .callingTheSingleUseCaseMethod();
        final DeserializationStep1Builder deserializationBuilder = instantiationFunction.apply(useCaseInvokingBuilder);
        final ResponseSerializationStep1Builder serializationStep1Builder = deserializationBuilder
                .throwAnExceptionByDefaultIfNoParameterMappingCanBeApplied();
        finalStepBuilder = serializationStep1Builder.throwingAnExceptionByDefaultIfNoResponseMappingCanBeApplied()
                .puttingExceptionObjectNamedAsExceptionIntoResponseMapByDefault();
        messageBusBuilder.withExceptionHandler(allExceptionHandlingTestExceptionHandler(testEnvironment, EXCEPTION));
        return this;
    }

    public UseCaseInvocationSetupBuilder invokingTheUseCaseUsingAMissingSerializationParameter() {
        final Class<?> useCaseClass = testUseCase.getUseCaseClass();
        final EventType eventType = testUseCase.getEventType();
        final Step1Builder useCaseInvokingBuilder = useCaseAdapterBuilder.invokingUseCase(useCaseClass)
                .forType(eventType)
                .callingTheSingleUseCaseMethod();
        final DeserializationStep1Builder deserializationBuilder = instantiationFunction.apply(useCaseInvokingBuilder);
        testUseCase.defineDeserialization(deserializationBuilder);
        final ResponseSerializationStep1Builder serializationStep1Builder = deserializationBuilder
                .throwAnExceptionByDefaultIfNoParameterMappingCanBeApplied();
        finalStepBuilder = serializationStep1Builder.throwingAnExceptionByDefaultIfNoResponseMappingCanBeApplied()
                .puttingExceptionObjectNamedAsExceptionIntoResponseMapByDefault();
        messageBusBuilder.withExceptionHandler(allExceptionHandlingTestExceptionHandler(testEnvironment, EXCEPTION));
        return this;
    }

    public UseCaseInvocationSetupBuilder usingACustomInstantiationMechanism() {
        final Supplier<Object> useCaseInstantiationFunction = testUseCase.getInstantiationFunction();
        instantiationFunction = b -> b.obtainingUseCaseInstancesUsing(new UseCaseInstantiator() {
            @SuppressWarnings("unchecked")
            @Override
            public <T> T instantiate(final Class<T> type) {
                return (T) useCaseInstantiationFunction.get();
            }
        });
        return this;
    }

    public UseCaseInvocationSetup build() {
        final MessageBus messageBus = createMessageBus();
        testUseCase.applyOptionalParameterInjection(finalStepBuilder);
        final Object sut = sutBuildingFunction.apply(finalStepBuilder, messageBus);
        testEnvironment.setProperty(SUT, sut);
        testEnvironment.setProperty(MOCK, messageBus);
        return new UseCaseInvocationSetup(testEnvironment, testUseCase, messageBus);
    }

    private MessageBus createMessageBus() {
        final AsynchronousConfiguration asynchronousConfiguration = constantPoolSizeAsynchronousConfiguration(3);
        messageBusBuilder.forType(ASYNCHRONOUS)
                .withAsynchronousConfiguration(asynchronousConfiguration);
        final BiConsumer<MessageBusBuilder, TestEnvironment> messageBusEnhancer = testUseCase.getMessageBusEnhancer();
        messageBusEnhancer.accept(messageBusBuilder, testEnvironment);
        return messageBusBuilder
                .build();
    }

}
