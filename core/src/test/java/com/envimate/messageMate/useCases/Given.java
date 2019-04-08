package com.envimate.messageMate.useCases;

public class Given {
    public static When given(UseCaseInvocationSetupBuilder useCaseInvocationSetupBuilder) {
        return new When(useCaseInvocationSetupBuilder);
    }
}
