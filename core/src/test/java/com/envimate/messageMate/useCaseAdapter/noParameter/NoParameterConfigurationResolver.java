package com.envimate.messageMate.useCaseAdapter.noParameter;

import com.envimate.messageMate.messageBus.EventType;
import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.shared.config.AbstractTestConfigProvider;
import com.envimate.messageMate.useCaseAdapter.TestUseCase;
import com.envimate.messageMate.useCaseAdapter.building.UseCaseAdapterStep3Builder;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.RESULT;
import static com.envimate.messageMate.useCaseAdapter.TestUseCase.testUseCase;
import static com.envimate.messageMate.useCaseAdapter.UseCaseInvokingResponseEventType.USE_CASE_RESPONSE_EVENT_TYPE;

public class NoParameterConfigurationResolver extends AbstractTestConfigProvider {

    public static final Class<?> USE_CASE_CLASS = NoParameterUseCase.class;
    public static final EventType EVENT_TYPE = EventType.eventTypeFromString("NoParameterUseCase");

    @Override
    protected Class<?> forConfigClass() {
        return TestUseCase.class;
    }

    @Override
    protected Object testConfig() {
        final BiConsumer<MessageBus, TestEnvironment> messageBusSetup = (messageBus, testEnvironment) -> {
            messageBus.subscribe(USE_CASE_RESPONSE_EVENT_TYPE, s -> {
                if (testEnvironment.has(RESULT)) {
                    testEnvironment.addToListProperty(RESULT, s);
                } else {
                    testEnvironment.setProperty(RESULT, s);
                }
            });
        };
        final Object requestObject = new DummyUseCaseEvent();
        final Supplier<Object> instantiationFunction = NoParameterUseCase::new;
        final Consumer<UseCaseAdapterStep3Builder<?>> parameterMapping = callingBuilder -> {
            callingBuilder.calling((useCaseInstance, event) -> {
                final NoParameterUseCase useCase = (NoParameterUseCase) useCaseInstance;
                final String stringReturnValue = useCase.useCaseMethod();
                return stringReturnValue;
            });
        };
        final String expectedResult = NoParameterUseCase.NO_PARAMETER_USE_CASE_RETURN_VALUE;
        return testUseCase(USE_CASE_CLASS, EVENT_TYPE, messageBusSetup, instantiationFunction, parameterMapping, requestObject, expectedResult);
    }
}