/*
 * Copyright (c) 2019 envimate GmbH - https://envimate.com/.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.envimate.messageMate.useCases;

import com.envimate.messageMate.mapping.MissingDeserializationException;
import com.envimate.messageMate.mapping.MissingSerializationException;
import com.envimate.messageMate.useCases.shared.TestUseCase;
import org.junit.jupiter.api.Test;

import static com.envimate.messageMate.useCases.givenWhenThen.Given.given;
import static com.envimate.messageMate.useCases.givenWhenThen.UseCaseInvocationActionBuilder.*;
import static com.envimate.messageMate.useCases.givenWhenThen.UseCaseInvocationSetupBuilder.aUseCaseAdapterFor;
import static com.envimate.messageMate.useCases.givenWhenThen.UseCaseInvocationSetupBuilder.aUseCaseBusFor;
import static com.envimate.messageMate.useCases.givenWhenThen.UseCaseInvocationValidationBuilder.*;

public interface UseCaseInvocationSpecs {

    //UseCaseAdapter
    @Test
    default void testUseCaseAdapter_canInvokeUseCaseUsingTheAutomaticMethod(final TestUseCase testUseCase) {
        given(aUseCaseAdapterFor(testUseCase)
                .invokingTheUseCaseUsingTheSingleUseCaseMethod())
                .when(theAssociatedEventIsSend())
                .then(expectTheUseCaseToBeInvokedOnce());
    }

    @Test
    default void testUseCaseAdapter_explicitMappingCanBeDefined(final TestUseCase testUseCase) {
        given(aUseCaseAdapterFor(testUseCase)
                .invokingTheUseCaseUsingTheDefinedMapping())
                .when(theAssociatedEventIsSend())
                .then(expectTheUseCaseToBeInvokedOnce());
    }

    @Test
    default void testUseCaseAdapter_canUseCustomInstantiation(final TestUseCase testUseCase) {
        given(aUseCaseAdapterFor(testUseCase)
                .invokingTheUseCaseUsingTheSingleUseCaseMethod()
                .usingACustomInstantiationMechanism())
                .when(theAssociatedEventIsSend())
                .then(expectTheUseCaseToBeInvokedOnce());
    }

    //errors
    @Test
    default void testUseCaseAdapter_failsForMissingDeserializationMapping(final TestUseCase testUseCase) {
        given(aUseCaseAdapterFor(testUseCase)
                .invokingTheUseCaseUsingAMissingDeserializationParameter())
                .when(anEventWithMissingMappingIsSend())
                .then(expectAExceptionOfType(MissingDeserializationException.class));
    }

    @Test
    default void testUseCaseAdapter_failsForMissingSerializationMapping(final TestUseCase testUseCase) {
        given(aUseCaseAdapterFor(testUseCase)
                .invokingTheUseCaseUsingAMissingSerializationParameter())
                .when(anEventWithMissingMappingIsSend())
                .then(expectAExceptionOfType(MissingSerializationException.class));
    }

    //UseCaseAdapter with MessageFunction
    @Test
    default void testUseCaseAdapter_canBeUsedInCombinationWithAMessageFunction(final TestUseCase testUseCase) {
        given(aUseCaseAdapterFor(testUseCase)
                .invokingTheUseCaseUsingTheSingleUseCaseMethod())
                .when(theRequestIsExecutedUsingAMessageFunction())
                .then(expectTheResponseToBeReceivedByTheMessageFunction());
    }

    @Test
    default void testUseCaseAdapter_canAMessageFunctionAndACustomMapping(final TestUseCase testUseCase) {
        given(aUseCaseAdapterFor(testUseCase)
                .invokingTheUseCaseUsingTheDefinedMapping())
                .when(theRequestIsExecutedUsingAMessageFunction())
                .then(expectTheResponseToBeReceivedByTheMessageFunction());
    }

    //UseCaseBus
    @Test
    default void testUseCaseBus_canInvokeAUseCase(final TestUseCase testUseCase) {
        given(aUseCaseBusFor(testUseCase)
                .invokingTheUseCaseUsingTheSingleUseCaseMethod())
                .when(theRequestIsInvokedOnTheUseCaseBus())
                .then(expectTheUseCaseToBeInvokedByTheUseCaseBus());
    }

    @Test
    default void testUseCaseBus_canInvokeAUseCaseWithTimeout(final TestUseCase testUseCase) {
        given(aUseCaseBusFor(testUseCase)
                .invokingTheUseCaseUsingTheSingleUseCaseMethod())
                .when(theRequestIsInvokedOnTheUseCaseBusWithTimeout())
                .then(expectTheUseCaseToBeInvokedByTheUseCaseBus());
    }

    @Test
    default void testUseCaseBus_canInvokeAUseCaseNotDeserialized(final TestUseCase testUseCase) {
        given(aUseCaseBusFor(testUseCase)
                .invokingTheUseCaseUsingTheSingleUseCaseMethod())
                .when(theRequestIsInvokedOnTheUseCaseBusNotDeserialized())
                .then(expectTheUseCaseToBeInvokedByTheUseCaseBus());
    }

    @Test
    default void testUseCaseBus_canInvokeAUseCaseNotDeserializedWithTimeout(final TestUseCase testUseCase) {
        given(aUseCaseBusFor(testUseCase)
                .invokingTheUseCaseUsingTheSingleUseCaseMethod())
                .when(theRequestIsInvokedOnTheUseCaseBusNotDeserializedWithTimeout())
                .then(expectTheUseCaseToBeInvokedByTheUseCaseBus());
    }

}
