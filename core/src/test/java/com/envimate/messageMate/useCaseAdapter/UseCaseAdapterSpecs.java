package com.envimate.messageMate.useCaseAdapter;

import org.junit.jupiter.api.Test;

public class UseCaseAdapterSpecs {

    @Test
    void testUseCaseAdapter_canInvokeASimpleUseCase() {
        Given.given(UseCaseAdapterSetupBuilder.aUseCaseAdapter()
                .withAUseCaseWithASingleTestEventAsParameter())
                .when(UseCaseAdapterActionBuilder.aTestEventIsSend())
                .then(UseCaseAdapterValidationBuilder.expectTheUseCaseToBeInvokedOnce());
    }

    @Test
    void testUseCaseAdapter_canInvokeUseCaseWithoutParameter() {
        Given.given(UseCaseAdapterSetupBuilder.aUseCaseAdapter()
                .withAUseCaseWithoutParameter())
                .when(UseCaseAdapterActionBuilder.theAssociatedEventIsSend())
                .then(UseCaseAdapterValidationBuilder.expectTheUseCaseToBeInvokedOnce());
    }
    //TODO: automatic mapping for >=2 parameter


    @Test
    void testUseCaseAdapter_canInvokeUseCaseWithoutReturnValue() {
        Given.given(UseCaseAdapterSetupBuilder.aUseCaseAdapter()
                .withAUseCaseWithoutReturnValue())
                .when(UseCaseAdapterActionBuilder.theAssociatedEventIsSend())
                .then(UseCaseAdapterValidationBuilder.expectTheUseCaseToBeInvokedOnce());
    }

}