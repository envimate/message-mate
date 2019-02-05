package com.envimate.messageMate.chain;

import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class Given {
    public static When given(final ChainSetupBuilder chainSetupBuilder) {
        return new When(chainSetupBuilder);
    }
}
