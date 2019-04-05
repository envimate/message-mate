package com.envimate.messageMate.useCaseAdapter.exceptionThrowing;

import com.envimate.messageMate.channel.Channel;
import com.envimate.messageMate.messageBus.EventType;
import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.messageBus.MessageBusBuilder;
import com.envimate.messageMate.messageBus.exception.MessageBusExceptionHandler;
import com.envimate.messageMate.processingContext.ProcessingContext;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.shared.config.AbstractTestConfigProvider;
import com.envimate.messageMate.shared.subscriber.TestException;
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

public class ExceptionThrowingConfigurationResolver extends AbstractTestConfigProvider {

    public static final Class<?> USE_CASE_CLASS = ExceptionThrowingUseCase.class;
    public static final EventType EVENT_TYPE = EventType.eventTypeFromString("ExceptionThrowingUseCase");

    @Override
    protected Class<?> forConfigClass() {
        return TestUseCase.class;
    }

    @Override
    protected Object testConfig() {
        final BiConsumer<MessageBus, TestEnvironment> messageBusSetup = (messageBus, testEnvironment) -> {
            messageBus.onException(EVENT_TYPE, (request, e) -> {
                testEnvironment.setPropertyIfNotSet(RESULT, e);
            });
        };
        final TestException expectedResult = new TestException();
        final Map<String, Object> requestObject = new HashMap<>();
        requestObject.put("exception", expectedResult);
        final Supplier<Object> instantiationFunction = ExceptionThrowingUseCase::new;
        final Consumer<UseCaseAdapterDeserializationStep1Builder> deserializationEnhancer = deserializationStepBuilder -> {
            deserializationStepBuilder.mappingRequestsToUseCaseParametersOfType(ExceptionThrowingRequest.class)
                    .using((targetType, map) -> new ExceptionThrowingRequest((RuntimeException) map.get("exception")));
        };
        Consumer<UseCaseAdapterStep3Builder<?>> customCallingLogic = useCaseAdapterStep3Builder -> {
            useCaseAdapterStep3Builder.callingVoid((useCase, map) -> {
                final ExceptionThrowingUseCase exceptionthrowingusecase = (ExceptionThrowingUseCase) useCase;
                final Map<String, Object> requestMap = (Map<String, Object>) map;
                final RuntimeException exception = (RuntimeException) requestMap.get("exception");
                final ExceptionThrowingRequest request = new ExceptionThrowingRequest(exception);
                exceptionthrowingusecase.useCaseMethod(request);
            });
        };
        final Consumer<MessageBusBuilder> messageBusEnhancer = messageBusBuilder -> {
            messageBusBuilder.withExceptionHandler(new MessageBusExceptionHandler() {
                @Override
                public boolean shouldDeliveryChannelErrorBeHandledAndDeliveryAborted(ProcessingContext<?> message, Exception e, Channel<?> channel) {
                    return true;
                }

                @Override
                public void handleDeliveryChannelException(ProcessingContext<?> message, Exception e, Channel<?> channel) {
                    if (e != expectedResult) {
                        throw (RuntimeException) e;
                    }
                }

                @Override
                public void handleFilterException(ProcessingContext<?> message, Exception e, Channel<?> channel) {
                    if (e != expectedResult) {
                        throw (RuntimeException) e;
                    }
                }
            });
        };
        return testUseCase(USE_CASE_CLASS, EVENT_TYPE, messageBusSetup, instantiationFunction, deserializationEnhancer, customCallingLogic, requestObject, expectedResult, messageBusEnhancer);
    }
}
