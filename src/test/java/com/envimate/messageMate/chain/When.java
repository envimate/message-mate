package com.envimate.messageMate.chain;

import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
public class When {
    private final ChainSetupBuilder chainSetupBuilder;

    public Then when(final ChainActionBuilder chainActionBuilder) {
        return new Then(chainSetupBuilder, chainActionBuilder);
    }
}
