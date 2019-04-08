package com.envimate.messageMate.useCases.exceptionThrowing;

import com.envimate.messageMate.channel.Channel;
import com.envimate.messageMate.messageBus.EventType;
import com.envimate.messageMate.messageBus.exception.MessageBusExceptionHandler;
import com.envimate.messageMate.processingContext.ProcessingContext;
import com.envimate.messageMate.shared.config.AbstractTestConfigProvider;
import com.envimate.messageMate.shared.subscriber.TestException;
import com.envimate.messageMate.useCases.TestUseCase;
import com.envimate.messageMate.useCases.TestUseCaseBuilder;

import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.RESULT;
import static com.envimate.messageMate.useCases.UseCaseBusCallBuilder.aUseCasBusCall;
import static com.envimate.messageMate.useCases.UseCaseInvocationTestProperties.RETRIEVE_ERROR_FROM_FUTURE;
import static com.envimate.messageMate.useCases.useCaseAdapter.UseCaseInvokingResponseEventType.USE_CASE_RESPONSE_EVENT_TYPE;

public class ExceptionThrowingConfigurationResolver extends AbstractTestConfigProvider {

    public static final Class<?> USE_CASE_CLASS = ExceptionThrowingUseCase.class;
    public static final EventType EVENT_TYPE = EventType.eventTypeFromString("ExceptionThrowingUseCase");
    private static final String PARAMETER_MAP_PROPERTY_NAME = "Exception";

    @Override
    protected Class<?> forConfigClass() {
        return TestUseCase.class;
    }

    @Override
    protected Object testConfig() {
        final TestException expectedException = new TestException();
        return TestUseCaseBuilder.aTestUseCase()
                .forUseCaseClass(USE_CASE_CLASS)
                .forEventType(EVENT_TYPE)
                .withRequestMap(map -> map.put(PARAMETER_MAP_PROPERTY_NAME, expectedException))
                .withAUseCaseInvocationRequestSerialization(ExceptionThrowingRequest.class, (e, map) -> map.put(PARAMETER_MAP_PROPERTY_NAME, e.getExceptionToThrow()))
                .withExpectedResponseMap(map -> map.put(PARAMETER_MAP_PROPERTY_NAME, expectedException))
                .withParameterDeserialization(ExceptionThrowingRequest.class, map -> new ExceptionThrowingRequest((RuntimeException) map.get(PARAMETER_MAP_PROPERTY_NAME)))
                .withAUseCaseInvocationResponseDeserialization(TestException.class, map -> (TestException) map.get("Exception"))
                .callingUseCaseWith((useCase, requestMap, responseMap) -> {
                    final ExceptionThrowingUseCase exceptionthrowingusecase = (ExceptionThrowingUseCase) useCase;
                    final RuntimeException exception = (RuntimeException) requestMap.get(PARAMETER_MAP_PROPERTY_NAME);
                    final ExceptionThrowingRequest request = new ExceptionThrowingRequest(exception);
                    exceptionthrowingusecase.useCaseMethod(request);
                })
                .instantiatingUseCaseWith(ExceptionThrowingUseCase::new)
                .withSetup((messageBus, testEnvironment) -> {
                    messageBus.subscribeRaw(USE_CASE_RESPONSE_EVENT_TYPE, processingContext -> testEnvironment.setPropertyIfNotSet(RESULT, processingContext.getErrorPayload()));
                })
                .withMessageBusEnhancer((messageBusBuilder, testEnvironment) -> {
                    testEnvironment.setPropertyIfNotSet(RETRIEVE_ERROR_FROM_FUTURE, true);
                    messageBusBuilder.withExceptionHandler(new MessageBusExceptionHandler() {
                        @Override
                        public boolean shouldDeliveryChannelErrorBeHandledAndDeliveryAborted(ProcessingContext<?> message, Exception e, Channel<?> channel) {
                            return true;
                        }

                        @Override
                        public void handleDeliveryChannelException(ProcessingContext<?> message, Exception e, Channel<?> channel) {
                            if (e != expectedException) {
                                throw (RuntimeException) e;
                            }
                        }

                        @Override
                        public void handleFilterException(ProcessingContext<?> message, Exception e, Channel<?> channel) {
                            if (e != expectedException) {
                                throw (RuntimeException) e;
                            }
                        }
                    });
                })

                .invokingOnTheUseCaseBusWith(aUseCasBusCall()
                        .withRequestData(new ExceptionThrowingRequest(expectedException))
                        .withSuccessResponseClass(Void.class)
                        .withErrorResponseClass(TestException.class)
                        .expectOnlyErrorPayload(expectedException)
                        .expectedErrorPayloadNotDeserialized(map -> map.put("Exception", expectedException)))
                .build();

    }
}
