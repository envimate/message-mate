package com.envimate.messageMate.qcec.constrainig.givenWhenThen;

import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.qcec.shared.TestReceiver;
import com.envimate.messageMate.qcec.shared.TestValidation;
import com.envimate.messageMate.qcec.shared.testConstraints.TestConstraint;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.*;
import static lombok.AccessLevel.PRIVATE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;

@RequiredArgsConstructor(access = PRIVATE)
public final class ConstraintValidationBuilder {
    private final TestValidation testValidation;

    public static ConstraintValidationBuilder expectTheConstraintToBeReceivedByAll() {
        return new ConstraintValidationBuilder(testEnvironment -> {
            ensureNoExceptionOccurred(testEnvironment);
            @SuppressWarnings("unchecked")
            final List<TestReceiver<TestConstraint>> expectedReceivers = (List<TestReceiver<TestConstraint>>) testEnvironment.getProperty(EXPECTED_RECEIVERS);
            final TestConstraint sendConstraint = testEnvironment.getPropertyAsType(TEST_OBJECT, TestConstraint.class);
            for (final TestReceiver<TestConstraint> receiver : expectedReceivers) {
                assertThat(receiver.hasReceived(sendConstraint), equalTo(true));
            }
        });
    }

    public static ConstraintValidationBuilder expectTheConstraintToBeReceivedByAllRemainingSubscribers() {
        return expectTheConstraintToBeReceivedByAll();
    }

    public static ConstraintValidationBuilder expectTheExceptionToBeThrown() {
        return new ConstraintValidationBuilder(testEnvironment -> {
            final Exception exception = testEnvironment.getPropertyAsType(EXCEPTION, Exception.class);
            assertThat(exception.getClass(), equalTo(RuntimeException.class));
            final String expectedExceptionMessage = testEnvironment.getPropertyAsType(EXPECTED_EXCEPTION_MESSAGE, String.class);
            assertThat(exception.getMessage(), equalTo(expectedExceptionMessage));
        });
    }

    private static void ensureNoExceptionOccurred(final TestEnvironment testEnvironment) {
        final boolean exceptionOccurred = testEnvironment.has(EXCEPTION);
        assertFalse(exceptionOccurred);
    }

    public TestValidation build() {
        return testValidation;
    }
}
