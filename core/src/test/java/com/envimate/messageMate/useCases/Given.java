package com.envimate.messageMate.useCases;

public class Given {
    public static When given(UseCaseAdapterSetupBuilder useCaseAdapterSetupBuilder) {
        return new When(useCaseAdapterSetupBuilder);
    }
}
