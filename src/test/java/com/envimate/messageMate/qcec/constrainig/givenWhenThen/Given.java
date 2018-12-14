package com.envimate.messageMate.qcec.constrainig.givenWhenThen;


import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class Given {
    public static When given(final TestConstraintEnforcer testConstraintEnforcer) {
        return new When(testConstraintEnforcer);
    }
}
