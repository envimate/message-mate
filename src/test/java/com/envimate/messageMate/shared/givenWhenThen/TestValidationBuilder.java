package com.envimate.messageMate.shared.givenWhenThen;


import com.envimate.messageMate.shared.subscriber.SimpleTestSubscriber;
import com.envimate.messageMate.shared.subscriber.TestSubscriber;
import com.envimate.messageMate.shared.testMessages.TestMessageOfInterest;
import com.envimate.messageMate.subscribing.Subscriber;

import java.util.*;
import java.util.stream.Collectors;

import static com.envimate.messageMate.shared.context.TestExecutionProperty.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;


public abstract class TestValidationBuilder<T> {
    private final List<TestValidation<T>> validations;

    @SafeVarargs
    @SuppressWarnings("varargs")
    public TestValidationBuilder(final TestValidation<T>... validations) {
        this.validations = new LinkedList<>();
        this.validations.addAll(Arrays.asList(validations));
    }

    private static List<SimpleTestSubscriber<?>> subscribersThatReceivedMessage(final List<SimpleTestSubscriber<?>> receivers, final Object expectedMessage) {
        return receivers.stream()
                .filter(subscriber -> subscriber.getReceivedMessages().contains(expectedMessage))
                .collect(Collectors.toList());
    }

    protected TestValidationBuilder<T> withAValidation(final TestValidation<T> validation) {
        this.validations.add(validation);
        return this;
    }

    protected TestValidationBuilder<T> thatExpectsTheMessageToBeReceived() {
        return withAValidation(
                (t, executionContext) -> {
                    @SuppressWarnings("unchecked")
                    final List<SimpleTestSubscriber<?>> receivers = (List<SimpleTestSubscriber<?>>) executionContext.getProperty(EXPECTED_RECEIVERS);
                    for (final SimpleTestSubscriber<?> receiver : receivers) {
                        final List<?> receivedMessages = receiver.getReceivedMessages();
                        assertEquals(receivedMessages.size(), 1);
                        final Object receivedMessage = receivedMessages.get(0);
                        final Object expectedMessage = executionContext.getProperty(SINGLE_SEND_MESSAGE);
                        assertEquals(receivedMessage, expectedMessage);
                    }
                }
        );
    }

    protected TestValidationBuilder<T> thatExpectsAllMessagesToBeReceivedByAllSubscribers() {
        return withAValidation(
                (t, executionContext) -> {
                    @SuppressWarnings("unchecked")
                    final List<Object> expectedReceivedMessages = (List<Object>) executionContext.getProperty(MESSAGES_SEND_OF_INTEREST);
                    @SuppressWarnings("unchecked")
                    final List<SimpleTestSubscriber<?>> receivers = (List<SimpleTestSubscriber<?>>) executionContext.getProperty(EXPECTED_RECEIVERS);
                    for (final SimpleTestSubscriber<?> receiver : receivers) {
                        final List<?> receivedMessages = receiver.getReceivedMessages();
                        assertEquals(receivedMessages.size(), expectedReceivedMessages.size());
                        final Object[] ar = expectedReceivedMessages.toArray();
                        assertThat(receivedMessages, containsInAnyOrder(ar));
                    }
                }
        );
    }

    public TestValidationBuilder<T> thatExpectsAllRemainingSubscribersToStillBeSubscribed() {
        final TestValidationBuilder<T> that = this;
        return withAValidation(
                (t, executionContext) -> {
                    @SuppressWarnings("unchecked")
                    final List<Subscriber<?>> expectedSubscriber = (List<Subscriber<?>>) executionContext.getProperty(EXPECTED_SUBSCRIBER);
                    final List<Subscriber<?>> allSubscribers = that.getAllSubscribers(t);
                    assertThat(allSubscribers, containsInAnyOrder(expectedSubscriber.toArray()));
                }
        );
    }

    @SuppressWarnings("unchecked")
    public TestValidationBuilder<T> thatExpectsAllMessagesToHaveTheContentChanged() {
        final TestValidationBuilder<T> that = this;
        return withAValidation(
                (t, executionContext) -> {
                    final List<?> expectedMessages = (List<?>) executionContext.getProperty(MESSAGES_SEND_OF_INTEREST);
                    final List<Subscriber<?>> subscribers = that.getAllSubscribers(t);
                    final String expectedContent = executionContext.getPropertyAsType(EXPECTED_CHANGED_CONTENT, String.class);
                    for (final Subscriber<?> subscriber : subscribers) {
                        final TestSubscriber<TestMessageOfInterest> testSubscriber = (TestSubscriber<TestMessageOfInterest>) subscriber;
                        final List<TestMessageOfInterest> receivedMessages = testSubscriber.getReceivedMessages();
                        assertThat(expectedMessages.size(), equalTo(receivedMessages.size()));
                        for (final TestMessageOfInterest receivedMessage : receivedMessages) {
                            assertThat(receivedMessage.content, equalTo(expectedContent));
                        }
                    }
                }
        );
    }

