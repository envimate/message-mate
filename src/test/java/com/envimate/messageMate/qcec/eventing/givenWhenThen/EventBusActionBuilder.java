package com.envimate.messageMate.qcec.eventing.givenWhenThen;

import com.envimate.messageMate.error.DeliveryFailedMessage;
import com.envimate.messageMate.qcec.shared.TestAction;
import com.envimate.messageMate.qcec.shared.TestReceiver;
import com.envimate.messageMate.qcec.shared.testEvents.TestEvent;
import com.envimate.messageMate.subscribing.SubscriptionId;
import lombok.RequiredArgsConstructor;

import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.*;
import static com.envimate.messageMate.qcec.shared.TestReceiver.aTestReceiver;
import static com.envimate.messageMate.qcec.shared.testEvents.TestEvent.testEvent;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class EventBusActionBuilder {
    private final TestAction<TestEventBus> testAction;

    public static EventBusActionBuilder anEventIsPublishedToSeveralReceiver() {
        return new EventBusActionBuilder((testEventBus, testEnvironment) -> {
            final int numberOfReceivers = 5;
            for (int i = 0; i < numberOfReceivers; i++) {
                final TestReceiver<TestEvent> receiver = aTestReceiver();
                testEventBus.reactTo(TestEvent.class, receiver);
                testEnvironment.addToListProperty(EXPECTED_RECEIVERS, receiver);
            }
            final TestEvent testEvent = testEvent();
            testEnvironment.setProperty(TEST_OBJECT, testEvent);
            testEventBus.publish(testEvent);
            return null;
        });
    }

    public static EventBusActionBuilder anEventIsPublishedToNoOne() {
        return new EventBusActionBuilder((testEventBus, testEnvironment) -> {
            final TestEvent testEvent = testEvent();
            testEnvironment.setProperty(TEST_OBJECT, testEvent);
            testEventBus.publish(testEvent);
            return null;
        });
    }

    public static EventBusActionBuilder anReceiverUnsubscribes() {
        return new EventBusActionBuilder((testEventBus, testEnvironment) -> {
            final SubscriptionId subscriptionId = testEventBus.reactTo(TestEvent.class, e -> {
                throw new RuntimeException("This receiver should not be called");
            });

            final TestReceiver<TestEvent> receiver = aTestReceiver();
            testEventBus.reactTo(TestEvent.class, receiver);
            testEnvironment.addToListProperty(EXPECTED_RECEIVERS, receiver);

            testEventBus.unsubscribe(subscriptionId);

            final TestEvent testEvent = testEvent();
            testEnvironment.setProperty(TEST_OBJECT, testEvent);
            testEventBus.publish(testEvent);
            return null;
        });
    }

    public static EventBusActionBuilder anEventIsDeliveredToAnErrorThrowingReceiver() {
        return new EventBusActionBuilder((testEventBus, testEnvironment) -> {
            final String expectedExceptionMessage = "An expected exception";
            testEventBus.reactTo(TestEvent.class, e -> {
                throw new RuntimeException(expectedExceptionMessage);
            });
            testEnvironment.setProperty(EXPECTED_EXCEPTION_MESSAGE, expectedExceptionMessage);

            @SuppressWarnings("rawtypes")
            final TestReceiver<DeliveryFailedMessage> receiver = aTestReceiver();
            testEventBus.reactTo(DeliveryFailedMessage.class, receiver);
            testEnvironment.addToListProperty(EXPECTED_RECEIVERS, receiver);

            final TestEvent testEvent = testEvent();
            testEnvironment.setProperty(TEST_OBJECT, testEvent);
            testEventBus.publish(testEvent);
            return null;
        });
    }

    public TestAction<TestEventBus> build() {
        return testAction;
    }
}
