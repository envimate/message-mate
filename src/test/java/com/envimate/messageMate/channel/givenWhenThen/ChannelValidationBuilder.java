package com.envimate.messageMate.channel.givenWhenThen;

import com.envimate.messageMate.channel.Channel;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.shared.channelMessageBus.givenWhenThen.ChannelMessageBusSharedTestValidationBuilder;
import com.envimate.messageMate.shared.channelMessageBus.givenWhenThen.ChannelMessageBusSutActions;
import com.envimate.messageMate.shared.channelMessageBus.givenWhenThen.TestValidationBuilder;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import lombok.RequiredArgsConstructor;

import static com.envimate.messageMate.channel.givenWhenThen.ChannelTestActions.channelTestActions;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.SUT;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class ChannelValidationBuilder extends ChannelMessageBusSharedTestValidationBuilder<Channel<TestMessage>> {

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

    public static TestValidationBuilder<Channel<TestMessage>> expectTheException(final Class<?> expectedExceptionClass) {
        return new ChannelValidationBuilder()
                .thatExpectsTheExceptionClass(expectedExceptionClass);
    }

    public static TestValidationBuilder<Channel<TestMessage>> expectAListWithAllFilters() {
        return new ChannelValidationBuilder()
                .thatExpectsAListOfAllFilters();
    }

    public static TestValidationBuilder<Channel<TestMessage>> expectTheRemainingFilter() {
        return new ChannelValidationBuilder()
                .thatExpectsTheSutToHaveAllRemainingFilters();
    }

    public static TestValidationBuilder<Channel<TestMessage>> expectTheResultToAlwaysBeFalse() {
        return new ChannelValidationBuilder()
                .thatExpectsTheResultToAlwaysBeFalse();
    }

    @Override
    protected ChannelMessageBusSutActions sutActions(final TestEnvironment testEnvironment) {
        final Channel<TestMessage> channel = getChannel(testEnvironment);
        return channelTestActions(channel);
    }

    @SuppressWarnings("unchecked")
    private Channel<TestMessage> getChannel(final TestEnvironment testEnvironment) {
        return (Channel<TestMessage>) testEnvironment.getProperty(SUT);
    }
}
