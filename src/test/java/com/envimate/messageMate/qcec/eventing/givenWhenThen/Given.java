package com.envimate.messageMate.qcec.eventing.givenWhenThen;

import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class Given {
    public static When given(final TestEventBus testEventBus) {
        return new When(testEventBus);
    }
}
