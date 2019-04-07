package com.envimate.messageMate.useCases.primitiveReturnType;

import com.envimate.messageMate.messageBus.EventType;
import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.shared.config.AbstractTestConfigProvider;
import com.envimate.messageMate.useCases.TestUseCase;
import com.envimate.messageMate.useCases.building.DeserializationStep1Builder;
import com.envimate.messageMate.useCases.building.ResponseSerializationStep1Builder;
import com.envimate.messageMate.useCases.building.Step3Builder;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.RESULT;
import static com.envimate.messageMate.useCases.TestUseCase.testUseCase;
import static com.envimate.messageMate.useCases.useCaseAdapter.UseCaseInvokingResponseEventType.USE_CASE_RESPONSE_EVENT_TYPE;

public class PrimitiveReturnTypeConfigurationResolver extends AbstractTestConfigProvider {
    private static final Class<PrimitiveReturnTypeUseCase> USE_CASE_CLASS = PrimitiveReturnTypeUseCase.class;
    private static final EventType EVENT_TYPE = EventType.eventTypeFromString("PrimitiveReturnTypeUseCase");
    private static final String PARAMETER_MAP_PROPERTY_NAME = "int";
    private static final String RETURN_MAP_PROPERTY_NAME = "returnValue";

    @Override
    protected Class<?> forConfigClass() {
        return TestUseCase.class;
    }

    @Override
    protected Object testConfig() {
        final int expectedResponse = 5;
        final Map<String, Object> expectedResponseMap = new HashMap<>();
        expectedResponseMap.put(RETURN_MAP_PROPERTY_NAME, expectedResponse);
        final BiConsumer<MessageBus, TestEnvironment> messageBusSetup = (messageBus, testEnvironment) -> {
            messageBus.subscribe(USE_CASE_RESPONSE_EVENT_TYPE, i -> testEnvironment.setPropertyIfNotSet(RESULT, i));
        };
        final Map<String, Object> requestObject = new HashMap<>();
        requestObject.put(PARAMETER_MAP_PROPERTY_NAME, expectedResponse);
        final Supplier<Object> instantiationFunction = PrimitiveReturnTypeUseCase::new;
        final Consumer<DeserializationStep1Builder> deserializationEnhancer = deserializationStepBuilder -> {
            deserializationStepBuilder.mappingRequestsToUseCaseParametersOfType(PrimitiveReturnTypeRequest.class)
                    .using((targetType, map) -> new PrimitiveReturnTypeRequest((int) map.get(PARAMETER_MAP_PROPERTY_NAME)));
        };

        final Consumer<ResponseSerializationStep1Builder> serializationEnhancer = serializationStepBuilder -> {
            serializationStepBuilder.serializingResponseObjectsOfType(Integer.class).using(intReturnValue -> { //TODO: inlucde int != Integer in doku
                final Map<String, Object> map = new HashMap<>();
                map.put(RETURN_MAP_PROPERTY_NAME, intReturnValue);
                return map;
            });
        };
        Consumer<Step3Builder<?>> customCallingLogic = useCaseAdapterStep3Builder -> {
            useCaseAdapterStep3Builder.calling((useCase, map) -> {
                final PrimitiveReturnTypeUseCase primitiveReturnTypeUseCase = (PrimitiveReturnTypeUseCase) useCase;
                final Map<String, Object> requestMap = (Map<String, Object>) map;
                final int intParameter = (int) requestMap.get(PARAMETER_MAP_PROPERTY_NAME);
                final PrimitiveReturnTypeRequest request = new PrimitiveReturnTypeRequest(intParameter);
                final int returnValue = primitiveReturnTypeUseCase.useCaseMethod(request);
                final Map<String, Object> responseMap = new HashMap<>();
                responseMap.put(RETURN_MAP_PROPERTY_NAME, returnValue);
                return responseMap;
            });
        };
        return testUseCase(USE_CASE_CLASS, EVENT_TYPE, messageBusSetup, instantiationFunction, deserializationEnhancer, serializationEnhancer,
                customCallingLogic, requestObject, expectedResponseMap);
    }
}
