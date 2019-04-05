package com.envimate.messageMate.useCaseAdapter.singleEventParameter;

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
        final Map<String, Object> requestObject = new HashMap<>();
        requestObject.put("message", expectedResponse);
        final Supplier<Object> instantiationFunction = SingleEventParameterUseCase::new;
        final Consumer<UseCaseAdapterDeserializationStep1Builder> deserializationEnhancer = deserializationStepBuilder -> {
            deserializationStepBuilder.mappingRequestsToUseCaseParametersOfType(SingleParameterEvent.class)
                    .using((targetType, map) -> SingleParameterEvent.testUseCaseRequest((String) map.get("message")));
        };
        Consumer<UseCaseAdapterStep3Builder<?>> customCallingLogic = useCaseAdapterStep3Builder -> {
            useCaseAdapterStep3Builder.calling((useCase, map) -> {
                final SingleEventParameterUseCase singleEventParameterUseCase = (SingleEventParameterUseCase) useCase;
                final Map<String, Object> requestMap = (Map<String, Object>) map;
                final String message = (String) requestMap.get("message");
                final SingleParameterEvent request = SingleParameterEvent.testUseCaseRequest(message);
                final String returnValue = singleEventParameterUseCase.useCaseMethod(request);
                return returnValue;
            });
        };
        return testUseCase(USE_CASE_CLASS, EVENT_TYPE, messageBusSetup, instantiationFunction, deserializationEnhancer, customCallingLogic, requestObject, expectedResponse);
    }
}
