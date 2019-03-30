package com.envimate.messageMate.useCaseAdapter;

import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.qcec.shared.TestAction;
import com.envimate.messageMate.useCaseAdapter.useCases.noParameter.NoParameterUseCase;
import com.envimate.messageMate.useCaseAdapter.useCases.singleObjectParameter.TestUseCaseRequest;
import lombok.RequiredArgsConstructor;

import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.EXPECTED_RESULT;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.RESULT;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class UseCaseAdapterActionBuilder {
    private final TestAction<MessageBus> testAction;

    private static UseCaseAdapterActionBuilder asAction(final TestAction<MessageBus> testAction) {
        return new UseCaseAdapterActionBuilder(testAction);
    }

    public static UseCaseAdapterActionBuilder aTestEventIsSend() {
        final String expectedResponse = "test response";
        return sendTestUseCaseEvent(expectedResponse);
    }

    public static UseCaseAdapterActionBuilder theAssociatedEventIsSend() {
        final String expectedResponse = NoParameterUseCase.RETURN_VALUE;
        return sendTestUseCaseEvent(expectedResponse);
    }

    private static UseCaseAdapterActionBuilder sendTestUseCaseEvent(final String expectedResponse) {
        return asAction((messageBus, testEnvironment) -> {
            messageBus.subscribe(String.class, s -> {
                if (testEnvironment.has(RESULT)) {
                    testEnvironment.addToListProperty(RESULT, s);
                } else {
                    testEnvironment.setProperty(RESULT, s);
                }
            });
            testEnvironment.setProperty(EXPECTED_RESULT, expectedResponse);
            final TestUseCaseRequest request = TestUseCaseRequest.testUseCaseRequest(expectedResponse);
            messageBus.send(request);
            return null;
        });
    }

    public TestAction<MessageBus> build() {
        return testAction;
    }
}
