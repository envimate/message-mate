package com.envimate.messageMate.useCaseAdapter.voidReturn;

import com.envimate.messageMate.messageBus.EventType;
import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.shared.config.AbstractTestConfigProvider;
import com.envimate.messageMate.useCaseAdapter.TestUseCase;
import com.envimate.messageMate.useCaseAdapter.building.UseCaseAdapterDeserializationStep1Builder;
import com.envimate.messageMate.useCaseAdapter.building.UseCaseAdapterResponseSerializationStep1Builder;
import com.envimate.messageMate.useCaseAdapter.building.UseCaseAdapterStep3Builder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.EXPECTED_RESULT;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.RESULT;
import static com.envimate.messageMate.useCaseAdapter.TestUseCase.testUseCase;
import static com.envimate.messageMate.useCaseAdapter.UseCaseAdapterTestProperties.MESSAGE_FUNCTION_USED;

public class VoidReturnConfigurationResolver extends AbstractTestConfigProvider {

    public static final Class<?> USE_CASE_CLASS = VoidReturnUseCase.class;
    public static final EventType EVENT_TYPE = EventType.eventTypeFromString("ExceptionThrowingUseCase");
    private static final String PARAMETER_MAP_PROPERTY_NAME = "consumer";

    @Override
    protected Class<?> forConfigClass() {
        return TestUseCase.class;
    }

    @Override
    protected Object testConfig() {
        final BiConsumer<MessageBus, TestEnvironment> messageBusSetup = (messageBus, testEnvironment) -> {
        };
        final Function<TestEnvironment, Object> requestObjectFunction = testEnvironment -> {
            final String expectedResponse = "expected Response";
            final Consumer<Object> consumer = o -> {
                if (!testEnvironment.getPropertyAsType(MESSAGE_FUNCTION_USED, Boolean.class)) {
                    testEnvironment.setPropertyIfNotSet(EXPECTED_RESULT, expectedResponse);
                    testEnvironment.setPropertyIfNotSet(RESULT, expectedResponse);
                }
            };
            final Map<String, Object> requestObject = new HashMap<>();
            requestObject.put(PARAMETER_MAP_PROPERTY_NAME, consumer);
            return requestObject;
        };
        final Supplier<Object> instantiationFunction = VoidReturnUseCase::new;
        final Consumer<UseCaseAdapterDeserializationStep1Builder> deserializationEnhancer = deserializationStepBuilder -> {
            deserializationStepBuilder.mappingRequestsToUseCaseParametersOfType(CallbackTestRequest.class)
                    .using((targetType, map) -> CallbackTestRequest.callbackTestRequest((Consumer<Object>) map.get(PARAMETER_MAP_PROPERTY_NAME)));
        };

        final Consumer<UseCaseAdapterResponseSerializationStep1Builder> serializationEnhancer = serializationStepBuilder -> {
            serializationStepBuilder.serializingResponseObjectsOfTypeVoid().using(nullValue -> {
                return Collections.emptyMap();
            });
        };
        Consumer<UseCaseAdapterStep3Builder<?>> customCallingLogic = useCaseAdapterStep3Builder -> {
            useCaseAdapterStep3Builder.callingVoid((useCase, map) -> {
                final VoidReturnUseCase voidReturnUseCase = (VoidReturnUseCase) useCase;
                final Map<String, Object> requestMap = (Map<String, Object>) map;
                final Consumer<Object> consumer = (Consumer<Object>) requestMap.get(PARAMETER_MAP_PROPERTY_NAME);
                final CallbackTestRequest request = CallbackTestRequest.callbackTestRequest(consumer);
                voidReturnUseCase.useCaseMethod(request);
            });
        };
        final Function<TestEnvironment, Object> expectedResultSupplier = testEnvironment -> {
            if (testEnvironment.getPropertyAsType(MESSAGE_FUNCTION_USED, Boolean.class)) {
                return Collections.emptyMap();
            } else {
                return testEnvironment.getProperty(EXPECTED_RESULT);
            }
        };
        return testUseCase(USE_CASE_CLASS, EVENT_TYPE, messageBusSetup, instantiationFunction, deserializationEnhancer,
                serializationEnhancer, customCallingLogic, requestObjectFunction, expectedResultSupplier);
    }

}
