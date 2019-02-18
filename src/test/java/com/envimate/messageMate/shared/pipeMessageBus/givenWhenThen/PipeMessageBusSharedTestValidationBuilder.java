package com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen;


import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.qcec.shared.TestValidation;

import static com.envimate.messageMate.shared.validations.SharedTestValidations.*;


public abstract class PipeMessageBusSharedTestValidationBuilder<T> implements TestValidationBuilder<T> {
    protected TestValidation testValidation;

    protected PipeMessageBusSharedTestValidationBuilder<T> asValidation(final TestValidation testValidation) {
        this.testValidation = testValidation;
        return this;
    }

    protected PipeMessageBusSharedTestValidationBuilder<T> thatExpectsTheMessageToBeReceived() {
        return asValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            PipeMessageBusTestValidations.assertExpectedReceiverReceivedSingleMessage(testEnvironment);
        });
    }

    protected PipeMessageBusSharedTestValidationBuilder<T> thatExpectsAllMessagesToBeReceivedByAllSubscribers() {
        return asValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            PipeMessageBusTestValidations.assertExpectedReceiverReceivedAllMessages(testEnvironment);
        });
    }

    public PipeMessageBusSharedTestValidationBuilder<T> thatExpectsAllRemainingSubscribersToStillBeSubscribed() {
        return asValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            final PipeMessageBusSutActions sutActions = sutActions(testEnvironment);
            PipeMessageBusTestValidations.assertExpectedReceiverReceivedAllMessages(sutActions, testEnvironment);
        });
    }

    public PipeMessageBusSharedTestValidationBuilder<T> thatExpectsAllMessagesToHaveTheContentChanged() {
        return asValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            final PipeMessageBusSutActions sutActions = sutActions(testEnvironment);
            PipeMessageBusTestValidations.assertAllMessagesHaveContentChanged(sutActions, testEnvironment);
        });
    }

    public PipeMessageBusSharedTestValidationBuilder<T> thatExpectsOnlyValidMessageToBeReceived() {
        return asValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            PipeMessageBusTestValidations.assertReceiverReceivedOnlyValidMessages(testEnvironment);
        });
    }

    public PipeMessageBusSharedTestValidationBuilder<T> thatExpectsXMessagesToBeDelivered(final int expectedNumberOfDeliveredMessages) {
        return asValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            PipeMessageBusTestValidations.assertNumberOfMessagesReceived(testEnvironment, expectedNumberOfDeliveredMessages);
        });
    }

    public PipeMessageBusSharedTestValidationBuilder<T> thatExpectsResultToBe(final Object expectedResult) {
        return asValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertResultEqualsExpected(testEnvironment, expectedResult);
        });
    }

    public PipeMessageBusSharedTestValidationBuilder<T> thatExpectsTimestampToBeInTheLastXSeconds(final long maximumSecondsDifference) {
        return asValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertTimestampToBeInTheLastXSeconds(testEnvironment, maximumSecondsDifference);
        });
    }

    public PipeMessageBusSharedTestValidationBuilder<T> thatExpectsAListOfSize(final int expectedSize) {
        return asValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertListOfSize(testEnvironment, expectedSize);
        });
    }

    public PipeMessageBusSharedTestValidationBuilder<T> thatExpectsTheMessageBusToBeShutdownInTime() {
        return asValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            final PipeMessageBusSutActions sutActions = sutActions(testEnvironment);
            PipeMessageBusTestValidations.assertSutWasShutdownInTime(sutActions, testEnvironment);
        });
    }

    public PipeMessageBusSharedTestValidationBuilder<T> thatExpectsTheMessageBusToBeShutdown() {
        return asValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            final PipeMessageBusSutActions sutActions = sutActions(testEnvironment);
            PipeMessageBusTestValidations.assertSutIsShutdown(sutActions, testEnvironment);
        });
    }

    public PipeMessageBusSharedTestValidationBuilder<T> thatExpectsEachMessagesToBeReceivedByOnlyOneSubscriber() {
        return asValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            PipeMessageBusTestValidations.assertEachMessagesToBeReceivedByOnlyOneSubscriber(testEnvironment);
        });
    }

    public PipeMessageBusSharedTestValidationBuilder<T> thatExpectsTheExceptionClass(final Class<?> expectedExceptionClass) {
        return asValidation(testEnvironment -> assertExceptionThrownOfType(testEnvironment, expectedExceptionClass));
    }

    public PipeMessageBusSharedTestValidationBuilder<T> thatExpectsAListOfAllFilters() {
        return asValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            PipeMessageBusTestValidations.assertResultEqualToExpectedFilter(testEnvironment);
        });
    }

    public PipeMessageBusSharedTestValidationBuilder<T> thatExpectsTheSutToHaveAllRemainingFilters() {
        return asValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            final PipeMessageBusSutActions sutActions = sutActions(testEnvironment);
            PipeMessageBusTestValidations.assertSutHasExpectedFilter(sutActions, testEnvironment);
        });
    }

    public PipeMessageBusSharedTestValidationBuilder<T> thatExpectsTheResultToAlwaysBeFalse() {
        return asValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertResultEqualsExpected(testEnvironment, false);
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public PipeMessageBusSharedTestValidationBuilder<T> and(final TestValidationBuilder<T> validationBuilder) {
        if (validationBuilder instanceof PipeMessageBusSharedTestValidationBuilder) {
            final TestValidation currentTestValidation = this.testValidation;
            final PipeMessageBusSharedTestValidationBuilder<Object> other = (PipeMessageBusSharedTestValidationBuilder<Object>) validationBuilder;
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

    protected abstract PipeMessageBusSutActions sutActions(final TestEnvironment testEnvironment);

    public TestValidation build() {
        return testValidation;
    }
}
