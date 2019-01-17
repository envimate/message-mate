package com.envimate.messageMate.messageBus.givenWhenThen;


import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.error.DeliveryFailedMessage;
import com.envimate.messageMate.shared.givenWhenThen.TestValidation;
import com.envimate.messageMate.shared.givenWhenThen.TestValidationBuilder;
import com.envimate.messageMate.shared.subscriber.TestSubscriber;
import com.envimate.messageMate.subscribing.Subscriber;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.envimate.messageMate.shared.context.TestExecutionProperty.ERROR_SUBSCRIBER;
import static com.envimate.messageMate.shared.context.TestExecutionProperty.RESULT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public final class MessageBusValidationBuilder extends TestValidationBuilder<MessageBus> {

    @SafeVarargs
    @SuppressWarnings("varargs")
    private MessageBusValidationBuilder(TestValidation<MessageBus>... validations) {
        super(validations);
    }

    public static TestValidationBuilder<MessageBus> expectTheMessageToBeReceived() {
        return new MessageBusValidationBuilder()
                .thatExpectsTheMessageToBeReceived();
    }

    public static TestValidationBuilder<MessageBus> expectAllMessagesToBeReceivedByAllSubscribers() {
        return new MessageBusValidationBuilder()
                .thatExpectsAllMessagesToBeReceivedByAllSubscribers();
    }

    public static TestValidationBuilder<MessageBus> expectAllRemainingSubscribersToStillBeSubscribed() {
        return new MessageBusValidationBuilder()
                .thatExpectsAllRemainingSubscribersToStillBeSubscribed();
    }

    public static TestValidationBuilder<MessageBus> expectAllMessagesToHaveTheContentChanged() {
        return new MessageBusValidationBuilder()
                .thatExpectsAllMessagesToHaveTheContentChanged();
    }

    public static TestValidationBuilder<MessageBus> expectOnlyValidMessageToBeReceived() {
        return new MessageBusValidationBuilder()
                .thatExpectsOnlyValidMessageToBeReceived();
    }

    public static TestValidationBuilder<MessageBus> expectXMessagesToBeDelivered(final int expectedNumberOfDeliveredMessages) {
        return new MessageBusValidationBuilder()
                .thatExpectsXMessagesToBeDelivered(expectedNumberOfDeliveredMessages);
    }

    public static TestValidationBuilder<MessageBus> expectNoMessagesToBeDelivered() {
        return new MessageBusValidationBuilder()
                .thatExpectsXMessagesToBeDelivered(0);
    }

    public static TestValidationBuilder<MessageBus> expectResultToBe(final Object expectedResult) {
        return new MessageBusValidationBuilder()
                .thatExpectsResultToBe(expectedResult);
    }

    public static TestValidationBuilder<MessageBus> expectTimestampToBeInTheLastXSeconds(final long maximumSecondsDifference) {
        return new MessageBusValidationBuilder()
                .thatExpectsTimestampToBeInTheLastXSeconds(maximumSecondsDifference);
    }

    public static TestValidationBuilder<MessageBus> expectAListOfSize(final int expectedSize) {
        return new MessageBusValidationBuilder()
                .thatExpectsAListOfSize(expectedSize);
    }

    public static TestValidationBuilder<MessageBus> expectSubscriberOfType(final int expectedNumberOfSubscribers, final Class<?> messageClass) {
        return new MessageBusValidationBuilder()
                .withAValidation((messageBus, executionContext) -> {
                    @SuppressWarnings("unchecked")
                    final Map<Object, List<Subscriber<Object>>> resultMap = (Map<Object, List<Subscriber<Object>>>) executionContext.getProperty(RESULT);
                    final List<Subscriber<Object>> subscribersForType = resultMap.get(messageClass);
                    assertThat(subscribersForType.size(), equalTo(expectedNumberOfSubscribers));
                });
    }

    public static TestValidationBuilder<MessageBus> expectTheMessageBusToBeShutdownInTime() {
        return new MessageBusValidationBuilder()
                .thatExpectsTheMessageBusToBeShutdownInTime();
    }

    public static TestValidationBuilder<MessageBus> expectTheMessageBusToBeShutdown() {
        return new MessageBusValidationBuilder()
                .thatExpectsTheMessageBusToBeShutdown();
    }

    public static TestValidationBuilder<MessageBus> expectErrorMessageWithCause(final Class<?> expectedCauseClass) {
        return new MessageBusValidationBuilder().withAValidation(
                (messageBus, executionContext) -> {
                    final TestSubscriber<?> testSubscriber = executionContext.getPropertyAsType(ERROR_SUBSCRIBER, TestSubscriber.class);
                    final List<?> receivedMessages = testSubscriber.getReceivedMessages();
                    assertThat(receivedMessages.size(), equalTo(1));
                    final Object firstMessage = receivedMessages.get(0);
                    assertThat(firstMessage.getClass(), equalTo(DeliveryFailedMessage.class));
                    @SuppressWarnings("unchecked")
                    final DeliveryFailedMessage<Object> errorMessage = (DeliveryFailedMessage<Object>) firstMessage;
                    assertThat(errorMessage.getCause().getClass(), equalTo(expectedCauseClass));
                }
        );
    }


    public static TestValidationBuilder<MessageBus> expectEachMessagesToBeReceivedByOnlyOneSubscriber() {
        return new MessageBusValidationBuilder()
                .thatExpectsEachMessagesToBeReceivedByOnlyOneSubscriber();
    }

    @Override
    protected boolean isShutdown(final MessageBus messageBus) {
        return messageBus.isShutdown();
    }

    @Override
    protected List<Subscriber<?>> getAllSubscribers(final MessageBus messageBus) {
        final List<Subscriber<Object>> allSubscribers = messageBus.getStatusInformation().getAllSubscribers();
        final LinkedList<Subscriber<?>> subscribers = new LinkedList<>(allSubscribers);
        return subscribers;
    }

}
