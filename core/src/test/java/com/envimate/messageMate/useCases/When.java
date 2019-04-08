package com.envimate.messageMate.useCases;

import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
public class When {
    private final UseCaseInvocationSetupBuilder useCaseInvocationSetupBuilder;

    public Then when(UseCaseInvocationActionBuilder useCaseInvocationActionBuilder) {
        return new Then(useCaseInvocationSetupBuilder, useCaseInvocationActionBuilder);
    }
}
