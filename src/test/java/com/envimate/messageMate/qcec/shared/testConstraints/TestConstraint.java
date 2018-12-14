package com.envimate.messageMate.qcec.shared.testConstraints;

import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class TestConstraint {

    public static TestConstraint testConstraint() {
        return new TestConstraint();
    }
}
