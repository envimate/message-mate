package com.envimate.messageMate.useCaseAdapter.singleEventParameter;

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

public class SingleEventParameterConfigurationResolver extends AbstractTestConfigProvider {
    private static final Class<SingleEventParameterUseCase> USE_CASE_CLASS = SingleEventParameterUseCase.class;
    private static final EventType EVENT_TYPE = EventType.eventTypeFromString("ExceptionThrowingUseCase");

    @Override
    protected Class<?> forConfigClass() {
        return TestUseCase.class;
    }

    @Override
    protected Object testConfig() {
        final String expectedResponse = "expected Response";
        final BiConsumer<MessageBus, TestEnvironment> messageBusSetup = (messageBus, testEnvironment) -> {
            messageBus.subscribe(USE_CASE_RESPONSE_EVENT_TYPE, s -> testEnvironment.setPropertyIfNotSet(RESULT, s));
        };
        final Object requestObject = SingleParameterEvent.testUseCaseRequest(expectedResponse);
        final Supplier<Object> instantiationFunction = SingleEventParameterUseCase::new;
        final Consumer<UseCaseAdapterStep3Builder<?>> parameterMapping = callingBuilder -> {
            callingBuilder.calling((useCaseInstance, event) -> {
                final SingleEventParameterUseCase useCase = (SingleEventParameterUseCase) useCaseInstance;
                final SingleParameterEvent singleParameterEvent = (SingleParameterEvent) event;
                final String stringReturnValue = useCase.useCaseMethod(singleParameterEvent);
                return stringReturnValue;
            });
        };
        return testUseCase(USE_CASE_CLASS, EVENT_TYPE, messageBusSetup, instantiationFunction, parameterMapping, requestObject, expectedResponse);
    }
}
