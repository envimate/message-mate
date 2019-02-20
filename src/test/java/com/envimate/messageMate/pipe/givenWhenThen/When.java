package com.envimate.messageMate.pipe.givenWhenThen;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class When {
    private final PipeSetupBuilder setupBuilder;


    public Then when(final PipeActionBuilder actionBuilder) {
        return new Then(setupBuilder, actionBuilder);
    }
}
