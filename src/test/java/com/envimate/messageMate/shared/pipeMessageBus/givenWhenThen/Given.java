package com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen;


import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
public final class Given {
    public static <T> When<T> given(final SetupBuilder<T> setupBuilder) {
        return new When<>(setupBuilder);
    }
}
