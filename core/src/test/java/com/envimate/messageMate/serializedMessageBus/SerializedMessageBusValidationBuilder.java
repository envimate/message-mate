package com.envimate.messageMate.serializedMessageBus;

import com.envimate.messageMate.messageBus.PayloadAndErrorPayload;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.qcec.shared.TestEnvironmentProperty;
import com.envimate.messageMate.qcec.shared.TestValidation;
import com.envimate.messageMate.shared.subscriber.TestSubscriber;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.TimeoutException;

import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.RESULT;
import static com.envimate.messageMate.serializedMessageBus.SerializedMessageBusTestProperties.SEND_DATA;
import static com.envimate.messageMate.serializedMessageBus.SerializedMessageBusTestProperties.SEND_ERROR_DATA;
import static com.envimate.messageMate.shared.validations.SharedTestValidations.*;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class SerializedMessageBusValidationBuilder {
    private final TestValidation testValidation;

    public static SerializedMessageBusValidationBuilder expectTheCorrectDataToBeReceived() {
        return new SerializedMessageBusValidationBuilder(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertCorrectDataToBeReceived(testEnvironment);
        });
    }

    public static SerializedMessageBusValidationBuilder expectTheDataAndTheErrorToBeReceived() {
        return new SerializedMessageBusValidationBuilder(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertCorrectDataToBeReceived(testEnvironment);
        });
    }

    public static SerializedMessageBusValidationBuilder expectToHaveWaitedUntilTheCorrectResponseWasReceived() {
        return new SerializedMessageBusValidationBuilder(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertReceivedResultEqualsExpected(testEnvironment);
        });
    }

    public static SerializedMessageBusValidationBuilder expectTheTimeoutToBeOccurred() {
        return new SerializedMessageBusValidationBuilder(testEnvironment -> {
            assertExceptionThrownOfType(testEnvironment, TimeoutException.class);
        });
    }

    private static void assertCorrectDataToBeReceived(final TestEnvironment testEnvironment) {
        final Object expectedPayload = testEnvironment.getProperty(SEND_DATA);
        final Object expectedErrorPayload;
        if (testEnvironment.has(SEND_ERROR_DATA)) {
            expectedErrorPayload = testEnvironment.getProperty(SEND_ERROR_DATA);
        } else {
            expectedErrorPayload = null;
        }
        final List<TestSubscriber<PayloadAndErrorPayload<?, ?>>> receivers = (List<TestSubscriber<PayloadAndErrorPayload<?, ?>>>) testEnvironment.getProperty(TestEnvironmentProperty.EXPECTED_RECEIVERS);
        for (TestSubscriber<PayloadAndErrorPayload<?, ?>> receiver : receivers) {
            final List<PayloadAndErrorPayload<?, ?>> receivedMessages = receiver.getReceivedMessages();
            assertCollectionOfSize(receivedMessages, 1);
            final PayloadAndErrorPayload<?, ?> payloadAndErrorPayload = receivedMessages.get(0);
            final Object payload = payloadAndErrorPayload.getPayload();
            assertEquals(payload, expectedPayload);
            final Object errorPayload = payloadAndErrorPayload.getErrorPayload();
            assertEquals(errorPayload, expectedErrorPayload);
        }
    }

    private static void assertReceivedResultEqualsExpected(final TestEnvironment testEnvironment) {
        final Object expectedPayload = testEnvironment.getProperty(SEND_DATA);
        final Object expectedErrorPayload;
        if (testEnvironment.has(SEND_ERROR_DATA)) {
            expectedErrorPayload = testEnvironment.getProperty(SEND_ERROR_DATA);
        } else {
            expectedErrorPayload = null;
        }
        final PayloadAndErrorPayload<?, ?> payloadAndErrorPayload = (PayloadAndErrorPayload<?, ?>) testEnvironment.getProperty(RESULT);
        final Object payload = payloadAndErrorPayload.getPayload();
        assertEquals(payload, expectedPayload);
        final Object errorPayload = payloadAndErrorPayload.getErrorPayload();
        assertEquals(errorPayload, expectedErrorPayload);
    }

    public TestValidation build() {
        return testValidation;
    }
}
