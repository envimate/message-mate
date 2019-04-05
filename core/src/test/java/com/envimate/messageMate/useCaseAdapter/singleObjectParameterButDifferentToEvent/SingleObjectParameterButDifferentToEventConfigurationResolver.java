package com.envimate.messageMate.useCaseAdapter.singleObjectParameterButDifferentToEvent;

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
import static com.envimate.messageMate.useCaseAdapter.singleObjectParameterButDifferentToEvent.SingleObjectParameterButDifferentToEventParameterEvent.singleObjectParameterButDifferentToEvent;

public class SingleObjectParameterButDifferentToEventConfigurationResolver extends AbstractTestConfigProvider {
    private static final Class<SingleObjectParameterButDifferentToEventParameterUseCase> USE_CASE_CLASS = SingleObjectParameterButDifferentToEventParameterUseCase.class;
    private static final EventType EVENT_TYPE = EventType.eventTypeFromString("SingleObjectParameterButDifferentToEventParameterUseCase");

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
        final Object requestObject = singleObjectParameterButDifferentToEvent(expectedResponse);
        final Supplier<Object> instantiationFunction = SingleObjectParameterButDifferentToEventParameterUseCase::new;
        final Consumer<UseCaseAdapterStep3Builder<?>> parameterMapping = callingBuilder -> {
        };
        final Consumer<UseCaseAdapterStep3Builder<?>> customCallingLogic = callingBuilder -> {
        };
        return testUseCase(USE_CASE_CLASS, EVENT_TYPE, messageBusSetup, instantiationFunction, parameterMapping, customCallingLogic, requestObject, expectedResponse);
    }
}
