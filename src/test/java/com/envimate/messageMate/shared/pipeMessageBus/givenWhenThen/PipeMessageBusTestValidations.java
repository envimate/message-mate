package com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen;

import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.shared.subscriber.SimpleTestSubscriber;
import com.envimate.messageMate.shared.subscriber.TestSubscriber;
import com.envimate.messageMate.shared.testMessages.TestMessageOfInterest;
import com.envimate.messageMate.subscribing.Subscriber;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.EXPECTED_RECEIVERS;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.RESULT;
import static lombok.AccessLevel.PRIVATE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

@RequiredArgsConstructor(access = PRIVATE)
public final class PipeMessageBusTestValidations {

    public static void assertExpectedReceiverReceivedSingleMessage(final TestEnvironment testEnvironment) {
        final List<SimpleTestSubscriber<?>> receivers = getExpectedReceiversAsSubscriber(testEnvironment);
        for (final SimpleTestSubscriber<?> receiver : receivers) {
            final List<?> receivedMessages = receiver.getReceivedMessages();
            assertEquals(receivedMessages.size(), 1);
            final Object receivedMessage = receivedMessages.get(0);
            final Object expectedMessage = testEnvironment.getProperty(PipeMessageBusTestProperties.SINGLE_SEND_MESSAGE);
            assertEquals(receivedMessage, expectedMessage);
        }
    }

    public static void assertExpectedReceiverReceivedAllMessages(final TestEnvironment testEnvironment) {
        final List<?> expectedReceivedMessages = testEnvironment.getPropertyAsType(PipeMessageBusTestProperties.MESSAGES_SEND_OF_INTEREST, List.class);
        final List<SimpleTestSubscriber<?>> receivers = getExpectedReceiversAsSubscriber(testEnvironment);
        for (final SimpleTestSubscriber<?> receiver : receivers) {
            final List<?> receivedMessages = receiver.getReceivedMessages();
            assertEquals(receivedMessages.size(), expectedReceivedMessages.size());
            final Object[] ar = expectedReceivedMessages.toArray();
            assertThat(receivedMessages, containsInAnyOrder(ar));
        }
    }

    public static void assertExpectedReceiverReceivedAllMessages(final PipeMessageBusSutActions sutActions, final TestEnvironment testEnvironment) {
        final List<Subscriber<?>> expectedSubscriber = getExpectedSubscriber(testEnvironment);
        final List<Subscriber<?>> allSubscribers = sutActions.getAllSubscribers();
        assertThat(allSubscribers, containsInAnyOrder(expectedSubscriber.toArray()));
    }

