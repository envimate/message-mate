package com.envimate.messageMate.pipe.givenWhenThen;

import com.envimate.messageMate.pipe.Pipe;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.shared.channelMessageBus.givenWhenThen.ChannelMessageBusSharedTestValidationBuilder;
import com.envimate.messageMate.shared.channelMessageBus.givenWhenThen.ChannelMessageBusSutActions;
import com.envimate.messageMate.shared.channelMessageBus.givenWhenThen.TestValidationBuilder;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import lombok.RequiredArgsConstructor;

import static com.envimate.messageMate.pipe.givenWhenThen.ChannelTestActions.channelTestActions;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.SUT;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class ChannelValidationBuilder extends ChannelMessageBusSharedTestValidationBuilder<Pipe<TestMessage>> {

    public static TestValidationBuilder<Pipe<TestMessage>> expectTheMessageToBeReceived() {
        return new ChannelValidationBuilder()
                .thatExpectsTheMessageToBeReceived();
    }

    public static TestValidationBuilder<Pipe<TestMessage>> expectAllMessagesToBeReceivedByAllSubscribers() {
        return new ChannelValidationBuilder()
                .thatExpectsAllMessagesToBeReceivedByAllSubscribers();
    }

    public static TestValidationBuilder<Pipe<TestMessage>> expectAllRemainingSubscribersToStillBeSubscribed() {
        return new ChannelValidationBuilder()
                .thatExpectsAllRemainingSubscribersToStillBeSubscribed();
    }

    public static TestValidationBuilder<Pipe<TestMessage>> expectAllMessagesToHaveTheContentChanged() {
        return new ChannelValidationBuilder()
                .thatExpectsAllMessagesToHaveTheContentChanged();
    }

    public static TestValidationBuilder<Pipe<TestMessage>> expectOnlyValidMessageToBeReceived() {
        return new ChannelValidationBuilder()
                .thatExpectsOnlyValidMessageToBeReceived();
    }

    public static TestValidationBuilder<Pipe<TestMessage>> expectXMessagesToBeDelivered(final int expectedNumberOfDeliveredMessages) {
        return new ChannelValidationBuilder()
                .thatExpectsXMessagesToBeDelivered(expectedNumberOfDeliveredMessages);
    }

    public static TestValidationBuilder<Pipe<TestMessage>> expectNoMessagesToBeDelivered() {
        return new ChannelValidationBuilder()
                .thatExpectsXMessagesToBeDelivered(0);
    }

    public static TestValidationBuilder<Pipe<TestMessage>> expectResultToBe(final Object expectedResult) {
        return new ChannelValidationBuilder()
                .thatExpectsResultToBe(expectedResult);
    }

    public static TestValidationBuilder<Pipe<TestMessage>> expectTimestampToBeInTheLastXSeconds(final long maximumSecondsDifference) {
        return new ChannelValidationBuilder()
                .thatExpectsTimestampToBeInTheLastXSeconds(maximumSecondsDifference);
    }

    public static TestValidationBuilder<Pipe<TestMessage>> expectTheChannelToBeShutdownInTime() {
        return new ChannelValidationBuilder()
                .thatExpectsTheMessageBusToBeShutdownInTime();
    }

    public static TestValidationBuilder<Pipe<TestMessage>> expectTheChannelToBeShutdown() {
        return new ChannelValidationBuilder()
                .thatExpectsTheMessageBusToBeShutdown();
    }

    public static TestValidationBuilder<Pipe<TestMessage>> expectEachMessagesToBeReceivedByOnlyOneSubscriber() {
        return new ChannelValidationBuilder()
                .thatExpectsEachMessagesToBeReceivedByOnlyOneSubscriber();
    }

    public static TestValidationBuilder<Pipe<TestMessage>> expectTheException(final Class<?> expectedExceptionClass) {
        return new ChannelValidationBuilder()
                .thatExpectsTheExceptionClass(expectedExceptionClass);
    }

    public static TestValidationBuilder<Pipe<TestMessage>> expectAListWithAllFilters() {
        return new ChannelValidationBuilder()
                .thatExpectsAListOfAllFilters();
    }

    public static TestValidationBuilder<Pipe<TestMessage>> expectTheRemainingFilter() {
        return new ChannelValidationBuilder()
                .thatExpectsTheSutToHaveAllRemainingFilters();
    }

    public static TestValidationBuilder<Pipe<TestMessage>> expectTheResultToAlwaysBeFalse() {
        return new ChannelValidationBuilder()
                .thatExpectsTheResultToAlwaysBeFalse();
    }

    @Override
    protected ChannelMessageBusSutActions sutActions(final TestEnvironment testEnvironment) {
        final Pipe<TestMessage> pipe = getChannel(testEnvironment);
        return channelTestActions(pipe);
    }

    @SuppressWarnings("unchecked")
    private Pipe<TestMessage> getChannel(final TestEnvironment testEnvironment) {
        return (Pipe<TestMessage>) testEnvironment.getProperty(SUT);
    }
}
