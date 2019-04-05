package com.envimate.messageMate.useCaseAdapter.primitiveParameter;

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

public class PrimitiveParameterConfigurationResolver extends AbstractTestConfigProvider {
    private static final Class<PrimitiveParameterUseCase> USE_CASE_CLASS = PrimitiveParameterUseCase.class;
    private static final EventType EVENT_TYPE = EventType.eventTypeFromString("PrimitiveParameterUseCase");

    @Override
    protected Class<?> forConfigClass() {
        return TestUseCase.class;
    }

    @Override
    protected Object testConfig() {
        final BiConsumer<MessageBus, TestEnvironment> messageBusSetup = (messageBus, testEnvironment) -> {
            messageBus.subscribe(USE_CASE_RESPONSE_EVENT_TYPE, i -> testEnvironment.setPropertyIfNotSet(RESULT, i));
        };
        final Supplier<Object> instantiationFunction = PrimitiveParameterUseCase::new;
        final Consumer<UseCaseAdapterStep3Builder<?>> parameterMapping = callingBuilder -> {
            callingBuilder.mappingEventToParameter(int.class, o -> (int) o); //TODO: think about adding default mappings for primitive
        };
        final Consumer<UseCaseAdapterStep3Builder<?>> customCallingLogic = callingBuilder -> {
            callingBuilder.calling((useCaseInstance, event) -> {
                final PrimitiveParameterUseCase useCase = (PrimitiveParameterUseCase) useCaseInstance;
                final int parameter = (int) event;
                //final int intReturnValue = useCase.useCaseMethod(parameter);
                return null;
            });
        };
        final boolean expectedResponse = true;
        return testUseCase(USE_CASE_CLASS, EVENT_TYPE, messageBusSetup, instantiationFunction, parameterMapping, customCallingLogic, expectedResponse, expectedResponse);
    }
}
