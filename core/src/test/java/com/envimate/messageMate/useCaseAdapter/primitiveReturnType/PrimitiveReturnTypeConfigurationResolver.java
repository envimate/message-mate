package com.envimate.messageMate.useCaseAdapter.primitiveReturnType;

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

public class PrimitiveReturnTypeConfigurationResolver extends AbstractTestConfigProvider {
    private static final Class<PrimitiveReturnTypeUseCase> USE_CASE_CLASS = PrimitiveReturnTypeUseCase.class;
    private static final EventType EVENT_TYPE = EventType.eventTypeFromString("PrimitiveReturnTypeUseCase");

    @Override
    protected Class<?> forConfigClass() {
        return TestUseCase.class;
    }

    @Override
    protected Object testConfig() {
        final int expectedResponse = 5;
        final BiConsumer<MessageBus, TestEnvironment> messageBusSetup = (messageBus, testEnvironment) -> {
            messageBus.subscribe(USE_CASE_RESPONSE_EVENT_TYPE, i -> testEnvironment.setPropertyIfNotSet(RESULT, i));
        };
        final Supplier<Object> instantiationFunction = PrimitiveReturnTypeUseCase::new;
        final Consumer<UseCaseAdapterStep3Builder<?>> parameterMapping = callingBuilder -> {
            callingBuilder.mappingEventToParameter(int.class, o -> (int) o); //TODO: think about adding default mappings for primitive
        };
        final Consumer<UseCaseAdapterStep3Builder<?>> customCallingLogic = callingBuilder -> {
            callingBuilder.calling((useCaseInstance, event) -> {
                final PrimitiveReturnTypeUseCase useCase = (PrimitiveReturnTypeUseCase) useCaseInstance;
                final int parameter = (int) event;
                final int intReturnValue = useCase.useCaseMethod(parameter);
                return intReturnValue;
            });
        };
        return testUseCase(USE_CASE_CLASS, EVENT_TYPE, messageBusSetup, instantiationFunction, parameterMapping, customCallingLogic, expectedResponse, expectedResponse);
    }
}
