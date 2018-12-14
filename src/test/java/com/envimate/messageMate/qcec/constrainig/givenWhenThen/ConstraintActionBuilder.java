package com.envimate.messageMate.qcec.constrainig.givenWhenThen;


import com.envimate.messageMate.qcec.shared.TestAction;
import com.envimate.messageMate.qcec.shared.TestReceiver;
import com.envimate.messageMate.qcec.shared.testConstraints.TestConstraint;
import com.envimate.messageMate.subscribing.SubscriptionId;
import lombok.RequiredArgsConstructor;

import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.EXPECTED_EXCEPTION_MESSAGE;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.EXPECTED_RECEIVERS;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.TEST_OBJECT;
import static com.envimate.messageMate.qcec.shared.TestReceiver.aTestReceiver;
import static com.envimate.messageMate.qcec.shared.testConstraints.TestConstraint.testConstraint;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class ConstraintActionBuilder {
    private final TestAction<TestConstraintEnforcer> testAction;

    public static ConstraintActionBuilder aPassingConstraintIsEnforced() {
        return new ConstraintActionBuilder((testConstraintEnforcer, testEnvironment) -> {
            final TestConstraint testConstraint = testConstraint();
            testEnvironment.setProperty(TEST_OBJECT, testConstraint);
            testConstraintEnforcer.enforce(testConstraint);
            return null;
        });
    }

    public static ConstraintActionBuilder anExceptionCausingConstraintIsEnforced() {
        return new ConstraintActionBuilder((testConstraintEnforcer, testEnvironment) -> {
            final TestConstraint testConstraint = testConstraint();
            testEnvironment.setProperty(TEST_OBJECT, testConstraint);
            final String expected_exception_message = "Constraint exception";
            testEnvironment.setProperty(EXPECTED_EXCEPTION_MESSAGE, expected_exception_message);
            testConstraintEnforcer.withASubscriber(TestConstraint.class, c -> {
                throw new RuntimeException(expected_exception_message);
            });
            testConstraintEnforcer.enforce(testConstraint);
            return null;
        });
    }

    public static ConstraintActionBuilder anReceiverUnsubscribes() {
        return new ConstraintActionBuilder((testConstraintEnforcer, testEnvironment) -> {
            final TestReceiver<TestConstraint> unsubscribingReceiver = aTestReceiver();
            final SubscriptionId subscriptionId = testConstraintEnforcer.subscribing(TestConstraint.class, unsubscribingReceiver);

            final TestReceiver<TestConstraint> remainingReceiver = aTestReceiver();
            testConstraintEnforcer.subscribing(TestConstraint.class, remainingReceiver);
            testEnvironment.addToListProperty(EXPECTED_RECEIVERS, remainingReceiver);

            testConstraintEnforcer.unsubscribe(subscriptionId);

            final TestConstraint testConstraint = testConstraint();
            testEnvironment.setProperty(TEST_OBJECT, testConstraint);
            testConstraintEnforcer.enforce(testConstraint);
            return null;
        });
    }

    public TestAction<TestConstraintEnforcer> build() {
        return testAction;
    }
}
