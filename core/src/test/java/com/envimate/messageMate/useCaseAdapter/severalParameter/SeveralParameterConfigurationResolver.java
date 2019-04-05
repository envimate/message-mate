package com.envimate.messageMate.useCaseAdapter.severalParameter;

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

public class SeveralParameterConfigurationResolver extends AbstractTestConfigProvider {

    public static final Class<?> USE_CASE_CLASS = SeveralParameterUseCase.class;
    public static final EventType EVENT_TYPE = EventType.eventTypeFromString("SeveralParameterUseCase");

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
        requestObject.put("string", "1");
        final Object expectedObject = new Object();
        requestObject.put("object", expectedObject);
        requestObject.put("int", 5);
        requestObject.put("boolean", true);
        final SeveralParameterUseCaseResponse expectedResult = createExpectedResponse("1", expectedObject, 5, true);
        final Supplier<Object> instantiationFunction = SeveralParameterUseCase::new;
        final Consumer<UseCaseAdapterDeserializationStep1Builder> deserializationEnhancer = deserializationStepBuilder -> {
            deserializationStepBuilder.mappingRequestsToUseCaseParametersOfType(SeveralParameterUseCaseRequest1.class)
                    .using((targetType, map) -> {
                        final int intParameter = (int) map.get("int");
                        final Boolean booleanParameter = (Boolean) map.get("boolean");
                        return new SeveralParameterUseCaseRequest1(intParameter, booleanParameter);
                    }).mappingRequestsToUseCaseParametersOfType(SeveralParameterUseCaseRequest2.class)
                    .using((targetType, map) -> {
                        final String string = (String) map.get("string");
                        final Object objectParameter = map.get("object");
                        return new SeveralParameterUseCaseRequest2(string, objectParameter);
                    });
        };
        final Consumer<UseCaseAdapterStep3Builder<?>> customCallingLogic = callingBuilder -> {
            callingBuilder.calling((useCase, map) -> {
                final SeveralParameterUseCase severalParameterUseCase = (SeveralParameterUseCase) useCase;
                final Map<String, Object> requestMap = (Map<String, Object>) map;
                final String stringParameter = (String) requestMap.get("string");
                final int intParameter = (int) requestMap.get("int");
                final Boolean booleanParameter = (Boolean) requestMap.get("boolean");
                final Object objectParameter = requestMap.get("object");
                final SeveralParameterUseCaseRequest1 request1 = new SeveralParameterUseCaseRequest1(intParameter, booleanParameter);
                final SeveralParameterUseCaseRequest2 request2 = new SeveralParameterUseCaseRequest2(stringParameter, objectParameter);
                final SeveralParameterUseCaseResponse response = severalParameterUseCase.useCaseMethod(request1, request2);
                return response;
            });
        };
        return testUseCase(USE_CASE_CLASS, EVENT_TYPE, messageBusSetup, instantiationFunction, deserializationEnhancer, customCallingLogic, requestObject, expectedResult);
    }

    private SeveralParameterUseCaseResponse createExpectedResponse(String expectedString, Object expectedObject, int expectedInt, boolean expectedBoolean) {
        return new SeveralParameterUseCaseResponse(expectedInt, expectedBoolean, expectedObject, expectedString);
    }
}
