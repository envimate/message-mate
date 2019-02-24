package com.envimate.messageMate.qcec.eventing.givenWhenThen;

import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.qcec.shared.TestReceiver;
import com.envimate.messageMate.qcec.shared.TestValidation;
import com.envimate.messageMate.qcec.shared.testEvents.TestEvent;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.*;
import static com.envimate.messageMate.shared.validations.SharedTestValidations.assertExceptionThrownOfType;
import static lombok.AccessLevel.PRIVATE;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RequiredArgsConstructor(access = PRIVATE)
public final class EventBusValidationBuilder {
    private final TestValidation validation;

    public static EventBusValidationBuilder expectItToReceivedByAll() {
        return new EventBusValidationBuilder(testEnvironment -> {
            ensureNoExceptionOccurred(testEnvironment);
            final TestEvent testEvent = testEnvironment.getPropertyAsType(TEST_OBJECT, TestEvent.class);
            @SuppressWarnings("unchecked")
            final List<TestReceiver<TestEvent>> receivers = (List<TestReceiver<TestEvent>>) testEnvironment.getProperty(EXPECTED_RECEIVERS);
            for (final TestReceiver<TestEvent> receiver : receivers) {
                assertTrue(receiver.hasReceived(testEvent));
            }
        });
    }

    public static EventBusValidationBuilder expectTheEventToBeReceivedByAllRemainingSubscribers() {
        return expectItToReceivedByAll();
    }

    public static EventBusValidationBuilder expectNoException() {
        return new EventBusValidationBuilder(EventBusValidationBuilder::ensureNoExceptionOccurred);
    }


    public static EventBusValidationBuilder expectTheException(final Class<?> expectedExceptionClass) {
        return new EventBusValidationBuilder(testEnvironment -> {
            assertExceptionThrownOfType(testEnvironment, expectedExceptionClass);
        });
    }

    private static void ensureNoExceptionOccurred(final TestEnvironment testEnvironment) {
        final boolean exceptionOccurred = testEnvironment.has(EXCEPTION);
        assertFalse(exceptionOccurred);
    }

    public TestValidation build() {
        return validation;
    }
}
