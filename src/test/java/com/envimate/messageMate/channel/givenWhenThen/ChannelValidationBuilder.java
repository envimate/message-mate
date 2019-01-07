package com.envimate.messageMate.channel.givenWhenThen;

import com.envimate.messageMate.channel.Channel;
import com.envimate.messageMate.channel.ChannelStatusInformation;
import com.envimate.messageMate.shared.givenWhenThen.TestValidation;
import com.envimate.messageMate.shared.givenWhenThen.TestValidationBuilder;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import com.envimate.messageMate.subscribing.Subscriber;

import java.util.LinkedList;
import java.util.List;

public final class ChannelValidationBuilder extends TestValidationBuilder<Channel<TestMessage>> {

    @SafeVarargs
    @SuppressWarnings("varargs")
    private ChannelValidationBuilder(TestValidation<Channel<TestMessage>>... validations) {
        super(validations);
    }

    public static TestValidationBuilder<Channel<TestMessage>> expectTheMessageToBeReceived() {
        return new ChannelValidationBuilder()
                .thatExpectsTheMessageToBeReceived();
    }

    public static TestValidationBuilder<Channel<TestMessage>> expectAllMessagesToBeReceivedByAllSubscribers() {
        return new ChannelValidationBuilder()
                .thatExpectsAllMessagesToBeReceivedByAllSubscribers();
    }

    public static TestValidationBuilder<Channel<TestMessage>> expectAllRemainingSubscribersToStillBeSubscribed() {
        return new ChannelValidationBuilder()
                .thatExpectsAllRemainingSubscribersToStillBeSubscribed();
    }

    public static TestValidationBuilder<Channel<TestMessage>> expectAllMessagesToHaveTheContentChanged() {
        return new ChannelValidationBuilder()
                .thatExpectsAllMessagesToHaveTheContentChanged();
    }

    public static TestValidationBuilder<Channel<TestMessage>> expectOnlyValidMessageToBeReceived() {
        return new ChannelValidationBuilder()
                .thatExpectsOnlyValidMessageToBeReceived();
    }

    public static TestValidationBuilder<Channel<TestMessage>> expectXMessagesToBeDelivered(final int expectedNumberOfDeliveredMessages) {
        return new ChannelValidationBuilder()
                .thatExpectsXMessagesToBeDelivered(expectedNumberOfDeliveredMessages);
    }

    public static TestValidationBuilder<Channel<TestMessage>> expectNoMessagesToBeDelivered() {
        return new ChannelValidationBuilder()
                .thatExpectsXMessagesToBeDelivered(0);
    }

    public static TestValidationBuilder<Channel<TestMessage>> expectResultToBe(final Object expectedResult) {
        return new ChannelValidationBuilder()
                .thatExpectsResultToBe(expectedResult);
    }

    public static TestValidationBuilder<Channel<TestMessage>> expectTimestampToBeInTheLastXSeconds(final long maximumSecondsDifference) {
        return new ChannelValidationBuilder()
                .thatExpectsTimestampToBeInTheLastXSeconds(maximumSecondsDifference);
    }

    public static TestValidationBuilder<Channel<TestMessage>> expectTheChannelToBeShutdownInTime() {
        return new ChannelValidationBuilder()
                .thatExpectsTheMessageBusToBeShutdownInTime();
    }

    public static TestValidationBuilder<Channel<TestMessage>> expectTheChannelToBeShutdown() {
        return new ChannelValidationBuilder()
                .thatExpectsTheMessageBusToBeShutdown();
    }

    public static TestValidationBuilder<Channel<TestMessage>> expectEachMessagesToBeReceivedByOnlyOneSubscriber() {
        return new ChannelValidationBuilder()
                .thatExpectsEachMessagesToBeReceivedByOnlyOneSubscriber();
    }

    @Override
    protected List<Subscriber<?>> getAllSubscribers(final Channel<TestMessage> channel) {
        final ChannelStatusInformation<TestMessage> statusInformation = channel.getStatusInformation();
        final List<Subscriber<TestMessage>> subscribers = statusInformation.getAllSubscribers();
        final List<Subscriber<?>> allSubscribers = new LinkedList<>(subscribers);
        return allSubscribers;
    }

    @Override
    protected boolean isShutdown(final Channel<TestMessage> channel) {
        return channel.isShutdown();
    }
}
