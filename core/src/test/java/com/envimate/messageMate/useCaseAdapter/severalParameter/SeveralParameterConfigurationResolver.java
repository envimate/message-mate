package com.envimate.messageMate.useCaseAdapter.severalParameter;

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

public class SeveralParameterConfigurationResolver extends AbstractTestConfigProvider {

    public static final Class<?> USE_CASE_CLASS = SeveralParameterUseCase.class;
    public static final EventType EVENT_TYPE = EventType.eventTypeFromString("SeveralParameterUseCase");
    private static final String PARAMETER_MAP_PROPERTY_NAME_INT = "int";
    private static final String PARAMETER_MAP_PROPERTY_NAME_STRING = "string";
    private static final String PARAMETER_MAP_PROPERTY_NAME_BOOLEAN = "boolean";
    private static final String PARAMETER_MAP_PROPERTY_NAME_OBJECT = "object";
    private static final String RETURN_MAP_PROPERTY_NAME = "returnValue";

    @Override
    protected Class<?> forConfigClass() {
        return TestUseCase.class;
    }

    @Override
    protected Object testConfig() {
        final BiConsumer<MessageBus, TestEnvironment> messageBusSetup = (messageBus, testEnvironment) -> {
            messageBus.subscribe(USE_CASE_RESPONSE_EVENT_TYPE, severalParameterUseCaseResponse -> {
                testEnvironment.setPropertyIfNotSet(RESULT, severalParameterUseCaseResponse);
            });
        };
        final Map<String, Object> requestObject = new HashMap<>();
        requestObject.put(PARAMETER_MAP_PROPERTY_NAME_STRING, "1");
        final Object expectedObject = new Object();
        requestObject.put(PARAMETER_MAP_PROPERTY_NAME_OBJECT, expectedObject);
        requestObject.put(PARAMETER_MAP_PROPERTY_NAME_INT, 5);
        requestObject.put(PARAMETER_MAP_PROPERTY_NAME_BOOLEAN, true);
        final Map<String, Object> expectedResult = createExpectedResponse("1", expectedObject, 5, true);
        final Supplier<Object> instantiationFunction = SeveralParameterUseCase::new;
        final Consumer<UseCaseAdapterDeserializationStep1Builder> deserializationEnhancer = deserializationStepBuilder -> {
            deserializationStepBuilder.mappingRequestsToUseCaseParametersOfType(SeveralParameterUseCaseRequest1.class)
                    .using((targetType, map) -> {
                        final int intParameter = (int) map.get(PARAMETER_MAP_PROPERTY_NAME_INT);
                        final Boolean booleanParameter = (Boolean) map.get(PARAMETER_MAP_PROPERTY_NAME_BOOLEAN);
                        return new SeveralParameterUseCaseRequest1(intParameter, booleanParameter);
                    }).mappingRequestsToUseCaseParametersOfType(SeveralParameterUseCaseRequest2.class)
                    .using((targetType, map) -> {
                        final String string = (String) map.get(PARAMETER_MAP_PROPERTY_NAME_STRING);
                        final Object objectParameter = map.get(PARAMETER_MAP_PROPERTY_NAME_OBJECT);
                        return new SeveralParameterUseCaseRequest2(string, objectParameter);
                    });
        };
        final Consumer<UseCaseAdapterResponseSerializationStep1Builder> serializationEnhancer = serializationStepBuilder -> {
            serializationStepBuilder.serializingResponseObjectsOfType(SeveralParameterUseCaseResponse.class).using(response -> {
                final Map<String, Object> map = new HashMap<>();
                map.put(RETURN_MAP_PROPERTY_NAME, response);
                return map;
            });
        };
        final Consumer<UseCaseAdapterStep3Builder<?>> customCallingLogic = callingBuilder -> {
            callingBuilder.calling((useCase, map) -> {
                final SeveralParameterUseCase severalParameterUseCase = (SeveralParameterUseCase) useCase;
                final Map<String, Object> requestMap = (Map<String, Object>) map;
                final String stringParameter = (String) requestMap.get(PARAMETER_MAP_PROPERTY_NAME_STRING);
                final int intParameter = (int) requestMap.get(PARAMETER_MAP_PROPERTY_NAME_INT);
                final Boolean booleanParameter = (Boolean) requestMap.get(PARAMETER_MAP_PROPERTY_NAME_BOOLEAN);
                final Object objectParameter = requestMap.get(PARAMETER_MAP_PROPERTY_NAME_OBJECT);
                final SeveralParameterUseCaseRequest1 request1 = new SeveralParameterUseCaseRequest1(intParameter, booleanParameter);
                final SeveralParameterUseCaseRequest2 request2 = new SeveralParameterUseCaseRequest2(stringParameter, objectParameter);
                final SeveralParameterUseCaseResponse response = severalParameterUseCase.useCaseMethod(request1, request2);
                final Map<String, Object> responseMap = new HashMap<>();
                responseMap.put(RETURN_MAP_PROPERTY_NAME, response);
                return responseMap;
            });
        };
        return testUseCase(USE_CASE_CLASS, EVENT_TYPE, messageBusSetup, instantiationFunction, deserializationEnhancer, serializationEnhancer, customCallingLogic, requestObject, expectedResult);
    }

    private Map<String, Object> createExpectedResponse(String expectedString, Object expectedObject, int expectedInt, boolean expectedBoolean) {
        final Map<String, Object> expectedResponse = new HashMap<>();
        final SeveralParameterUseCaseResponse response = new SeveralParameterUseCaseResponse(expectedInt, expectedBoolean, expectedObject, expectedString);
        expectedResponse.put(RETURN_MAP_PROPERTY_NAME, response);
        return expectedResponse;
    }
}