    @SuppressWarnings("unchecked")
    public TestValidationBuilder<T> thatExpectsOnlyValidMessageToBeReceived() {
        return withAValidation(
                (t, executionContext) -> {
                    final List<?> expectedReceivedMessages = (List<?>) executionContext.getProperty(MESSAGES_SEND);
                    final List<SimpleTestSubscriber<?>> receivers = (List<SimpleTestSubscriber<?>>) executionContext.getProperty(EXPECTED_RECEIVERS);
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
        );
    }

    @SuppressWarnings("unchecked")
    public TestValidationBuilder<T> thatExpectsXMessagesToBeDelivered(final int expectedNumberOfDeliveredMessages) {
        return withAValidation(
                (t, executionContext) -> {
                    final List<TestSubscriber<?>> receiver;
                    if (executionContext.has(SINGLE_RECEIVER)) {
                        final TestSubscriber<?> singleSubscriber = executionContext.getPropertyAsType(SINGLE_RECEIVER, TestSubscriber.class);
                        receiver = Collections.singletonList(singleSubscriber);
                    } else {
                        receiver = (List<TestSubscriber<?>>) executionContext.getPropertyAsType(EXPECTED_RECEIVERS, List.class);
                    }
                    for (final TestSubscriber<?> currentReceiver : receiver) {
                        final List<?> receivedMessages = currentReceiver.getReceivedMessages();
                        assertEquals(expectedNumberOfDeliveredMessages, receivedMessages.size());
                    }
                }
        );
    }

    public TestValidationBuilder<T> thatExpectsResultToBe(final Object expectedResult) {
        return withAValidation(
                (t, executionContext) -> {
                    final Object result = executionContext.getProperty(RESULT);
                    if (expectedResult instanceof Number && result instanceof Number) {
                        final double resultAsDouble = ((Number) result).doubleValue();
                        final double expectedAsDouble = ((Number) expectedResult).doubleValue();
                        assertEquals(expectedAsDouble, resultAsDouble);
                    } else {
                        assertEquals(expectedResult, result);
                    }
                }
        );
    }

    public TestValidationBuilder<T> thatExpectsTimestampToBeInTheLastXSeconds(final long maximumSecondsDifference) {
        return withAValidation(
                (t, executionContext) -> {
                    final Date now = new Date();
                    final Date timestamp = executionContext.getPropertyAsType(RESULT, Date.class);
                    final long secondsDifference = (now.getTime() - timestamp.getTime()) / 1000;
                    assertThat(secondsDifference, lessThanOrEqualTo(maximumSecondsDifference));
                }
        );
    }

    public TestValidationBuilder<T> thatExpectsAListOfSize(final int expectedSize) {
        return withAValidation(
                (t, executionContext) -> {
                    final List<?> list = executionContext.getPropertyAsType(RESULT, List.class);
                    assertThat(list.size(), equalTo(expectedSize));
                }
        );
    }

    public TestValidationBuilder<T> thatExpectsTheMessageBusToBeShutdownInTime() {
        final TestValidationBuilder<T> that = this;
        return withAValidation(
                (t, executionContext) -> {
                    final boolean wasTerminatedInTime = executionContext.getPropertyAsType(RESULT, Boolean.class);
                    assertTrue(that.isShutdown(t));
                    assertTrue(wasTerminatedInTime);
                }
        );
    }

    public TestValidationBuilder<T> thatExpectsTheMessageBusToBeShutdown() {
        final TestValidationBuilder<T> that = this;
        return withAValidation(
                (t, executionContext) -> assertTrue(that.isShutdown(t))
        );
    }

    @SuppressWarnings("unchecked")
    public TestValidationBuilder<T> thatExpectsEachMessagesToBeReceivedByOnlyOneSubscriber() {
        return withAValidation(
                (t, executionContext) -> {
                    final List<?> expectedMessages = (List<?>) executionContext.getProperty(MESSAGES_SEND_OF_INTEREST);
                    final List<SimpleTestSubscriber<?>> receivers = (List<SimpleTestSubscriber<?>>) executionContext.getProperty(POTENTIAL_RECEIVERS);
                    for (final Object expectedMessage : expectedMessages) {
                        final List<SimpleTestSubscriber<?>> subscribersThatReceivedMessage = subscribersThatReceivedMessage(receivers, expectedMessage);
                        assertThat(subscribersThatReceivedMessage.size(), equalTo(1));
                    }
                }
        );
    }

    protected abstract List<Subscriber<?>> getAllSubscribers(T t);

    protected abstract boolean isShutdown(T t);


    public TestValidationBuilder<T> and(final TestValidationBuilder<T> followUpValidationBuilder) {
        this.validations.addAll(followUpValidationBuilder.validations);
        return this;
    }

    public List<TestValidation<T>> build() {
        return validations;
    }
}
