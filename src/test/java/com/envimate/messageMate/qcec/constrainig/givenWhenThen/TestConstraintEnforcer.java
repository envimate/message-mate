package com.envimate.messageMate.qcec.constrainig.givenWhenThen;


import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.qcec.shared.TestReceiver;
import com.envimate.messageMate.qcec.shared.testConstraints.TestConstraint;
import com.envimate.messageMate.subscribing.SubscriptionId;

import java.util.function.Consumer;

import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.EXPECTED_RECEIVERS;


public abstract class TestConstraintEnforcer {
    private final TestEnvironment testEnvironment = TestEnvironment.emptyTestEnvironment();

    public TestEnvironment getEnvironment() {
        return testEnvironment;
    }

    public abstract void enforce(Object constraint);

    public abstract <T> TestConstraintEnforcer withASubscriber(final Class<T> constraintClass, Consumer<T> consumer);

    public abstract <T> SubscriptionId subscribing(final Class<T> constraintClass, Consumer<T> consumer);

    public abstract void unsubscribe(SubscriptionId subscriptionId);

    public TestConstraintEnforcer withSeveralSubscriber() {
        final int numberOfSubscriber = 5;
        for (int i = 0; i < numberOfSubscriber; i++) {
            final TestReceiver<TestConstraint> receiver = TestReceiver.aTestReceiver();
            testEnvironment.addToListProperty(EXPECTED_RECEIVERS, receiver);
            withASubscriber(TestConstraint.class, receiver);
        }
        return this;
    }
}
