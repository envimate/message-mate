package com.envimate.messageMate.serializedMessageBus;

import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
public final class When {
    private final SerializedMessageBusSetupBuilder setupBuilder;

    public Then when(final SerializedMessageBusActionBuilder actionBuilder) {
        return new Then(setupBuilder, actionBuilder);
    }
}
