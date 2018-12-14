package com.envimate.messageMate.qcec.shared.testEvents;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@EqualsAndHashCode
@RequiredArgsConstructor(access = PRIVATE)
public final class EndingEvent {
    public static EndingEvent endingEvent() {
        return new EndingEvent();
    }
}
