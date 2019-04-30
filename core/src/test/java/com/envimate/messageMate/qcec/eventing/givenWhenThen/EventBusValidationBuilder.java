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

package com.envimate.messageMate.qcec.eventing.givenWhenThen;

import com.envimate.messageMate.shared.environment.TestEnvironment;
import com.envimate.messageMate.qcec.shared.TestReceiver;
import com.envimate.messageMate.shared.givenWhenThen.TestValidation;
import com.envimate.messageMate.qcec.shared.testEvents.TestEvent;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.envimate.messageMate.shared.environment.TestEnvironmentProperty.*;
import static com.envimate.messageMate.shared.validations.SharedTestValidations.assertExceptionThrownOfType;
import static lombok.AccessLevel.PRIVATE;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RequiredArgsConstructor(access = PRIVATE)
public final class EventBusValidationBuilder {
    private final TestValidation validation;

    public static EventBusValidationBuilder expectItToReceivedByAll() {
        return new EventBusValidationBuilder(testEnvironment -> {
            ensureNoExceptionOccurred(testEnvironment);
            final TestEvent testEvent = testEnvironment.getPropertyAsType(TEST_OBJECT, TestEvent.class);
            final List<TestReceiver<TestEvent>> receivers = getExpectedTestEventReceivers(testEnvironment);
            for (final TestReceiver<TestEvent> receiver : receivers) {
                assertTrue(receiver.hasReceived(testEvent));
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static List<TestReceiver<TestEvent>> getExpectedTestEventReceivers(final TestEnvironment testEnvironment) {
        return (List<TestReceiver<TestEvent>>) testEnvironment.getProperty(EXPECTED_RECEIVERS);
    }

    public static EventBusValidationBuilder expectTheEventToBeReceivedByAllRemainingSubscribers() {
        return expectItToReceivedByAll();
    }

    public static EventBusValidationBuilder expectNoException() {
        return new EventBusValidationBuilder(EventBusValidationBuilder::ensureNoExceptionOccurred);
    }

    public static EventBusValidationBuilder expectTheException(final Class<?> expectedExceptionClass) {
        return new EventBusValidationBuilder(testEnvironment -> {
            assertExceptionThrownOfType(testEnvironment, expectedExceptionClass);
        });
    }

    private static void ensureNoExceptionOccurred(final TestEnvironment testEnvironment) {
        final boolean exceptionOccurred = testEnvironment.has(EXCEPTION);
        assertFalse(exceptionOccurred);
    }

    public TestValidation build() {
        return validation;
    }
}
