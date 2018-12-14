package com.envimate.messageMate.qcec.shared.testConstraints;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@EqualsAndHashCode
@RequiredArgsConstructor(access = PRIVATE)
public class SpecificConstraint {
    public final int id;

    public static SpecificConstraint specificConstraintWithId(final int id) {
        return new SpecificConstraint(id);
    }
}
