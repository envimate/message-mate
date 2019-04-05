package com.envimate.messageMate.useCaseAdapter;

import com.envimate.messageMate.internal.pipe.configuration.AsynchronousConfiguration;
import com.envimate.messageMate.messageBus.EventType;
import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.messageBus.MessageBusBuilder;
import com.envimate.messageMate.messageBus.givenWhenThen.MessageBusTestExceptionHandler;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.useCaseAdapter.building.UseCaseAdapterDeserializationStep1Builder;
import com.envimate.messageMate.useCaseAdapter.building.UseCaseAdapterInstantiationBuilder;
import com.envimate.messageMate.useCaseAdapter.building.UseCaseAdapterStep1Builder;
import com.envimate.messageMate.useCaseAdapter.building.UseCaseAdapterStep3Builder;
import com.envimate.messageMate.useCaseAdapter.usecaseInstantiating.UseCaseInstantiator;

import java.util.function.Consumer;
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
    private final UseCaseAdapterStep1Builder useCaseAdapterBuilder;
    private final MessageBusBuilder messageBusBuilder = aMessageBus();
    private Function<UseCaseAdapterStep1Builder, UseCaseAdapterDeserializationStep1Builder> instantiationFunction;
    private UseCaseAdapter useCaseAdapter;

    public UseCaseAdapterSetupBuilder(final TestUseCase testUseCase) {
        this.testUseCase = testUseCase;
        this.testEnvironment = emptyTestEnvironment();
        this.useCaseAdapterBuilder = UseCaseAdapterBuilder.anUseCaseAdapter();
        this.instantiationFunction = UseCaseAdapterInstantiationBuilder::obtainingUseCaseInstancesUsingTheZeroArgumentConstructor;
    }

    public static UseCaseAdapterSetupBuilder aUseCaseAdapter(final TestUseCase testUseCase) {
        return new UseCaseAdapterSetupBuilder(testUseCase);
    }

    public UseCaseAdapterSetupBuilder invokingTheUseCaseUsingTheSingleUseCaseMethod() {
        final Class<?> useCaseClass = testUseCase.getUseCaseClass();
        final EventType eventType = testUseCase.getEventType();
        final UseCaseAdapterStep1Builder useCaseInvokingBuilder = useCaseAdapterBuilder.invokingUseCase(useCaseClass)
                .forType(eventType)
                .callingTheSingleUseCaseMethod();
        final UseCaseAdapterDeserializationStep1Builder deserializationBuilder = instantiationFunction.apply(useCaseInvokingBuilder);
        testUseCase.defineDeserialization(deserializationBuilder);
        useCaseAdapter = deserializationBuilder.throwAnExceptionByDefault();
        return this;
    }

    public UseCaseAdapterSetupBuilder invokingTheUseCaseUsingTheDefinedMapping() {
        final Class<?> useCaseClass = testUseCase.getUseCaseClass();
        final EventType eventType = testUseCase.getEventType();
        final UseCaseAdapterStep3Builder<?> callingBuilder = useCaseAdapterBuilder.invokingUseCase(useCaseClass)
                .forType(eventType);
        testUseCase.useCustomInvocationLogic(callingBuilder);
        useCaseAdapter = useCaseAdapterBuilder.obtainingUseCaseInstancesUsingTheZeroArgumentConstructor()
                .throwAnExceptionByDefault();
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
        final Consumer<MessageBusBuilder> messageBusEnhancer = testUseCase.getMessageBusEnhancer();
        messageBusEnhancer.accept(messageBusBuilder);
        final MessageBus messageBus = messageBusBuilder
                .build();
        useCaseAdapter.attachTo(messageBus);
        testEnvironment.setProperty(MOCK, messageBus);
        return new UseCaseAdapterSetup(testEnvironment, testUseCase, messageBus);
    }

}
