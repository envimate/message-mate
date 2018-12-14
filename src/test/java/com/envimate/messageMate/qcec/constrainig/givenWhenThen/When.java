package com.envimate.messageMate.qcec.constrainig.givenWhenThen;

import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
public class When {
    private final TestConstraintEnforcer testConstraintEnforcer;

    public Then when(final ConstraintActionBuilder constraintActionBuilder) {
        return new Then(testConstraintEnforcer, constraintActionBuilder);
    }
}
