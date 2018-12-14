package com.envimate.messageMate.qcec.eventing.givenWhenThen;

import com.envimate.messageMate.messages.DeliveryFailedMessage;
import com.envimate.messageMate.messages.ExceptionInSubscriberException;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.qcec.shared.TestReceiver;
import com.envimate.messageMate.qcec.shared.TestValidation;
import com.envimate.messageMate.qcec.shared.testEvents.TestEvent;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.*;
import static lombok.AccessLevel.PRIVATE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RequiredArgsConstructor(access = PRIVATE)
public final class EventBusValidationBuilder {
    private final TestValidation validation;

    public static EventBusValidationBuilder expectItToReceivedByAll() {
        return new EventBusValidationBuilder(testEnvironment -> {
            ensureNoExceptionOccurred(testEnvironment);
            final TestEvent testEvent = testEnvironment.getPropertyAsType(TEST_OBJECT, TestEvent.class);
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


    public static EventBusValidationBuilder expectTheExceptionToBeDeliveredInADeliveryFailedMessage() {
        return new EventBusValidationBuilder(testEnvironment -> {
            ensureNoExceptionOccurred(testEnvironment);
            final List<TestReceiver<DeliveryFailedMessage>> receivers = (List<TestReceiver<DeliveryFailedMessage>>) testEnvironment.getProperty(EXPECTED_RECEIVERS);
            assertThat(receivers.size(), equalTo(1));
            final TestReceiver<DeliveryFailedMessage> errorReceiver = receivers.get(0);
            final List<Object> deliveryFailedMessages = errorReceiver.getReceivedObjects();
            assertThat(deliveryFailedMessages.size(), equalTo(1));
            final DeliveryFailedMessage deliveryFailedMessage = (DeliveryFailedMessage) deliveryFailedMessages.get(0);
            final Exception cause = deliveryFailedMessage.getCause();
            assertThat(cause.getClass(), equalTo(ExceptionInSubscriberException.class));
            final ExceptionInSubscriberException exceptionInSubscriberException = (ExceptionInSubscriberException) cause;
            final String expectedExceptionMessage = testEnvironment.getPropertyAsType(EXPECTED_EXCEPTION_MESSAGE, String.class);
            final Throwable originalException = exceptionInSubscriberException.getCause();
            assertThat(originalException.getMessage(), equalTo(expectedExceptionMessage));
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
