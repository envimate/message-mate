package com.envimate.messageMate.messageBus.givenWhenThen;


import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
public final class Given {
    public static When given(final MessageBusSetupBuilder setupBuilder) {
        return new When(setupBuilder);
    }
}
