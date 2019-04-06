package com.envimate.messageMate.useCaseAdapter.singleEventParameter;

import com.envimate.messageMate.messageBus.EventType;
import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.shared.config.AbstractTestConfigProvider;
import com.envimate.messageMate.useCaseAdapter.TestUseCase;
import com.envimate.messageMate.useCaseAdapter.building.UseCaseAdapterDeserializationStep1Builder;
import com.envimate.messageMate.useCaseAdapter.building.UseCaseAdapterResponseSerializationStep1Builder;
import com.envimate.messageMate.useCaseAdapter.building.UseCaseAdapterStep3Builder;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.RESULT;
import static com.envimate.messageMate.useCaseAdapter.TestUseCase.testUseCase;
import static com.envimate.messageMate.useCaseAdapter.UseCaseInvokingResponseEventType.USE_CASE_RESPONSE_EVENT_TYPE;

public class SingleEventParameterConfigurationResolver extends AbstractTestConfigProvider {
    private static final Class<SingleEventParameterUseCase> USE_CASE_CLASS = SingleEventParameterUseCase.class;
    private static final EventType EVENT_TYPE = EventType.eventTypeFromString("ExceptionThrowingUseCase");
    private static final String PARAMETER_MAP_PROPERTY_NAME = "int";
    private static final String RETURN_MAP_PROPERTY_NAME = "returnValue";

    @Override
    protected Class<?> forConfigClass() {
        return TestUseCase.class;
    }

    @Override
    protected Object testConfig() {
        final String response = "expected Response";
        final Map<String, Object> expectedResponse = new HashMap<>();
        expectedResponse.put(RETURN_MAP_PROPERTY_NAME, response);
        final BiConsumer<MessageBus, TestEnvironment> messageBusSetup = (messageBus, testEnvironment) -> {
            messageBus.subscribe(USE_CASE_RESPONSE_EVENT_TYPE, s -> testEnvironment.setPropertyIfNotSet(RESULT, s));
        };
        final Map<String, Object> requestObject = new HashMap<>();
        requestObject.put(PARAMETER_MAP_PROPERTY_NAME, response);
        final Supplier<Object> instantiationFunction = SingleEventParameterUseCase::new;
        final Consumer<UseCaseAdapterDeserializationStep1Builder> deserializationEnhancer = deserializationStepBuilder -> {
            deserializationStepBuilder.mappingRequestsToUseCaseParametersOfType(SingleParameterEvent.class)
                    .using((targetType, map) -> SingleParameterEvent.testUseCaseRequest((String) map.get(PARAMETER_MAP_PROPERTY_NAME)));
        };
        final Consumer<UseCaseAdapterResponseSerializationStep1Builder> serializationEnhancer = serializationStepBuilder -> {
            serializationStepBuilder.serializingResponseObjectsOfType(String.class).using(string -> {
                final Map<String, Object> map = new HashMap<>();
                map.put(RETURN_MAP_PROPERTY_NAME, string);
                return map;
            });
        };
        Consumer<UseCaseAdapterStep3Builder<?>> customCallingLogic = useCaseAdapterStep3Builder -> {
            useCaseAdapterStep3Builder.calling((useCase, map) -> {
                final SingleEventParameterUseCase singleEventParameterUseCase = (SingleEventParameterUseCase) useCase;
                final Map<String, Object> requestMap = (Map<String, Object>) map;
                final String message = (String) requestMap.get(PARAMETER_MAP_PROPERTY_NAME);
                final SingleParameterEvent request = SingleParameterEvent.testUseCaseRequest(message);
                final String returnValue = singleEventParameterUseCase.useCaseMethod(request);
                final Map<String, Object> responseMap = new HashMap<>();
                responseMap.put(RETURN_MAP_PROPERTY_NAME, returnValue);
                return responseMap;
            });
        };
        return testUseCase(USE_CASE_CLASS, EVENT_TYPE, messageBusSetup, instantiationFunction, deserializationEnhancer, serializationEnhancer,
                customCallingLogic, requestObject, expectedResponse);
    }
}
