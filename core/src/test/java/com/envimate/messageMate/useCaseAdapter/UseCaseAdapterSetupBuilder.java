package com.envimate.messageMate.useCaseAdapter;

import com.envimate.messageMate.internal.pipe.configuration.AsynchronousConfiguration;
import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.messageBus.MessageBusBuilder;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.soonToBeExternal.building.UseCaseAdapterInstantiationBuilder;
import com.envimate.messageMate.soonToBeExternal.building.UseCaseAdapterStep1Builder;
import com.envimate.messageMate.soonToBeExternal.building.UseCaseAdapterStep3Builder;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.envimate.messageMate.internal.pipe.configuration.AsynchronousConfiguration.constantPoolSizeAsynchronousPipeConfiguration;
import static com.envimate.messageMate.messageBus.MessageBusBuilder.aMessageBus;
import static com.envimate.messageMate.messageBus.MessageBusType.ASYNCHRONOUS;
import static com.envimate.messageMate.qcec.shared.TestEnvironment.emptyTestEnvironment;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.MOCK;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.SUT;

public final class UseCaseAdapterSetupBuilder {
    private final TestUseCase testUseCase;
    private final TestEnvironment testEnvironment;
    private final UseCaseAdapterStep1Builder useCaseAdapterBuilder;
    private Function<UseCaseAdapterStep1Builder, UseCaseAdapter> instantiationFunction;

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
        final Class<?> eventClass = testUseCase.getUseCaseEvent();
        useCaseAdapterBuilder.invokingUseCase(useCaseClass)
                .forEvent(eventClass)
                .callingTheSingleUseCaseMethod();
        return this;
    }

    public UseCaseAdapterSetupBuilder invokingTheUseCaseUsingTheDefinedMapping() {
        final Class<?> useCaseClass = testUseCase.getUseCaseClass();
        final Class<?> eventClass = testUseCase.getUseCaseEvent();
        final UseCaseAdapterStep3Builder<?, ?> callingBuilder = useCaseAdapterBuilder.invokingUseCase(useCaseClass)
                .forEvent(eventClass);
        testUseCase.defineParameterMapping(callingBuilder);
        return this;
    }

    public UseCaseAdapterSetupBuilder usingACustomInstantiationMechanism() {
        final Supplier<Object> useCaseInstantiationFunction = testUseCase.getInstantiationFunction();
        instantiationFunction = b -> b.obtainingUseCaseInstancesUsing(new UseCaseInstantiator() {
            @Override
            public <T> T instantiate(final Class<T> type) {
                return (T) useCaseInstantiationFunction.get();
            }
        });
        return this;
    }

    public UseCaseAdapterSetup build() {
        final UseCaseAdapter useCaseAdapter = instantiationFunction.apply(useCaseAdapterBuilder);
        testEnvironment.setProperty(SUT, useCaseAdapter);
        final AsynchronousConfiguration asynchronousConfiguration = constantPoolSizeAsynchronousPipeConfiguration(3);
        final MessageBusBuilder messageBusBuilder = aMessageBus()
                .forType(ASYNCHRONOUS)
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
