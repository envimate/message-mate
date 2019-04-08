package com.envimate.messageMate.useCases;

import com.envimate.messageMate.internal.pipe.configuration.AsynchronousConfiguration;
import com.envimate.messageMate.messageBus.EventType;
import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.messageBus.MessageBusBuilder;
import com.envimate.messageMate.messageBus.givenWhenThen.MessageBusTestExceptionHandler;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.useCases.building.*;
import com.envimate.messageMate.useCases.useCaseAdapter.UseCaseAdapter;
import com.envimate.messageMate.useCases.useCaseAdapter.UseCaseInvocationBuilder;
import com.envimate.messageMate.useCases.useCaseAdapter.usecaseInstantiating.UseCaseInstantiator;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.envimate.messageMate.internal.pipe.configuration.AsynchronousConfiguration.constantPoolSizeAsynchronousPipeConfiguration;
import static com.envimate.messageMate.messageBus.MessageBusBuilder.aMessageBus;
import static com.envimate.messageMate.messageBus.MessageBusType.ASYNCHRONOUS;
import static com.envimate.messageMate.qcec.shared.TestEnvironment.emptyTestEnvironment;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.*;

public final class UseCaseInvocationSetupBuilder {
    private final TestUseCase testUseCase;
    private final TestEnvironment testEnvironment;
    private final Step1Builder useCaseAdapterBuilder;
    private final MessageBusBuilder messageBusBuilder = aMessageBus();
    private final BiFunction<BuilderStepBuilder, MessageBus, Object> sutBuildingFunction;
    private Function<Step1Builder, DeserializationStep1Builder> instantiationFunction;
    private BuilderStepBuilder builderStepBuilder;

    public UseCaseInvocationSetupBuilder(final TestUseCase testUseCase,
                                         final BiFunction<BuilderStepBuilder, MessageBus, Object> sutBuildingFunction) {
        this.testUseCase = testUseCase;
        this.sutBuildingFunction = sutBuildingFunction;
        this.testEnvironment = emptyTestEnvironment();
        this.useCaseAdapterBuilder = UseCaseInvocationBuilder.anUseCaseAdapter();
        this.instantiationFunction = InstantiationBuilder::obtainingUseCaseInstancesUsingTheZeroArgumentConstructor;
    }

    public static UseCaseInvocationSetupBuilder aUseCaseAdapter(final TestUseCase testUseCase) {
        final BiFunction<BuilderStepBuilder, MessageBus, Object> sutBuildingFunction = (builder, messageBus) -> {
            final UseCaseAdapter useCaseAdapter = builder.buildAsStandaloneAdapter();
            useCaseAdapter.attachAndEnhance(messageBus);
            return useCaseAdapter;
        };
        return new UseCaseInvocationSetupBuilder(testUseCase, sutBuildingFunction);
    }

    public static UseCaseInvocationSetupBuilder aUseCaseBus(final TestUseCase testUseCase) {
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
        final ResponseSerializationStep1Builder serializationStep1Builder = deserializationBuilder.throwAnExceptionByDefault();
        testUseCase.defineSerialization(serializationStep1Builder);
        builderStepBuilder = serializationStep1Builder.throwingAnExceptionIfNoResponseMappingCanBeFound()
                .puttingExceptionObjectNamedAsExceptionIntoResponseMapByDefault();
        return this;
    }

    public UseCaseInvocationSetupBuilder invokingTheUseCaseUsingTheDefinedMapping() {
        final Class<?> useCaseClass = testUseCase.getUseCaseClass();
        final EventType eventType = testUseCase.getEventType();
        final Step3Builder<?> callingBuilder = useCaseAdapterBuilder.invokingUseCase(useCaseClass)
                .forType(eventType);
        testUseCase.useCustomInvocationLogic(callingBuilder);
        builderStepBuilder = useCaseAdapterBuilder.obtainingUseCaseInstancesUsingTheZeroArgumentConstructor()
                .throwAnExceptionByDefault().throwingAnExceptionIfNoResponseMappingCanBeFound()
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
        final ResponseSerializationStep1Builder serializationStep1Builder = deserializationBuilder.throwAnExceptionByDefault();
        builderStepBuilder = serializationStep1Builder.throwingAnExceptionIfNoResponseMappingCanBeFound()
                .puttingExceptionObjectNamedAsExceptionIntoResponseMapByDefault();
        messageBusBuilder.withExceptionHandler(MessageBusTestExceptionHandler.allExceptionHandlingTestExceptionHandler(testEnvironment, EXCEPTION));
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
        final ResponseSerializationStep1Builder serializationStep1Builder = deserializationBuilder.throwAnExceptionByDefault();
        builderStepBuilder = serializationStep1Builder.throwingAnExceptionIfNoResponseMappingCanBeFound()
                .puttingExceptionObjectNamedAsExceptionIntoResponseMapByDefault();
        messageBusBuilder.withExceptionHandler(MessageBusTestExceptionHandler.allExceptionHandlingTestExceptionHandler(testEnvironment, EXCEPTION));
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
        final Object sut = sutBuildingFunction.apply(builderStepBuilder, messageBus);
        testEnvironment.setProperty(SUT, sut);
        testEnvironment.setProperty(MOCK, messageBus);
        return new UseCaseInvocationSetup(testEnvironment, testUseCase, messageBus);
    }

    private MessageBus createMessageBus() {
        final AsynchronousConfiguration asynchronousConfiguration = constantPoolSizeAsynchronousPipeConfiguration(3);
        messageBusBuilder.forType(ASYNCHRONOUS)
                .withAsynchronousConfiguration(asynchronousConfiguration);
        final BiConsumer<MessageBusBuilder, TestEnvironment> messageBusEnhancer = testUseCase.getMessageBusEnhancer();
        messageBusEnhancer.accept(messageBusBuilder, testEnvironment);
        return messageBusBuilder
                .build();
    }

}
