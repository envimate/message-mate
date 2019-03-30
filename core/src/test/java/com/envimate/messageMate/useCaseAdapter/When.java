package com.envimate.messageMate.useCaseAdapter;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
public class When {
    private final UseCaseAdapterSetupBuilder useCaseAdapterSetupBuilder;

    public Then when(UseCaseAdapterActionBuilder useCaseAdapterActionBuilder) {
        return new Then(useCaseAdapterSetupBuilder, useCaseAdapterActionBuilder);
    }
}
