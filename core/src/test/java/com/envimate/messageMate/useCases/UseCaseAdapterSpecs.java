package com.envimate.messageMate.useCases;

import org.junit.jupiter.api.Test;

import static com.envimate.messageMate.useCases.Given.given;
import static com.envimate.messageMate.useCases.UseCaseAdapterActionBuilder.theAssociatedEventIsSend;
import static com.envimate.messageMate.useCases.UseCaseAdapterActionBuilder.theRequestIsExecutedUsingAMessageFunction;
import static com.envimate.messageMate.useCases.UseCaseAdapterSetupBuilder.aUseCaseAdapter;
import static com.envimate.messageMate.useCases.UseCaseAdapterValidationBuilder.expectTheResponseToBeReceivedByTheMessageFunction;
import static com.envimate.messageMate.useCases.UseCaseAdapterValidationBuilder.expectTheUseCaseToBeInvokedOnce;

//TODO: test for primitive types as arguments
//TODO: test for missing mapping
public interface UseCaseAdapterSpecs {

    @Test
    default void testUseCaseAdapter_canInvokeUseCaseUsingTheAutomaticMethod(final TestUseCase testUseCase) {
        given(aUseCaseAdapter(testUseCase)
                .invokingTheUseCaseUsingTheSingleUseCaseMethod())
                .when(theAssociatedEventIsSend())
                .then(expectTheUseCaseToBeInvokedOnce());
    }

    @Test
    default void testUseCaseAdapter_explicitMappingCanBeDefined(final TestUseCase testUseCase) {
        given(aUseCaseAdapter(testUseCase)
                .invokingTheUseCaseUsingTheDefinedMapping())
                .when(theAssociatedEventIsSend())
                .then(expectTheUseCaseToBeInvokedOnce());
    }

    @Test
    default void testUseCaseAdapter_canUseCustomInstantiation(final TestUseCase testUseCase) {
        given(aUseCaseAdapter(testUseCase)
                .invokingTheUseCaseUsingTheSingleUseCaseMethod()
                .usingACustomInstantiationMechanism())
                .when(theAssociatedEventIsSend())
                .then(expectTheUseCaseToBeInvokedOnce());
    }

    @Test
    default void testUseCaseAdapter_canBeUsedInCombinationWithAMessageFunction(final TestUseCase testUseCase) {
        given(aUseCaseAdapter(testUseCase)
                .invokingTheUseCaseUsingTheSingleUseCaseMethod())
                .when(theRequestIsExecutedUsingAMessageFunction())
                .then(expectTheResponseToBeReceivedByTheMessageFunction());
    }

    @Test
    default void testUseCaseAdapter_canAMessageFunctionAndACustomMapping(final TestUseCase testUseCase) {
        given(aUseCaseAdapter(testUseCase)
                .invokingTheUseCaseUsingTheDefinedMapping())
                .when(theRequestIsExecutedUsingAMessageFunction())
                .then(expectTheResponseToBeReceivedByTheMessageFunction());
    }


}