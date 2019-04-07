package com.envimate.messageMate.serializedMessageBus;

import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class Given {
    public static When given(final SerializedMessageBusSetupBuilder setupBuilder) {
        return new When(setupBuilder);
    }
}
