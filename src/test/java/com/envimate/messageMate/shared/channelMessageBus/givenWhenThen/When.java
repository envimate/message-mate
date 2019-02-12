package com.envimate.messageMate.shared.channelMessageBus.givenWhenThen;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class When<T> {
    private final SetupBuilder<T> setupBuilder;


    public Then<T> when(final ActionBuilder<T> actionBuilder) {
        return new Then<>(setupBuilder, actionBuilder);
    }
}
