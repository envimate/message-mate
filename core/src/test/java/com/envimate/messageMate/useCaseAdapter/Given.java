package com.envimate.messageMate.useCaseAdapter;

public class Given {
    public static When given(UseCaseAdapterSetupBuilder useCaseAdapterSetupBuilder) {
        return new When(useCaseAdapterSetupBuilder);
    }
}
