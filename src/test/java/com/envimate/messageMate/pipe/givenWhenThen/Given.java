package com.envimate.messageMate.pipe.givenWhenThen;

import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
public final class Given {
    public static When given(final PipeSetupBuilder setupBuilder) {
        return new When(setupBuilder);
    }
}
