package com.envimate.messageMate.useCaseAdapter;

import org.junit.jupiter.api.Test;


public interface UseCaseAdapterSpecs {

    @Test
    default void testUseCaseAdapter_canInvokeUseCaseUsingTheAutomaticMethod(final TestUseCase testUseCase) {
        Given.given(UseCaseAdapterSetupBuilder.aUseCaseAdapter(testUseCase)
                .invokingTheUseCaseUsingTheSingleUseCaseMethod())
                .when(UseCaseAdapterActionBuilder.theAssociatedEventIsSend())
                .then(UseCaseAdapterValidationBuilder.expectTheUseCaseToBeInvokedOnce());
    }

    @Test
    default void testUseCaseAdapter_explicitMappingCanBeDefined(final TestUseCase testUseCase) {
        Given.given(UseCaseAdapterSetupBuilder.aUseCaseAdapter(testUseCase)
                .invokingTheUseCaseUsingTheDefinedMapping())
                .when(UseCaseAdapterActionBuilder.theAssociatedEventIsSend())
                .then(UseCaseAdapterValidationBuilder.expectTheUseCaseToBeInvokedOnce());
    }

    @Test
    default void testUseCaseAdapter_canUseCustomInstantiation(final TestUseCase testUseCase) {
        Given.given(UseCaseAdapterSetupBuilder.aUseCaseAdapter(testUseCase)
                .invokingTheUseCaseUsingTheSingleUseCaseMethod()
                .usingACustomInstantiationMechanism())
                .when(UseCaseAdapterActionBuilder.theAssociatedEventIsSend())
                .then(UseCaseAdapterValidationBuilder.expectTheUseCaseToBeInvokedOnce());
    }

    @Test
    default void testUseCaseAdapter_canBeUsedInCombinationWithAMessageFunction(final TestUseCase testUseCase) {
        Given.given(UseCaseAdapterSetupBuilder.aUseCaseAdapter(testUseCase)
                .invokingTheUseCaseUsingTheSingleUseCaseMethod())
                .when(UseCaseAdapterActionBuilder.theRequestIsExecutedUsingAMessageFunction())
                .then(UseCaseAdapterValidationBuilder.expectTheResponseToBeReceivedByTheMessageFunction());
    }

    @Test
    default void testUseCaseAdapter_canAMessageFunctionAndACustomMapping(final TestUseCase testUseCase) {
        Given.given(UseCaseAdapterSetupBuilder.aUseCaseAdapter(testUseCase)
                .invokingTheUseCaseUsingTheDefinedMapping())
                .when(UseCaseAdapterActionBuilder.theRequestIsExecutedUsingAMessageFunction())
                .then(UseCaseAdapterValidationBuilder.expectTheResponseToBeReceivedByTheMessageFunction());
    }


}