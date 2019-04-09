package com.envimate.messageMate.useCases.voidReturn;

import com.envimate.messageMate.messageBus.EventType;
import com.envimate.messageMate.shared.config.AbstractTestConfigProvider;
import com.envimate.messageMate.useCases.TestUseCase;
import com.envimate.messageMate.useCases.TestUseCaseBuilder;

import java.util.Collections;
import java.util.Objects;
import java.util.function.Consumer;

import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.EXPECTED_RESULT;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.RESULT;
import static com.envimate.messageMate.useCases.UseCaseBusCallBuilder.aUseCasBusCall;
import static com.envimate.messageMate.useCases.UseCaseInvocationTestProperties.MESSAGE_FUNCTION_USED;
import static com.envimate.messageMate.useCases.voidReturn.CallbackTestRequest.callbackTestRequest;

public class VoidReturnConfigurationResolver extends AbstractTestConfigProvider {

    public static final Class<?> USE_CASE_CLASS = VoidReturnUseCase.class;
    public static final EventType EVENT_TYPE = EventType.eventTypeFromString("ExceptionThrowingUseCase");
    private static final String PARAMETER_MAP_PROPERTY_NAME = "consumer";

    @Override
    protected Class<?> forConfigClass() {
        return TestUseCase.class;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Object testConfig() {
        return TestUseCaseBuilder.aTestUseCase()
                .forUseCaseClass(USE_CASE_CLASS)
                .forEventType(EVENT_TYPE)
                .withRequestProvider((testEnvironment, map) -> {
                    final String expectedResponse = "expected Response";
                    final Consumer<Object> consumer = o -> {
                        if (!testEnvironment.getPropertyAsType(MESSAGE_FUNCTION_USED, Boolean.class)) {
                            testEnvironment.setPropertyIfNotSet(EXPECTED_RESULT, expectedResponse);
                            testEnvironment.setPropertyIfNotSet(RESULT, expectedResponse);
                        }
                    };
                    map.put(PARAMETER_MAP_PROPERTY_NAME, consumer);
                    return map;
                })
                .withAParameterSerialization(Objects::isNull, (string, map) -> {
                })
                .withAParameterSerialization(CallbackTestRequest.class, (string, map) -> {
                })
                .withExpectedResponse((testEnvironment, map) -> {
                    if (testEnvironment.getPropertyAsType(MESSAGE_FUNCTION_USED, Boolean.class)) {
                        return Collections.emptyMap();
                    } else {
                        return testEnvironment.getProperty(EXPECTED_RESULT);
                    }
                })
                .withParameterDeserialization(CallbackTestRequest.class, map -> callbackTestRequest((Consumer<Object>) map.get(PARAMETER_MAP_PROPERTY_NAME)))
                .withAUseCaseInvocationResponseDeserialization((aClass, map) -> aClass.equals(Void.class), map -> null)
                .callingUseCaseWith((useCase, requestMap, responseMap) -> {
                    final VoidReturnUseCase voidReturnUseCase = (VoidReturnUseCase) useCase;
                    final Consumer<Object> consumer = (Consumer<Object>) requestMap.get(PARAMETER_MAP_PROPERTY_NAME);
                    final CallbackTestRequest request = callbackTestRequest(consumer);
                    voidReturnUseCase.useCaseMethod(request);
                })
                .instantiatingUseCaseWith(VoidReturnUseCase::new)
                .withSetup((messageBus, testEnvironment) -> {
                })
                .invokingOnTheUseCaseBusWith(aUseCasBusCall()
                        .withRequestData(callbackTestRequest(null))
                        .withSuccessResponseClass(Void.class)
                        .withErrorResponseClass(Void.class)
                        .expectOnlySuccessPayload(null)
                        .expectedSuccessPayloadNotDeserialized(map -> {
                        }))
                .build();
    }

}
