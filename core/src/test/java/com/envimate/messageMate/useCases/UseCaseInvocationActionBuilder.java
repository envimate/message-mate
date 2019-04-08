package com.envimate.messageMate.useCases;

import com.envimate.messageMate.messageBus.EventType;
import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.messageBus.PayloadAndErrorPayload;
import com.envimate.messageMate.messageFunction.MessageFunction;
import com.envimate.messageMate.messageFunction.MessageFunctionBuilder;
import com.envimate.messageMate.messageFunction.ResponseFuture;
import com.envimate.messageMate.qcec.shared.TestAction;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.useCases.useCaseAdapter.UseCaseInvokingResponseEventType;
import com.envimate.messageMate.useCases.useCaseBus.UseCaseBus;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.*;
import static com.envimate.messageMate.shared.TestEventType.testEventType;
import static com.envimate.messageMate.useCases.UseCaseInvocationTestProperties.EVENT_TYPE;
import static com.envimate.messageMate.useCases.UseCaseInvocationTestProperties.MESSAGE_FUNCTION_USED;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class UseCaseInvocationActionBuilder {
    private final TestAction<TestUseCase> testAction;

    private static UseCaseInvocationActionBuilder asAction(final TestAction<TestUseCase> testAction) {
        return new UseCaseInvocationActionBuilder(testAction);
    }

    public static UseCaseInvocationActionBuilder theAssociatedEventIsSend() {
        return asAction((testUseCase, testEnvironment) -> {
            testEnvironment.setProperty(MESSAGE_FUNCTION_USED, false);
            final MessageBus messageBus = testEnvironment.getPropertyAsType(MOCK, MessageBus.class);
            testUseCase.performNecessaryResultSubscriptionsOn(messageBus, testEnvironment);
            final Object requestObject = testUseCase.getRequestObject(testEnvironment);
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
            messageBus.send(eventType, requestObject);
            return null;
        });
    }

    public static UseCaseInvocationActionBuilder anEventWithMissingMappingIsSend() {
        return asAction((testUseCase, testEnvironment) -> {
            testEnvironment.setProperty(MESSAGE_FUNCTION_USED, false);
            final MessageBus messageBus = testEnvironment.getPropertyAsType(MOCK, MessageBus.class);
            final Object requestObject = testUseCase.getRequestObject(testEnvironment);
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
            messageBus.subscribeRaw(UseCaseInvokingResponseEventType.USE_CASE_RESPONSE_EVENT_TYPE, processingContext -> {
                final Map<String, Object> map = (Map<String, Object>) processingContext.getErrorPayload();
                testEnvironment.setPropertyIfNotSet(EXCEPTION, map.get("Exception"));
            });
            messageBus.send(eventType, requestObject);
            return null;
        });
    }

    public static UseCaseInvocationActionBuilder theRequestIsExecutedUsingAMessageFunction() {
        return asAction((testUseCase, testEnvironment) -> {
            testEnvironment.setProperty(MESSAGE_FUNCTION_USED, true);
            final MessageBus messageBus = testEnvironment.getPropertyAsType(MOCK, MessageBus.class);
            final Object requestObject = testUseCase.getRequestObject(testEnvironment);
            final MessageFunction messageFunction = MessageFunctionBuilder.aMessageFunction(messageBus);
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
            final ResponseFuture responseFuture = messageFunction.request(eventType, requestObject);
            testEnvironment.setProperty(RESULT, responseFuture);
            return null;
        });
    }


    public static UseCaseInvocationActionBuilder theRequestIsInvokedOnTheUseCaseBus() {
        return invokeOnTheUseCaseFullySerialized(UseCaseBus::invokeAndWait);
    }

    public static UseCaseInvocationActionBuilder theRequestIsInvokedOnTheUseCaseBusWithTimeout() {
        return invokeOnTheUseCaseFullySerialized((useCaseBus, eventType, data, payloadClass, errorPayloadClass) -> {
            return useCaseBus.invokeAndWait(eventType, data, payloadClass, errorPayloadClass, 10, MILLISECONDS);
        });
    }


    public static UseCaseInvocationActionBuilder theRequestIsInvokedOnTheUseCaseBusNotDeserialized() {
        return invokeOnTheUseCaseNotDeserialized((useCaseBus, eventType, data, payloadClass, errorPayloadClass) -> {
            return useCaseBus.invokeAndWaitNotDeserialized(eventType, data);
        });
    }

    public static UseCaseInvocationActionBuilder theRequestIsInvokedOnTheUseCaseBusNotDeserializedWithTimeout() {
        return invokeOnTheUseCaseNotDeserialized((useCaseBus, eventType, data, payloadClass, errorPayloadClass) -> {
            return useCaseBus.invokeAndWaitNotDeserialized(eventType, data, 10, MILLISECONDS);
        });
    }

    public static UseCaseInvocationActionBuilder invokeOnTheUseCaseFullySerialized(final UseCaseBusInvocation call) {
        return asAction((testUseCase, testEnvironment) -> {
            final UseCaseBusCall useCaseBusCall = testUseCase.getUseCaseBusCall();
            final PayloadAndErrorPayload<?, ?> expectedResult = useCaseBusCall.getExpectedResult();
            testEnvironment.setPropertyIfNotSet(EXPECTED_RESULT, expectedResult);
            invokeOnTheUseCase(testEnvironment, testUseCase, call);
            return null;
        });
    }

    public static UseCaseInvocationActionBuilder invokeOnTheUseCaseNotDeserialized(final UseCaseBusInvocation call) {
        return asAction((testUseCase, testEnvironment) -> {
            final UseCaseBusCall useCaseBusCall = testUseCase.getUseCaseBusCall();
            final PayloadAndErrorPayload<?, ?> expectedResult = useCaseBusCall.getNotDeserializedExpectedResult();
            testEnvironment.setPropertyIfNotSet(EXPECTED_RESULT, expectedResult);
            invokeOnTheUseCase(testEnvironment, testUseCase, call);
            return null;
        });
    }

    public static void invokeOnTheUseCase(final TestEnvironment testEnvironment, final TestUseCase testUseCase, final UseCaseBusInvocation call) {
        final UseCaseBus useCaseBus = testEnvironment.getPropertyAsType(SUT, UseCaseBus.class);
        final UseCaseBusCall useCaseBusCall = testUseCase.getUseCaseBusCall();
        final EventType eventType = useCaseBusCall.getEventType();
        final Object data = useCaseBusCall.getData();
        final Class<?> payloadClass = useCaseBusCall.getPayloadClass();
        final Class<?> errorPayloadClass = useCaseBusCall.getErrorPayloadClass();
        final PayloadAndErrorPayload<?, ?> result;
        try {
            result = call.invoke(useCaseBus, eventType, data, payloadClass, errorPayloadClass);
            testEnvironment.setPropertyIfNotSet(RESULT, result);

        } catch (final InterruptedException | ExecutionException | TimeoutException e) {
            testEnvironment.setPropertyIfNotSet(EXCEPTION, e);
        }
    }

    public TestAction<TestUseCase> build() {
        return testAction;
    }

    private interface UseCaseBusInvocation {
        PayloadAndErrorPayload<?, ?> invoke(UseCaseBus useCaseBus, EventType eventType, Object data, Class<?> payloadClass,
                                            Class<?> errorPayloadClass) throws InterruptedException, ExecutionException, TimeoutException;
    }
}
