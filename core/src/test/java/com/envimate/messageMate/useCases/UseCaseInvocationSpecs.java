package com.envimate.messageMate.useCases;

import com.envimate.messageMate.useCases.useCaseAdapter.mapping.RequestMapperException;
import com.envimate.messageMate.useCases.useCaseAdapter.mapping.ResponseMapperException;
import org.junit.jupiter.api.Test;

import static com.envimate.messageMate.useCases.Given.given;
import static com.envimate.messageMate.useCases.UseCaseInvocationActionBuilder.*;
import static com.envimate.messageMate.useCases.UseCaseInvocationSetupBuilder.aUseCaseAdapter;
import static com.envimate.messageMate.useCases.UseCaseInvocationSetupBuilder.aUseCaseBus;
import static com.envimate.messageMate.useCases.UseCaseInvocationValidationBuilder.expectTheResponseToBeReceivedByTheMessageFunction;
import static com.envimate.messageMate.useCases.UseCaseInvocationValidationBuilder.expectTheUseCaseToBeInvokedByTheUseCaseBus;
import static com.envimate.messageMate.useCases.UseCaseInvocationValidationBuilder.expectTheUseCaseToBeInvokedOnce;

public interface UseCaseInvocationSpecs {

    //UseCaseAdapter
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

    //errors
    @Test
    default void testUseCaseAdapter_failsForMissingDeserializationMapping(final TestUseCase testUseCase) {
        Given.given(UseCaseInvocationSetupBuilder.aUseCaseAdapter(testUseCase)
                .invokingTheUseCaseUsingAMissingDeserializationParameter())
                .when(UseCaseInvocationActionBuilder.anEventWithMissingMappingIsSend())
                .then(UseCaseInvocationValidationBuilder.expectAExceptionOfType(RequestMapperException.class));//TODO: better exception name
    }
    @Test
    default void testUseCaseAdapter_failsForMissingSerializationMapping(final TestUseCase testUseCase) {
        Given.given(UseCaseInvocationSetupBuilder.aUseCaseAdapter(testUseCase)
                .invokingTheUseCaseUsingAMissingSerializationParameter())
                .when(UseCaseInvocationActionBuilder.anEventWithMissingMappingIsSend())
                .then(UseCaseInvocationValidationBuilder.expectAExceptionOfType(ResponseMapperException.class));//TODO: better exception name
    }

    //UseCaseAdapter with MessageFunction
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

    //UseCaseBus
    @Test
    default void testUseCaseBus_canInvokeAUseCase(final TestUseCase testUseCase) {
        given(aUseCaseBus(testUseCase)
                .invokingTheUseCaseUsingTheSingleUseCaseMethod())
                .when(theRequestIsInvokedOnTheUseCaseBus())
                .then(expectTheUseCaseToBeInvokedByTheUseCaseBus());
    }

    @Test
    default void testUseCaseBus_canInvokeAUseCaseWithTimeout(final TestUseCase testUseCase) {
        given(aUseCaseBus(testUseCase)
                .invokingTheUseCaseUsingTheSingleUseCaseMethod())
                .when(theRequestIsInvokedOnTheUseCaseBusWithTimeout())
                .then(expectTheUseCaseToBeInvokedByTheUseCaseBus());
    }

    @Test
    default void testUseCaseBus_canInvokeAUseCaseNotDeserialized(final TestUseCase testUseCase) {
        given(aUseCaseBus(testUseCase)
                .invokingTheUseCaseUsingTheSingleUseCaseMethod())
                .when(theRequestIsInvokedOnTheUseCaseBusNotDeserialized())
                .then(expectTheUseCaseToBeInvokedByTheUseCaseBus());
    }

    @Test
    default void testUseCaseBus_canInvokeAUseCaseNotDeserializedWithTimeout(final TestUseCase testUseCase) {
        given(aUseCaseBus(testUseCase)
                .invokingTheUseCaseUsingTheSingleUseCaseMethod())
                .when(theRequestIsInvokedOnTheUseCaseBusNotDeserializedWithTimeout())
                .then(expectTheUseCaseToBeInvokedByTheUseCaseBus());
    }


}