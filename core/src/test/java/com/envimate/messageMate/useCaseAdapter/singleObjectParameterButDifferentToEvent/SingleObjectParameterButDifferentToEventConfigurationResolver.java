package com.envimate.messageMate.useCaseAdapter.singleObjectParameterButDifferentToEvent;

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
import static com.envimate.messageMate.useCaseAdapter.singleObjectParameterButDifferentToEvent.SingleObjectParameterButDifferentToEventParameterEvent.singleObjectParameterButDifferentToEvent;

public class SingleObjectParameterButDifferentToEventConfigurationResolver extends AbstractTestConfigProvider {
    private static final Class<SingleObjectParameterButDifferentToEventParameterUseCase> USE_CASE_CLASS = SingleObjectParameterButDifferentToEventParameterUseCase.class;
    private static final Class<SingleObjectParameterButDifferentToEventParameterEvent> EVENT_CLASS = SingleObjectParameterButDifferentToEventParameterEvent.class;

    @Override
    protected Class<?> forConfigClass() {
        return TestUseCase.class;
    }

    @Override
    protected Object testConfig() {
        final String expectedResponse = "expected Response";
        final BiConsumer<MessageBus, TestEnvironment> messageBusSetup = (messageBus, testEnvironment) -> {
            messageBus.subscribe(String.class, s -> testEnvironment.setPropertyIfNotSet(RESULT, s));
        };
        final Object requestObject = singleObjectParameterButDifferentToEvent(expectedResponse);
        final Supplier<Object> instantiationFunction = SingleObjectParameterButDifferentToEventParameterUseCase::new;
        final Consumer<UseCaseAdapterStep3Builder<?, ?>> parameterMapping = callingBuilder -> {
            callingBuilder.mappingEventToParameter(String.class, o -> ((SingleObjectParameterButDifferentToEventParameterEvent) o).getMessage());
        };
        final Consumer<UseCaseAdapterStep3Builder<?, ?>> customCallingLogic = callingBuilder -> {
            callingBuilder.calling((useCaseInstance, event) -> {
                final SingleObjectParameterButDifferentToEventParameterUseCase useCase = (SingleObjectParameterButDifferentToEventParameterUseCase) useCaseInstance;
                final SingleObjectParameterButDifferentToEventParameterEvent parameterEvent = (SingleObjectParameterButDifferentToEventParameterEvent) event;
                final String message = parameterEvent.getMessage();
                final String stringReturnValue = useCase.useCaseMethod(message);
                return stringReturnValue;
            });
        };
        return testUseCase(USE_CASE_CLASS, EVENT_CLASS, messageBusSetup, instantiationFunction, parameterMapping, customCallingLogic, requestObject, expectedResponse);
    }
}
