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

import com.envimate.messageMate.shared.exceptions.TestException;
import org.junit.jupiter.api.Test;

import static com.envimate.messageMate.useCases.specialInvocations.Given.given;
import static com.envimate.messageMate.useCases.specialInvocations.SpecialInvocationUseCaseBuilder.aUseCaseAdapter;
import static com.envimate.messageMate.useCases.specialInvocations.SpecialInvocationUseCaseInvoker.whenTheUSeCaseIsInvoked;
import static com.envimate.messageMate.useCases.specialInvocations.SpecialInvocationValidator.expectExecutionExceptionContaining;
import static com.envimate.messageMate.useCases.specialInvocations.SpecialInvocationValidator.expectExecutionExceptionContainingExceptionClass;

public class UseCaseSpecialInvocationSpecs {

    @Test
    void testUseCaseAdapter_canHandleExceptionDuringInitialization() {
        final TestException expectedException = new TestException();
        given(aUseCaseAdapter()
                .forAnUseCaseThrowingAnExceptionDuringInitialization(expectedException))
                .when(whenTheUSeCaseIsInvoked())
                .then(expectExecutionExceptionContaining(expectedException));
    }

    @Test
    void testUseCaseAdapter_canHandleExceptionDuringStaticInitializer() {
        given(aUseCaseAdapter()
                .forAnUseCaseThrowingAnExceptionDuringStaticInitializer())
                .when(whenTheUSeCaseIsInvoked())
                .then(expectExecutionExceptionContainingExceptionClass(TestException.class));
    }
}
