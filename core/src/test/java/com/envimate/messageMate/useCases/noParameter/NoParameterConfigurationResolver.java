package com.envimate.messageMate.useCases.noParameter;

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

public class NoParameterConfigurationResolver extends AbstractTestConfigProvider {

    public static final Class<?> USE_CASE_CLASS = NoParameterUseCase.class;
    public static final EventType EVENT_TYPE = EventType.eventTypeFromString("NoParameterUseCase");
    private static final String RETURN_MAP_PROPERTY_NAME = "returnValue";

    @Override
    protected Class<?> forConfigClass() {
        return TestUseCase.class;
    }

    @Override
    protected Object testConfig() {
        final BiConsumer<MessageBus, TestEnvironment> messageBusSetup = (messageBus, testEnvironment) -> {
            messageBus.subscribe(USE_CASE_RESPONSE_EVENT_TYPE, s -> {
                if (testEnvironment.has(RESULT)) {
                    testEnvironment.addToListProperty(RESULT, s);
                } else {
                    testEnvironment.setProperty(RESULT, s);
                }
            });
        };
        final Map<String, Object> requestObject = new HashMap<>();
        final Supplier<Object> instantiationFunction = NoParameterUseCase::new;
        final Consumer<DeserializationStep1Builder> deserializationEnhancer = deserializationStepBuilder -> {
        };
        final Consumer<ResponseSerializationStep1Builder> serializationEnhancer = serializationStepBuilder -> {
            serializationStepBuilder.serializingResponseObjectsOfType(String.class).using(string -> {
                final Map<String, Object> map = new HashMap<>();
                map.put(RETURN_MAP_PROPERTY_NAME, string);
                return map;
            });
        };
        final Consumer<Step3Builder<?>> customCallingLogic = useCaseAdapterStep3Builder -> {
            useCaseAdapterStep3Builder.calling((useCase, map) -> {
                final NoParameterUseCase noParameterUseCase = (NoParameterUseCase) useCase;
                final String returnValue = noParameterUseCase.useCaseMethod();
                final Map<String, Object> responseMap = new HashMap<>();
                responseMap.put(RETURN_MAP_PROPERTY_NAME, returnValue);
                return responseMap;
            });
        };

        final Map<String, Object> expectedResponse = new HashMap<>();
        expectedResponse.put(RETURN_MAP_PROPERTY_NAME, NoParameterUseCase.NO_PARAMETER_USE_CASE_RETURN_VALUE);
        return testUseCase(USE_CASE_CLASS, EVENT_TYPE, messageBusSetup, instantiationFunction, deserializationEnhancer, serializationEnhancer,
                customCallingLogic, requestObject, expectedResponse);
    }
}
