package com.envimate.messageMate.useCases.exceptionThrowing;

import com.envimate.messageMate.channel.Channel;
import com.envimate.messageMate.messageBus.EventType;
import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.messageBus.MessageBusBuilder;
import com.envimate.messageMate.messageBus.exception.MessageBusExceptionHandler;
import com.envimate.messageMate.processingContext.ProcessingContext;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.shared.config.AbstractTestConfigProvider;
import com.envimate.messageMate.shared.subscriber.TestException;
import com.envimate.messageMate.useCases.TestUseCase;
import com.envimate.messageMate.useCases.building.DeserializationStep1Builder;
import com.envimate.messageMate.useCases.building.Step3Builder;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.RESULT;
import static com.envimate.messageMate.useCases.TestUseCase.testUseCase;
import static com.envimate.messageMate.useCases.UseCaseAdapterTestProperties.RETRIEVE_ERROR_FROM_FUTURE;
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
        final BiConsumer<MessageBus, TestEnvironment> messageBusSetup = (messageBus, testEnvironment) -> {
            messageBus.subscribeRaw(USE_CASE_RESPONSE_EVENT_TYPE, processingContext -> testEnvironment.setPropertyIfNotSet(RESULT, processingContext.getErrorPayload()));
        };
        final TestException expectedException = new TestException();
        final Map<String, Object> requestObject = new HashMap<>();
        requestObject.put(PARAMETER_MAP_PROPERTY_NAME, expectedException);
        final Supplier<Object> instantiationFunction = ExceptionThrowingUseCase::new;
        final Consumer<DeserializationStep1Builder> deserializationEnhancer = deserializationStepBuilder -> {
            deserializationStepBuilder.mappingRequestsToUseCaseParametersOfType(ExceptionThrowingRequest.class)
                    .using((targetType, map) -> new ExceptionThrowingRequest((RuntimeException) map.get(PARAMETER_MAP_PROPERTY_NAME)));
        };
        Consumer<Step3Builder<?>> customCallingLogic = useCaseAdapterStep3Builder -> {
            useCaseAdapterStep3Builder.callingVoid((useCase, map) -> {
                final ExceptionThrowingUseCase exceptionthrowingusecase = (ExceptionThrowingUseCase) useCase;
                final Map<String, Object> requestMap = (Map<String, Object>) map;
                final RuntimeException exception = (RuntimeException) requestMap.get(PARAMETER_MAP_PROPERTY_NAME);
                final ExceptionThrowingRequest request = new ExceptionThrowingRequest(exception);
                exceptionthrowingusecase.useCaseMethod(request);
            });
        };
        final BiConsumer<MessageBusBuilder, TestEnvironment> messageBusEnhancer = (messageBusBuilder,testEnvironment) -> {
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
        };
        return testUseCase(USE_CASE_CLASS, EVENT_TYPE, messageBusSetup, instantiationFunction, deserializationEnhancer, customCallingLogic, requestObject, requestObject, messageBusEnhancer);
    }
}
