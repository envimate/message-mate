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
import java.util.function.Function;
import java.util.function.Supplier;

import static com.envimate.messageMate.internal.pipe.configuration.AsynchronousConfiguration.constantPoolSizeAsynchronousPipeConfiguration;
import static com.envimate.messageMate.messageBus.MessageBusBuilder.aMessageBus;
import static com.envimate.messageMate.messageBus.MessageBusType.ASYNCHRONOUS;
import static com.envimate.messageMate.qcec.shared.TestEnvironment.emptyTestEnvironment;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.*;

public final class UseCaseAdapterSetupBuilder {
    private final TestUseCase testUseCase;
    private final TestEnvironment testEnvironment;
    private final Step1Builder useCaseAdapterBuilder;
    private final MessageBusBuilder messageBusBuilder = aMessageBus();
    private Function<Step1Builder, DeserializationStep1Builder> instantiationFunction;
    private UseCaseAdapter useCaseAdapter;

    public UseCaseAdapterSetupBuilder(final TestUseCase testUseCase) {
        this.testUseCase = testUseCase;
        this.testEnvironment = emptyTestEnvironment();
        this.useCaseAdapterBuilder = UseCaseInvocationBuilder.anUseCaseAdapter();
        this.instantiationFunction = InstantiationBuilder::obtainingUseCaseInstancesUsingTheZeroArgumentConstructor;
    }

    public static UseCaseAdapterSetupBuilder aUseCaseAdapter(final TestUseCase testUseCase) {
        return new UseCaseAdapterSetupBuilder(testUseCase);
    }

    public UseCaseAdapterSetupBuilder invokingTheUseCaseUsingTheSingleUseCaseMethod() {
        final Class<?> useCaseClass = testUseCase.getUseCaseClass();
        final EventType eventType = testUseCase.getEventType();
        final Step1Builder useCaseInvokingBuilder = useCaseAdapterBuilder.invokingUseCase(useCaseClass)
                .forType(eventType)
                .callingTheSingleUseCaseMethod();
        final DeserializationStep1Builder deserializationBuilder = instantiationFunction.apply(useCaseInvokingBuilder);
        testUseCase.defineDeserialization(deserializationBuilder);
        final ResponseSerializationStep1Builder serializationStep1Builder = deserializationBuilder.throwAnExceptionByDefault();
        testUseCase.defineSerialization(serializationStep1Builder);
        useCaseAdapter = serializationStep1Builder.throwingAnExceptionIfNoResponseMappingCanBeFound()
                .puttingExceptionObjectNamedAsExceptionIntoResponseMapByDefault().buildAsStandaloneAdapter();
        return this;
    }

    public UseCaseAdapterSetupBuilder invokingTheUseCaseUsingTheDefinedMapping() {
        final Class<?> useCaseClass = testUseCase.getUseCaseClass();
        final EventType eventType = testUseCase.getEventType();
        final Step3Builder<?> callingBuilder = useCaseAdapterBuilder.invokingUseCase(useCaseClass)
                .forType(eventType);
        testUseCase.useCustomInvocationLogic(callingBuilder);
        useCaseAdapter = useCaseAdapterBuilder.obtainingUseCaseInstancesUsingTheZeroArgumentConstructor()
                .throwAnExceptionByDefault().throwingAnExceptionIfNoResponseMappingCanBeFound()
                .puttingExceptionObjectNamedAsExceptionIntoResponseMapByDefault().buildAsStandaloneAdapter();
        return this;
    }

    public UseCaseAdapterSetupBuilder invokingTheUseCaseUsingAMappingMissingAParameter() {
        final Class<?> useCaseClass = testUseCase.getUseCaseClass();
        final EventType eventType = testUseCase.getEventType();
        useCaseAdapterBuilder.invokingUseCase(useCaseClass)
                .forType(eventType).callingTheSingleUseCaseMethod();
        messageBusBuilder.withExceptionHandler(MessageBusTestExceptionHandler.allExceptionHandlingTestExceptionHandler(testEnvironment, EXCEPTION));
        return this;
    }

    public UseCaseAdapterSetupBuilder invokingTheUseCaseUsingARedundantMappingMissingAParameter() {
        final Class<?> useCaseClass = testUseCase.getUseCaseClass();
        final EventType eventType = testUseCase.getEventType();
        try {
            useCaseAdapterBuilder.invokingUseCase(useCaseClass)
                    .forType(eventType).callingTheSingleUseCaseMethod();
        } catch (final Exception e) {
            testEnvironment.setProperty(EXCEPTION, e);
        }
        return this;
    }

    public UseCaseAdapterSetupBuilder usingACustomInstantiationMechanism() {
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

    public UseCaseAdapterSetup build() {
        testEnvironment.setProperty(SUT, useCaseAdapter);
        final AsynchronousConfiguration asynchronousConfiguration = constantPoolSizeAsynchronousPipeConfiguration(3);
        messageBusBuilder.forType(ASYNCHRONOUS)
                .withAsynchronousConfiguration(asynchronousConfiguration);
        final BiConsumer<MessageBusBuilder, TestEnvironment> messageBusEnhancer = testUseCase.getMessageBusEnhancer();
        messageBusEnhancer.accept(messageBusBuilder, testEnvironment);
        final MessageBus messageBus = messageBusBuilder
                .build();
        useCaseAdapter.attachAndEnhance(messageBus);
        testEnvironment.setProperty(MOCK, messageBus);
        return new UseCaseAdapterSetup(testEnvironment, testUseCase, messageBus);
    }

}
