package com.envimate.messageMate.useCaseAdapter.noParameter;

import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.shared.config.AbstractTestConfigProvider;
import com.envimate.messageMate.soonToBeExternal.building.UseCaseAdapterStep3Builder;
import com.envimate.messageMate.useCaseAdapter.TestUseCase;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.RESULT;
import static com.envimate.messageMate.useCaseAdapter.TestUseCase.testUseCase;

public class NoParameterConfigurationResolver extends AbstractTestConfigProvider {

    public static final Class<?> USE_CASE_CLASS = NoParameterUseCase.class;
    public static final Class<?> EventClass = DummyUseCaseEvent.class;

    @Override
    protected Class<?> forConfigClass() {
        return TestUseCase.class;
    }

    @Override
    protected Object testConfig() {
        final BiConsumer<MessageBus, TestEnvironment> messageBusSetup = (messageBus, testEnvironment) -> {
            messageBus.subscribe(String.class, s -> {
                if (testEnvironment.has(RESULT)) {
                    testEnvironment.addToListProperty(RESULT, s);
                } else {
                    testEnvironment.setProperty(RESULT, s);
                }
            });
        };
        final Object requestObject = new DummyUseCaseEvent();
        final Supplier<Object> instantiationFunction = NoParameterUseCase::new;
        final Consumer<UseCaseAdapterStep3Builder<?, ?>> parameterMapping = callingBuilder -> {
            callingBuilder.calling((useCaseInstance, event) -> {
                final NoParameterUseCase useCase = (NoParameterUseCase) useCaseInstance;
                final String stringReturnValue = useCase.useCaseMethod();
                return stringReturnValue;
            });
        };
        final String expectedResult = NoParameterUseCase.NO_PARAMETER_USE_CASE_RETURN_VALUE;
        return testUseCase(USE_CASE_CLASS, EventClass, messageBusSetup, instantiationFunction, parameterMapping, requestObject, expectedResult);
    }
}
