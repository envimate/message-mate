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

package com.envimate.messageMate.useCases.specialInvocations;

import com.envimate.messageMate.shared.environment.TestEnvironment;
import com.envimate.messageMate.shared.givenWhenThen.TestAction;
import com.envimate.messageMate.shared.givenWhenThen.TestValidation;
import com.envimate.messageMate.useCases.useCaseBus.UseCaseBus;
import lombok.RequiredArgsConstructor;

import static com.envimate.messageMate.shared.environment.TestEnvironmentProperty.SUT;
import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
public final class Then {
    private final SpecialInvocationUseCaseBuilder useCaseBuilder;
    private final SpecialInvocationUseCaseInvoker useCaseInvoker;


    public void then(final SpecialInvocationValidator validator) {
        final TestEnvironment testEnvironment = useCaseBuilder.build();
        final UseCaseBus useCaseBus = testEnvironment.getPropertyAsType(SUT, UseCaseBus.class);
        final TestAction<UseCaseBus> testAction = useCaseInvoker.build();
        testAction.execute(useCaseBus, testEnvironment);

        final TestValidation testValidation = validator.build();
        testValidation.validate(testEnvironment);
    }
}
