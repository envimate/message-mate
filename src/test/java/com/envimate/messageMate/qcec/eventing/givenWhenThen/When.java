package com.envimate.messageMate.qcec.eventing.givenWhenThen;

import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
public class When {
    private final TestEventBus testEventBus;

    public Then when(final EventBusActionBuilder eventBusActionBuilder) {
        return new Then(testEventBus, eventBusActionBuilder);
    }
}
