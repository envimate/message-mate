package com.envimate.messageMate.useCases.primitiveReturnType;

import com.envimate.messageMate.messageBus.EventType;
import com.envimate.messageMate.shared.config.AbstractTestConfigProvider;
import com.envimate.messageMate.useCases.TestUseCase;
import com.envimate.messageMate.useCases.TestUseCaseBuilder;

import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.RESULT;
import static com.envimate.messageMate.useCases.UseCaseBusCallBuilder.aUseCasBusCall;
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
        final int expectedIntValue = 5;
        return TestUseCaseBuilder.aTestUseCase()
                .forUseCaseClass(USE_CASE_CLASS)
                .forEventType(EVENT_TYPE)
                .withRequestMap(map -> map.put(PARAMETER_MAP_PROPERTY_NAME, expectedIntValue))
                .withAParameterSerialization(Integer.class, (intValue, map) -> map.put(RETURN_MAP_PROPERTY_NAME, intValue))
                .withAUseCaseInvocationRequestSerialization(PrimitiveReturnTypeRequest.class, (r, map) -> map.put(PARAMETER_MAP_PROPERTY_NAME, r.getValue()))
                .withExpectedResponseMap(map -> map.put(RETURN_MAP_PROPERTY_NAME, expectedIntValue))
                .withParameterDeserialization(PrimitiveReturnTypeRequest.class, map -> {
                    return new PrimitiveReturnTypeRequest((int) map.get(PARAMETER_MAP_PROPERTY_NAME));
                })
                .withAUseCaseInvocationResponseDeserialization(Integer.class, map -> (int) map.get(RETURN_MAP_PROPERTY_NAME))
                .callingUseCaseWith((useCase, requestMap, responseMap) -> {
                    final PrimitiveReturnTypeUseCase primitiveReturnTypeUseCase = (PrimitiveReturnTypeUseCase) useCase;
                    final int intParameter = (int) requestMap.get(PARAMETER_MAP_PROPERTY_NAME);
                    final PrimitiveReturnTypeRequest request = new PrimitiveReturnTypeRequest(intParameter);
                    final int returnValue = primitiveReturnTypeUseCase.useCaseMethod(request);
                    responseMap.put(RETURN_MAP_PROPERTY_NAME, returnValue);
                })
                .instantiatingUseCaseWith(PrimitiveReturnTypeUseCase::new)
                .withSetup((messageBus, testEnvironment) -> {
                    messageBus.subscribe(USE_CASE_RESPONSE_EVENT_TYPE, v -> testEnvironment.setPropertyIfNotSet(RESULT, v));
                })
                .invokingOnTheUseCaseBusWith(aUseCasBusCall()
                        .withRequestData(new PrimitiveReturnTypeRequest(expectedIntValue))
                        .withSuccessResponseClass(Integer.class)
                        .withErrorResponseClass(Void.class)
                        .expectOnlySuccessPayload(expectedIntValue)
                        .expectedSuccessPayloadNotDeserialized(map -> map.put(RETURN_MAP_PROPERTY_NAME, expectedIntValue)))
                .build();
    }
}
