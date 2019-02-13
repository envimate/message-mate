package com.envimate.messageMate.shared.channelMessageBus.givenWhenThen;


import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.qcec.shared.TestValidation;

import static com.envimate.messageMate.shared.channelMessageBus.givenWhenThen.ChannelMessageBusTestValidations.*;
import static com.envimate.messageMate.shared.validations.SharedTestValidations.*;


public abstract class ChannelMessageBusSharedTestValidationBuilder<T> implements TestValidationBuilder<T> {
    protected TestValidation testValidation;

    protected ChannelMessageBusSharedTestValidationBuilder<T> asValidation(final TestValidation testValidation) {
        this.testValidation = testValidation;
        return this;
    }

    protected ChannelMessageBusSharedTestValidationBuilder<T> thatExpectsTheMessageToBeReceived() {
        return asValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertExpectedReceiverReceivedSingleMessage(testEnvironment);
        });
    }

    protected ChannelMessageBusSharedTestValidationBuilder<T> thatExpectsAllMessagesToBeReceivedByAllSubscribers() {
        return asValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertExpectedReceiverReceivedAllMessages(testEnvironment);
        });
    }

    public ChannelMessageBusSharedTestValidationBuilder<T> thatExpectsAllRemainingSubscribersToStillBeSubscribed() {
        return asValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            final ChannelMessageBusSutActions sutActions = sutActions(testEnvironment);
            assertExpectedReceiverReceivedAllMessages(sutActions, testEnvironment);
        });
    }

    public ChannelMessageBusSharedTestValidationBuilder<T> thatExpectsAllMessagesToHaveTheContentChanged() {
        return asValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            final ChannelMessageBusSutActions sutActions = sutActions(testEnvironment);
            assertAllMessagesHaveContentChanged(sutActions, testEnvironment);
        });
    }

    public ChannelMessageBusSharedTestValidationBuilder<T> thatExpectsOnlyValidMessageToBeReceived() {
        return asValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertReceiverReceivedOnlyValidMessages(testEnvironment);
        });
    }

    public ChannelMessageBusSharedTestValidationBuilder<T> thatExpectsXMessagesToBeDelivered(final int expectedNumberOfDeliveredMessages) {
        return asValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertNumberOfMessagesReceived(testEnvironment, expectedNumberOfDeliveredMessages);
        });
    }

    public ChannelMessageBusSharedTestValidationBuilder<T> thatExpectsResultToBe(final Object expectedResult) {
        return asValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertResultEqualsExpected(testEnvironment, expectedResult);
        });
    }

    public ChannelMessageBusSharedTestValidationBuilder<T> thatExpectsTimestampToBeInTheLastXSeconds(final long maximumSecondsDifference) {
        return asValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertTimestampToBeInTheLastXSeconds(testEnvironment, maximumSecondsDifference);
        });
    }

    public ChannelMessageBusSharedTestValidationBuilder<T> thatExpectsAListOfSize(final int expectedSize) {
        return asValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertListOfSize(testEnvironment, expectedSize);
        });
    }

    public ChannelMessageBusSharedTestValidationBuilder<T> thatExpectsTheMessageBusToBeShutdownInTime() {
        return asValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            final ChannelMessageBusSutActions sutActions = sutActions(testEnvironment);
            assertSutWasShutdownInTime(sutActions, testEnvironment);
        });
    }

    public ChannelMessageBusSharedTestValidationBuilder<T> thatExpectsTheMessageBusToBeShutdown() {
        return asValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            final ChannelMessageBusSutActions sutActions = sutActions(testEnvironment);
            assertSutIsShutdown(sutActions, testEnvironment);
        });
    }

    public ChannelMessageBusSharedTestValidationBuilder<T> thatExpectsEachMessagesToBeReceivedByOnlyOneSubscriber() {
        return asValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertEachMessagesToBeReceivedByOnlyOneSubscriber(testEnvironment);
        });
    }

    public ChannelMessageBusSharedTestValidationBuilder<T> thatExpectsTheExceptionClass(final Class<?> expectedExceptionClass) {
        return asValidation(testEnvironment -> assertExceptionThrownOfType(testEnvironment, expectedExceptionClass));
    }

    public ChannelMessageBusSharedTestValidationBuilder<T> thatExpectsAListOfAllFilters() {
        return asValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertResultEqualToExpectedFilter(testEnvironment);
        });
    }

    public ChannelMessageBusSharedTestValidationBuilder<T> thatExpectsTheSutToHaveAllRemainingFilters() {
        return asValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            final ChannelMessageBusSutActions sutActions = sutActions(testEnvironment);
            assertSutHasExpectedFilter(sutActions, testEnvironment);
        });
    }

    public ChannelMessageBusSharedTestValidationBuilder<T> thatExpectsTheResultToAlwaysBeFalse() {
        return asValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertResultEqualsExpected(testEnvironment, false);
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public ChannelMessageBusSharedTestValidationBuilder<T> and(final TestValidationBuilder<T> validationBuilder) {
        if (validationBuilder instanceof ChannelMessageBusSharedTestValidationBuilder) {
            final TestValidation currentTestValidation = this.testValidation;
            final ChannelMessageBusSharedTestValidationBuilder<Object> other = (ChannelMessageBusSharedTestValidationBuilder<Object>) validationBuilder;
            final TestValidation secondValidation = other.testValidation;
            this.testValidation = testEnvironment -> {
                currentTestValidation.validate(testEnvironment);
                secondValidation.validate(testEnvironment);
            };
        } else {
            throw new UnsupportedOperationException();
        }
        return this;
    }

    protected abstract ChannelMessageBusSutActions sutActions(final TestEnvironment testEnvironment);

    public TestValidation build() {
        return testValidation;
    }
}
