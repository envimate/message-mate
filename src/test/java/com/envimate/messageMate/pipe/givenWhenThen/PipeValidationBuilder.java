package com.envimate.messageMate.pipe.givenWhenThen;

import com.envimate.messageMate.pipe.Pipe;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeMessageBusSharedTestValidationBuilder;
import com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeMessageBusSutActions;
import com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.TestValidationBuilder;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import lombok.RequiredArgsConstructor;

import static com.envimate.messageMate.pipe.givenWhenThen.PipeTestActions.pipeTestActions;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.SUT;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class PipeValidationBuilder extends PipeMessageBusSharedTestValidationBuilder<Pipe<TestMessage>> {

    public static TestValidationBuilder<Pipe<TestMessage>> expectTheMessageToBeReceived() {
        return new PipeValidationBuilder()
                .thatExpectsTheMessageToBeReceived();
    }

    public static TestValidationBuilder<Pipe<TestMessage>> expectAllMessagesToBeReceivedByAllSubscribers() {
        return new PipeValidationBuilder()
                .thatExpectsAllMessagesToBeReceivedByAllSubscribers();
    }

    public static TestValidationBuilder<Pipe<TestMessage>> expectAllRemainingSubscribersToStillBeSubscribed() {
        return new PipeValidationBuilder()
                .thatExpectsAllRemainingSubscribersToStillBeSubscribed();
    }

    public static TestValidationBuilder<Pipe<TestMessage>> expectAllMessagesToHaveTheContentChanged() {
        return new PipeValidationBuilder()
                .thatExpectsAllMessagesToHaveTheContentChanged();
    }

    public static TestValidationBuilder<Pipe<TestMessage>> expectOnlyValidMessageToBeReceived() {
        return new PipeValidationBuilder()
                .thatExpectsOnlyValidMessageToBeReceived();
    }

    public static TestValidationBuilder<Pipe<TestMessage>> expectXMessagesToBeDelivered(final int expectedNumberOfDeliveredMessages) {
        return new PipeValidationBuilder()
                .thatExpectsXMessagesToBeDelivered(expectedNumberOfDeliveredMessages);
    }

    public static TestValidationBuilder<Pipe<TestMessage>> expectNoMessagesToBeDelivered() {
        return new PipeValidationBuilder()
                .thatExpectsXMessagesToBeDelivered(0);
    }

    public static TestValidationBuilder<Pipe<TestMessage>> expectResultToBe(final Object expectedResult) {
        return new PipeValidationBuilder()
                .thatExpectsResultToBe(expectedResult);
    }

    public static TestValidationBuilder<Pipe<TestMessage>> expectTimestampToBeInTheLastXSeconds(final long maximumSecondsDifference) {
        return new PipeValidationBuilder()
                .thatExpectsTimestampToBeInTheLastXSeconds(maximumSecondsDifference);
    }

    public static TestValidationBuilder<Pipe<TestMessage>> expectThePipeToBeShutdownInTime() {
        return new PipeValidationBuilder()
                .thatExpectsTheMessageBusToBeShutdownInTime();
    }

    public static TestValidationBuilder<Pipe<TestMessage>> expectThePipeToBeShutdown() {
        return new PipeValidationBuilder()
                .thatExpectsTheMessageBusToBeShutdown();
    }

    public static TestValidationBuilder<Pipe<TestMessage>> expectEachMessagesToBeReceivedByOnlyOneSubscriber() {
        return new PipeValidationBuilder()
                .thatExpectsEachMessagesToBeReceivedByOnlyOneSubscriber();
    }

    public static TestValidationBuilder<Pipe<TestMessage>> expectTheException(final Class<?> expectedExceptionClass) {
        return new PipeValidationBuilder()
                .thatExpectsTheExceptionClass(expectedExceptionClass);
    }

    public static TestValidationBuilder<Pipe<TestMessage>> expectAListWithAllFilters() {
        return new PipeValidationBuilder()
                .thatExpectsAListOfAllFilters();
    }

    public static TestValidationBuilder<Pipe<TestMessage>> expectTheRemainingFilter() {
        return new PipeValidationBuilder()
                .thatExpectsTheSutToHaveAllRemainingFilters();
    }

    public static TestValidationBuilder<Pipe<TestMessage>> expectTheResultToAlwaysBeFalse() {
        return new PipeValidationBuilder()
                .thatExpectsTheResultToAlwaysBeFalse();
    }

    @Override
    protected PipeMessageBusSutActions sutActions(final TestEnvironment testEnvironment) {
        final Pipe<TestMessage> pipe = getPipe(testEnvironment);
        return pipeTestActions(pipe);
    }

    @SuppressWarnings("unchecked")
    private Pipe<TestMessage> getPipe(final TestEnvironment testEnvironment) {
        return (Pipe<TestMessage>) testEnvironment.getProperty(SUT);
    }
}
