package com.envimate.messageMate.pipe.givenWhenThen;

import com.envimate.messageMate.pipe.Pipe;
import com.envimate.messageMate.error.AlreadyClosedException;
import com.envimate.messageMate.pipe.PipeStatusInformation;
import com.envimate.messageMate.pipe.statistics.PipeStatistics;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.qcec.shared.TestValidation;
import com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeMessageBusSutActions;
import com.envimate.messageMate.shared.subscriber.TestException;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import com.envimate.messageMate.shared.validations.SharedTestValidations;
import lombok.RequiredArgsConstructor;

import java.math.BigInteger;

import static com.envimate.messageMate.pipe.givenWhenThen.PipeTestActions.pipeTestActions;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.SUT;
import static com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeMessageBusTestValidations.*;
import static com.envimate.messageMate.shared.validations.SharedTestValidations.*;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class PipeValidationBuilder {
    private final TestValidation testValidation;

    public static PipeValidationBuilder expectTheMessageToBeReceived() {
        return new PipeValidationBuilder(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertExpectedReceiverReceivedSingleMessage(testEnvironment);
        });
    }

    public static PipeValidationBuilder expectAllMessagesToBeReceivedByAllSubscribers() {
        return new PipeValidationBuilder(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertExpectedReceiverReceivedAllMessages(testEnvironment);
        });
    }

    public static PipeValidationBuilder expectAllRemainingSubscribersToStillBeSubscribed() {
        return new PipeValidationBuilder(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            final PipeMessageBusSutActions sutActions = sutActions(testEnvironment);
            assertSutStillHasExpectedSubscriber(sutActions, testEnvironment);
        });
    }

    public static PipeValidationBuilder expectTheListOfAllSubscriber() {
        return new PipeValidationBuilder(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertResultEqualsCurrentSubscriber(testEnvironment);
        });
    }

    public static PipeValidationBuilder expectXMessagesToBeDelivered_despiteTheChannelClosed(final int expectedNumberOfDeliveredMessages) {
        return new PipeValidationBuilder(testEnvironment -> {
            assertExceptionThrownOfType(testEnvironment, AlreadyClosedException.class);
            assertNumberOfMessagesReceived(testEnvironment, expectedNumberOfDeliveredMessages);
        });
    }

    public static PipeValidationBuilder expectResultToBe(final Object expectedResult) {
        return new PipeValidationBuilder(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertResultEqualsExpected(testEnvironment, expectedResult);
        });
    }

    public static PipeValidationBuilder expectTimestampToBeInTheLastXSeconds(final long maximumSecondsDifference) {
        return new PipeValidationBuilder(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertTimestampToBeInTheLastXSeconds(testEnvironment, maximumSecondsDifference);
        });
    }

    public static PipeValidationBuilder expectThePipeToBeShutdownInTime() {
        return new PipeValidationBuilder(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            final PipeMessageBusSutActions sutActions = sutActions(testEnvironment);
            assertSutWasShutdownInTime(sutActions, testEnvironment);
        });
    }

    public static PipeValidationBuilder expectThePipeToBeShutdown() {
        return new PipeValidationBuilder(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            final PipeMessageBusSutActions sutActions = sutActions(testEnvironment);
            assertSutIsShutdown(sutActions, testEnvironment);
        });
    }

    public static PipeValidationBuilder expectEachMessagesToBeReceivedByOnlyOneSubscriber() {
        return new PipeValidationBuilder(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertEachMessagesToBeReceivedByOnlyOneSubscriber(testEnvironment);
        });
    }

    public static PipeValidationBuilder expectTheException(final Class<?> expectedExceptionClass) {
        return new PipeValidationBuilder(testEnvironment -> assertExceptionThrownOfType(testEnvironment, expectedExceptionClass));
    }

    public static PipeValidationBuilder expectNoException() {
        return new PipeValidationBuilder(SharedTestValidations::assertNoExceptionThrown);
    }

    public static PipeValidationBuilder expectTheResultToAlwaysBeFalse() {
        return new PipeValidationBuilder(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertResultEqualsExpected(testEnvironment, false);
        });
    }

    public static PipeValidationBuilder expectTheAwaitToBeTerminatedSuccessful(final int expectedNumberOfReceivedMessages) {
        return new PipeValidationBuilder(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertResultEqualsExpected(testEnvironment, true);
            assertNumberOfMessagesReceived(testEnvironment, expectedNumberOfReceivedMessages);
        });
    }

    public static PipeValidationBuilder expectTheAwaitToBeTerminatedWithFailure() {
        return new PipeValidationBuilder(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertResultEqualsExpected(testEnvironment, false);
        });
    }

    public static PipeValidationBuilder expectTheExceptionToBeHandled() {
        return new PipeValidationBuilder(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertResultOfClass(testEnvironment, TestException.class);
        });
    }

    public static PipeValidationBuilder expectTheDeliveryToBeStillSuccessful() {
        return new PipeValidationBuilder(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertNumberOfMessagesReceived(testEnvironment, 0);
            final Pipe<TestMessage> pipe = getPipe(testEnvironment);
            final PipeStatusInformation<TestMessage> statusInformation = pipe.getStatusInformation();
            final PipeStatistics currentMessageStatistics = statusInformation.getCurrentMessageStatistics();
            final BigInteger successfulMessages = currentMessageStatistics.getSuccessfulMessages();
            assertEquals(successfulMessages.longValueExact(), 1);
        });
    }

    private static PipeMessageBusSutActions sutActions(final TestEnvironment testEnvironment) {
        final Pipe<TestMessage> pipe = getPipe(testEnvironment);
        return pipeTestActions(pipe);
    }

    @SuppressWarnings("unchecked")
    private static Pipe<TestMessage> getPipe(final TestEnvironment testEnvironment) {
        return (Pipe<TestMessage>) testEnvironment.getProperty(SUT);
    }

    public TestValidation build() {
        return testValidation;
    }
}