    public static void assertAllMessagesHaveContentChanged(final PipeMessageBusSutActions sutActions, final TestEnvironment testEnvironment) {
        final List<?> expectedMessages = (List<?>) testEnvironment.getProperty(PipeMessageBusTestProperties.MESSAGES_SEND_OF_INTEREST);
        final List<Subscriber<?>> subscribers = sutActions.getAllSubscribers();
        final String expectedContent = testEnvironment.getPropertyAsType(PipeMessageBusTestProperties.EXPECTED_CHANGED_CONTENT, String.class);
        for (final Subscriber<?> subscriber : subscribers) {
            final TestSubscriber<TestMessageOfInterest> testSubscriber = castToTestSubscriber(subscriber);
            final List<TestMessageOfInterest> receivedMessages = testSubscriber.getReceivedMessages();
            assertThat(expectedMessages.size(), equalTo(receivedMessages.size()));
            for (final TestMessageOfInterest receivedMessage : receivedMessages) {
                assertThat(receivedMessage.content, equalTo(expectedContent));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static TestSubscriber<TestMessageOfInterest> castToTestSubscriber(final Subscriber<?> subscriber) {
        return (TestSubscriber<TestMessageOfInterest>) subscriber;
    }

    public static void assertReceiverReceivedOnlyValidMessages(final TestEnvironment testEnvironment) {
        final List<?> expectedReceivedMessages = (List<?>) testEnvironment.getProperty(PipeMessageBusTestProperties.MESSAGES_SEND);
        final List<SimpleTestSubscriber<?>> receivers = getExpectedReceiversAsSubscriber(testEnvironment);
        for (final SimpleTestSubscriber<?> receiver : receivers) {
            final List<?> receivedMessages = receiver.getReceivedMessages();
            assertThat(receivedMessages.size(), equalTo(expectedReceivedMessages.size()));
            for (final Object receivedMessage : receivedMessages) {
                if (!(receivedMessage instanceof TestMessageOfInterest)) {
                    fail("Found an invalid message. Expected only messages of type " + TestMessageOfInterest.class);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static void assertNumberOfMessagesReceived(final TestEnvironment testEnvironment, final int expectedNumberOfDeliveredMessages) {
        final List<TestSubscriber<?>> receiver;
        if (testEnvironment.has(PipeMessageBusTestProperties.SINGLE_RECEIVER)) {
            final TestSubscriber<?> singleSubscriber = testEnvironment.getPropertyAsType(PipeMessageBusTestProperties.SINGLE_RECEIVER, TestSubscriber.class);
            receiver = Collections.singletonList(singleSubscriber);
        } else {
            receiver = (List<TestSubscriber<?>>) testEnvironment.getPropertyAsType(EXPECTED_RECEIVERS, List.class);
        }
        for (final TestSubscriber<?> currentReceiver : receiver) {
            final List<?> receivedMessages = currentReceiver.getReceivedMessages();
            assertEquals(expectedNumberOfDeliveredMessages, receivedMessages.size());
        }
    }

    public static void assertSutWasShutdownInTime(final PipeMessageBusSutActions sutActions, final TestEnvironment testEnvironment) {
        final boolean wasTerminatedInTime = testEnvironment.getPropertyAsType(RESULT, Boolean.class);
        final boolean isShutdown = sutActions.isShutdown(testEnvironment);
        assertTrue(isShutdown);
        assertTrue(wasTerminatedInTime);
    }

    public static void assertSutIsShutdown(final PipeMessageBusSutActions sutActions, final TestEnvironment testEnvironment) {
        final boolean isShutdown = sutActions.isShutdown(testEnvironment);
        assertTrue(isShutdown);
    }

    public static void assertEachMessagesToBeReceivedByOnlyOneSubscriber(final TestEnvironment testEnvironment) {
        final List<?> expectedMessages = (List<?>) testEnvironment.getProperty(PipeMessageBusTestProperties.MESSAGES_SEND_OF_INTEREST);
        final List<SimpleTestSubscriber<?>> receivers = getPotentialReceiver(testEnvironment);
        for (final Object expectedMessage : expectedMessages) {
            final List<SimpleTestSubscriber<?>> subscribersThatReceivedMessage = subscribersThatReceivedMessage(receivers, expectedMessage);
            assertThat(subscribersThatReceivedMessage.size(), equalTo(1));
        }
    }

    private static List<SimpleTestSubscriber<?>> subscribersThatReceivedMessage(final List<SimpleTestSubscriber<?>> receivers, final Object expectedMessage) {
        return receivers.stream()
                .filter(subscriber -> subscriber.getReceivedMessages().contains(expectedMessage))
                .collect(Collectors.toList());
    }

    public static void assertResultEqualToExpectedFilter(final TestEnvironment testEnvironment) {
        final List<?> expectedFilter = (List<?>) testEnvironment.getProperty(PipeMessageBusTestProperties.EXPECTED_FILTER);
        final List<?> list = testEnvironment.getPropertyAsType(RESULT, List.class);
        assertThat(list, containsInAnyOrder(expectedFilter.toArray()));
    }

    public static void assertSutHasExpectedFilter(final PipeMessageBusSutActions sutActions, final TestEnvironment testEnvironment) {
        final List<?> expectedFilter = (List<?>) testEnvironment.getProperty(PipeMessageBusTestProperties.EXPECTED_FILTER);
        final List<?> list = sutActions.getFilter(testEnvironment);
        assertThat(list, containsInAnyOrder(expectedFilter.toArray()));
    }

    @SuppressWarnings("unchecked")
    private static List<SimpleTestSubscriber<?>> getExpectedReceiversAsSubscriber(final TestEnvironment testEnvironment) {
        return (List<SimpleTestSubscriber<?>>) testEnvironment.getProperty(EXPECTED_RECEIVERS);
    }

    @SuppressWarnings("unchecked")
    private static List<Subscriber<?>> getExpectedSubscriber(final TestEnvironment testEnvironment) {
        return (List<Subscriber<?>>) testEnvironment.getProperty(PipeMessageBusTestProperties.EXPECTED_SUBSCRIBER);
    }

    @SuppressWarnings("unchecked")
    private static List<SimpleTestSubscriber<?>> getPotentialReceiver(final TestEnvironment testEnvironment) {
        return (List<SimpleTestSubscriber<?>>) testEnvironment.getProperty(PipeMessageBusTestProperties.POTENTIAL_RECEIVERS);
    }
}
