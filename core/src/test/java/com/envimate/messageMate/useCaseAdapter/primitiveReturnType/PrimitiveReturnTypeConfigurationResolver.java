package com.envimate.messageMate.useCaseAdapter.primitiveReturnType;

import com.envimate.messageMate.messageBus.EventType;
import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.shared.config.AbstractTestConfigProvider;
import com.envimate.messageMate.useCaseAdapter.TestUseCase;
import com.envimate.messageMate.useCaseAdapter.building.UseCaseAdapterDeserializationStep1Builder;
import com.envimate.messageMate.useCaseAdapter.building.UseCaseAdapterStep3Builder;

import java.util.HashMap;
import java.util.Map;
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
        final Map<String, Object> requestObject = new HashMap<>();
        requestObject.put("int", expectedResponse);
        final Supplier<Object> instantiationFunction = PrimitiveReturnTypeUseCase::new;
        final Consumer<UseCaseAdapterDeserializationStep1Builder> deserializationEnhancer = deserializationStepBuilder -> {
            deserializationStepBuilder.mappingRequestsToUseCaseParametersOfType(PrimitiveReturnTypeRequest.class)
                    .using((targetType, map) -> new PrimitiveReturnTypeRequest((int) map.get("int")));
        };
        Consumer<UseCaseAdapterStep3Builder<?>> customCallingLogic = useCaseAdapterStep3Builder -> {
            useCaseAdapterStep3Builder.calling((useCase, map) -> {
                final PrimitiveReturnTypeUseCase primitiveReturnTypeUseCase = (PrimitiveReturnTypeUseCase) useCase;
                final Map<String, Object> requestMap = (Map<String, Object>) map;
                final int intParameter = (int) requestMap.get("int");
                final PrimitiveReturnTypeRequest request = new PrimitiveReturnTypeRequest(intParameter);
                final int returnValue = primitiveReturnTypeUseCase.useCaseMethod(request);
                return returnValue;
            });
        };
        return testUseCase(USE_CASE_CLASS, EVENT_TYPE, messageBusSetup, instantiationFunction, deserializationEnhancer, customCallingLogic, requestObject, expectedResponse);
    }
}
