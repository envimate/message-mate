package com.envimate.messageMate.qcec.shared.testEvents;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@EqualsAndHashCode
@RequiredArgsConstructor(access = PRIVATE)
public final class SpecificEvent {
    public final int id;

    public static SpecificEvent specificEventWithId(final int id) {
        return new SpecificEvent(id);
    }
}
