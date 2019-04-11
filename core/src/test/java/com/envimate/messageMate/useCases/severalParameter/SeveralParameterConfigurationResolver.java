package com.envimate.messageMate.useCases.severalParameter;

import com.envimate.messageMate.processingContext.EventType;
import com.envimate.messageMate.shared.config.AbstractTestConfigProvider;
import com.envimate.messageMate.useCases.TestUseCase;
import com.envimate.messageMate.useCases.TestUseCaseBuilder;

import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.RESULT;
import static com.envimate.messageMate.useCases.UseCaseBusCallBuilder.aUseCasBusCall;
import static com.envimate.messageMate.useCases.useCaseAdapter.UseCaseInvokingResponseEventType.USE_CASE_RESPONSE_EVENT_TYPE;

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
        final Object expectedObjectValue = new Object();
        final int expectedIntValue = 5;
        final boolean expectedBooleanValue = true;
        final String expectedStringValue = "abc";
        final SeveralParameterUseCaseResponse expectedResult = new SeveralParameterUseCaseResponse(expectedIntValue, expectedBooleanValue, expectedObjectValue, expectedStringValue);
        return TestUseCaseBuilder.aTestUseCase()
                .forUseCaseClass(USE_CASE_CLASS)
                .forEventType(EVENT_TYPE)
                .withRequestMap(map -> {
                    map.put(PARAMETER_MAP_PROPERTY_NAME_STRING, expectedStringValue);
                    map.put(PARAMETER_MAP_PROPERTY_NAME_OBJECT, expectedObjectValue);
                    map.put(PARAMETER_MAP_PROPERTY_NAME_INT, expectedIntValue);
                    map.put(PARAMETER_MAP_PROPERTY_NAME_BOOLEAN, expectedBooleanValue);
                })
                .withAParameterSerialization(SeveralParameterUseCaseResponse.class, (response, map) -> map.put(RETURN_MAP_PROPERTY_NAME, response))
                .withAUseCaseInvocationRequestSerialization(SeveralParameterUseCaseCombinedRequest.class, (r, map) -> {
                    map.put(PARAMETER_MAP_PROPERTY_NAME_STRING, r.stringParameter);
                    map.put(PARAMETER_MAP_PROPERTY_NAME_OBJECT, r.objectParameter);
                    map.put(PARAMETER_MAP_PROPERTY_NAME_INT, r.intParameter);
                    map.put(PARAMETER_MAP_PROPERTY_NAME_BOOLEAN, r.booleanParameter);
                })
                .withExpectedResponseMap(map -> {
                    final SeveralParameterUseCaseResponse response = expectedResult;
                    map.put(RETURN_MAP_PROPERTY_NAME, response);
                })
                .withParameterDeserialization(SeveralParameterUseCaseRequest1.class, map -> {
                    final int intParameter = (int) map.get(PARAMETER_MAP_PROPERTY_NAME_INT);
                    final Boolean booleanParameter = (Boolean) map.get(PARAMETER_MAP_PROPERTY_NAME_BOOLEAN);
                    return new SeveralParameterUseCaseRequest1(intParameter, booleanParameter);
                })
                .withParameterDeserialization(SeveralParameterUseCaseRequest2.class, map -> {
                    final String string = (String) map.get(PARAMETER_MAP_PROPERTY_NAME_STRING);
                    final Object objectParameter = map.get(PARAMETER_MAP_PROPERTY_NAME_OBJECT);
                    return new SeveralParameterUseCaseRequest2(string, objectParameter);
                })
                .withAUseCaseInvocationResponseDeserialization(SeveralParameterUseCaseResponse.class, map -> {
                    return (SeveralParameterUseCaseResponse) map.get(RETURN_MAP_PROPERTY_NAME);
                })
                .callingUseCaseWith((useCase, requestMap, responseMap) -> {
                    final SeveralParameterUseCase severalParameterUseCase = (SeveralParameterUseCase) useCase;
                    final String stringParameter = (String) requestMap.get(PARAMETER_MAP_PROPERTY_NAME_STRING);
                    final int intParameter = (int) requestMap.get(PARAMETER_MAP_PROPERTY_NAME_INT);
                    final Boolean booleanParameter = (Boolean) requestMap.get(PARAMETER_MAP_PROPERTY_NAME_BOOLEAN);
                    final Object objectParameter = requestMap.get(PARAMETER_MAP_PROPERTY_NAME_OBJECT);
                    final SeveralParameterUseCaseRequest1 request1 = new SeveralParameterUseCaseRequest1(intParameter, booleanParameter);
                    final SeveralParameterUseCaseRequest2 request2 = new SeveralParameterUseCaseRequest2(stringParameter, objectParameter);
                    final SeveralParameterUseCaseResponse response = severalParameterUseCase.useCaseMethod(request1, request2);
                    responseMap.put(RETURN_MAP_PROPERTY_NAME, response);
                })
                .instantiatingUseCaseWith(SeveralParameterUseCase::new)
                .withSetup((messageBus, testEnvironment) -> {
                    messageBus.subscribe(USE_CASE_RESPONSE_EVENT_TYPE, severalParameterUseCaseResponse -> {
                        testEnvironment.setPropertyIfNotSet(RESULT, severalParameterUseCaseResponse);
                    });
                })
                .invokingOnTheUseCaseBusWith(aUseCasBusCall()
                        .withRequestData(new SeveralParameterUseCaseCombinedRequest(expectedIntValue, expectedBooleanValue, expectedStringValue, expectedObjectValue))
                        .withSuccessResponseClass(SeveralParameterUseCaseResponse.class)
                        .withErrorResponseClass(Void.class)
                        .expectOnlySuccessPayload(expectedResult)
                        .expectedSuccessPayloadNotDeserialized(map -> {
                            map.put(RETURN_MAP_PROPERTY_NAME, expectedResult);
                        }))
                .build();
    }

}
