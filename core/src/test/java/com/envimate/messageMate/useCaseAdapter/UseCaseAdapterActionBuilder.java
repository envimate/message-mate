package com.envimate.messageMate.useCaseAdapter;

import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.messageFunction.MessageFunction;
import com.envimate.messageMate.messageFunction.MessageFunctionBuilder;
import com.envimate.messageMate.messageFunction.ResponseFuture;
import com.envimate.messageMate.qcec.shared.TestAction;
import lombok.RequiredArgsConstructor;

import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.MOCK;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.RESULT;
import static com.envimate.messageMate.useCaseAdapter.UseCaseAdapterTestProperties.MESSAGE_FUNCTION_USED;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class UseCaseAdapterActionBuilder {
    private final TestAction<TestUseCase> testAction;

    private static UseCaseAdapterActionBuilder asAction(final TestAction<TestUseCase> testAction) {
        return new UseCaseAdapterActionBuilder(testAction);
    }

    public static UseCaseAdapterActionBuilder theAssociatedEventIsSend() {
        return asAction((testUseCase, testEnvironment) -> {
            testEnvironment.setProperty(MESSAGE_FUNCTION_USED, false);
            final MessageBus messageBus = testEnvironment.getPropertyAsType(MOCK, MessageBus.class);
            testUseCase.performNecessaryResultSubscriptionsOn(messageBus, testEnvironment);
            final Object requestObject = testUseCase.getRequestObjectSupplier(testEnvironment);
            messageBus.send(requestObject);
            return null;
        });
    }

    public static UseCaseAdapterActionBuilder theRequestIsExecutedUsingAMessageFunction() {
        return asAction((testUseCase, testEnvironment) -> {
            testEnvironment.setProperty(MESSAGE_FUNCTION_USED, true);
            final MessageBus messageBus = testEnvironment.getPropertyAsType(MOCK, MessageBus.class);
            final Object requestObject = testUseCase.getRequestObjectSupplier(testEnvironment);
            final MessageFunction messageFunction = MessageFunctionBuilder.aMessageFunction(messageBus);
            final ResponseFuture responseFuture = messageFunction.request(requestObject);
            testEnvironment.setProperty(RESULT, responseFuture);
            return null;
        });
    }

    public TestAction<TestUseCase> build() {
        return testAction;
    }
}
