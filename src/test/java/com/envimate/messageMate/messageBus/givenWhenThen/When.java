package com.envimate.messageMate.messageBus.givenWhenThen;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class When {
    private final MessageBusSetupBuilder setupBuilder;

    public Then when(final MessageBusActionBuilder actionBuilder) {
        return new Then(setupBuilder, actionBuilder);
    }
}
