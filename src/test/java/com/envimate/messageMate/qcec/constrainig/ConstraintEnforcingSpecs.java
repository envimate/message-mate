package com.envimate.messageMate.qcec.constrainig;

import com.envimate.messageMate.qcec.constrainig.givenWhenThen.TestConstraintEnforcer;
import org.junit.jupiter.api.Test;

import static com.envimate.messageMate.qcec.constrainig.givenWhenThen.ConstraintActionBuilder.*;
import static com.envimate.messageMate.qcec.constrainig.givenWhenThen.ConstraintValidationBuilder.*;
import static com.envimate.messageMate.qcec.constrainig.givenWhenThen.Given.given;


public interface ConstraintEnforcingSpecs {

    @Test
    default void testConstraintEnforcer_constraintIsReceivedByAll(final TestConstraintEnforcer aConstraintEnforcer) {
        given(aConstraintEnforcer
                .withSeveralSubscriber())
                .when(aPassingConstraintIsEnforced())
                .then(expectTheConstraintToBeReceivedByAll());
    }

    @Test
    default void testConstraintEnforcer_constraintCanThrowException(final TestConstraintEnforcer aConstraintEnforcer) {
        given(aConstraintEnforcer)
                .when(anExceptionCausingConstraintIsEnforced())
                .then(expectTheExceptionToBeThrown());
    }


    @Test
    default void testConstraintEnforcer_canUnsubscribe(final TestConstraintEnforcer aConstraintEnforcer) {
        given(aConstraintEnforcer)
                .when(anReceiverUnsubscribes())
                .then(expectTheConstraintToBeReceivedByAllRemainingSubscribers());
    }
}
