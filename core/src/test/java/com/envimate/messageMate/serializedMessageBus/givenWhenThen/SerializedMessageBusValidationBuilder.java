/*
 * Copyright (c) 2018 envimate GmbH - https://envimate.com/.
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

package com.envimate.messageMate.serializedMessageBus.givenWhenThen;

import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.useCases.payloadAndErrorPayload.PayloadAndErrorPayload;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.qcec.shared.TestValidation;
import com.envimate.messageMate.shared.subscriber.TestException;
import com.envimate.messageMate.shared.subscriber.TestSubscriber;
import com.envimate.messageMate.shared.testMessages.TestMessageOfInterest;
import com.envimate.messageMate.subscribing.Subscriber;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.*;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.MOCK;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.RESULT;
import static com.envimate.messageMate.serializedMessageBus.givenWhenThen.SerializedMessageBusSetupBuilder.PAYLOAD_SERIALIZATION_KEY;
import static com.envimate.messageMate.serializedMessageBus.givenWhenThen.SerializedMessageBusTestProperties.SEND_DATA;
import static com.envimate.messageMate.serializedMessageBus.givenWhenThen.SerializedMessageBusTestProperties.SEND_ERROR_DATA;
import static com.envimate.messageMate.shared.validations.SharedTestValidations.*;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class SerializedMessageBusValidationBuilder {
    private final TestValidation testValidation;

    public static SerializedMessageBusValidationBuilder expectTheCorrectDataToBeReceived() {
        return new SerializedMessageBusValidationBuilder(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertCorrectDataToBeReceived(testEnvironment);
        });
    }

    public static SerializedMessageBusValidationBuilder expectTheDataAndTheErrorToBeReceived() {
        return new SerializedMessageBusValidationBuilder(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertCorrectDataToBeReceived(testEnvironment);
        });
    }

    public static SerializedMessageBusValidationBuilder expectToHaveWaitedUntilTheCorrectResponseWasReceived() {
        return new SerializedMessageBusValidationBuilder(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertReceivedResultEqualsExpected(testEnvironment);
        });
    }

    public static SerializedMessageBusValidationBuilder expectTheSendDataToBeReturnedAsErrorData() {
        return new SerializedMessageBusValidationBuilder(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            final Object sendObject = testEnvironment.getProperty(SEND_DATA);
            assertReceivedAsErrorResponse(testEnvironment, sendObject);
        });
    }

    private static void assertReceivedAsErrorResponse(final TestEnvironment testEnvironment, final Object expectedResult) {
        final PayloadAndErrorPayload<?, ?> result = (PayloadAndErrorPayload<?, ?>) testEnvironment.getProperty(RESULT);
        final Object errorPayload = result.getErrorPayload();
        assertEquals(errorPayload, expectedResult);
    }

    public static SerializedMessageBusValidationBuilder expectToHaveWaitedUntilTheNotSerializedResponseWasReceived() {
        return new SerializedMessageBusValidationBuilder(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            final TestMessageOfInterest sendObject = testEnvironment.getPropertyAsType(SEND_DATA, TestMessageOfInterest.class);
            final HashMap<String, Object> expectedResult = new HashMap<>();
            expectedResult.put(PAYLOAD_SERIALIZATION_KEY, sendObject.content);
            assertReceivedResultEqualsExpected(testEnvironment, expectedResult, null);
        });
    }

    public static SerializedMessageBusValidationBuilder expectTheSendDataToBeReturnedAsNotSerializedErrorData() {
        return new SerializedMessageBusValidationBuilder(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            final TestMessageOfInterest sendObject = testEnvironment.getPropertyAsType(SEND_DATA, TestMessageOfInterest.class);
            final HashMap<String, Object> expectedResult = new HashMap<>();
            expectedResult.put(PAYLOAD_SERIALIZATION_KEY, sendObject.content);
            assertReceivedAsErrorResponse(testEnvironment, expectedResult);
        });
    }

    public static SerializedMessageBusValidationBuilder expectTheTimeoutToBeOccurred() {
        return new SerializedMessageBusValidationBuilder(testEnvironment -> {
            assertExceptionThrownOfType(testEnvironment, TimeoutException.class);
        });
    }

    public static SerializedMessageBusValidationBuilder expectAnExecutionExceptionWithTheCorrectCause() {
        return new SerializedMessageBusValidationBuilder(testEnvironment -> {
            assertExceptionThrownOfTypeWithCause(testEnvironment, ExecutionException.class, TestException.class);
        });
    }

    public static SerializedMessageBusValidationBuilder expectAnExecutionExceptionFor(final Class<?> expectedCauseClass) {
        return new SerializedMessageBusValidationBuilder(testEnvironment -> {
            assertExceptionThrownOfTypeWithCause(testEnvironment, ExecutionException.class, expectedCauseClass);
        });
    }

    public static SerializedMessageBusValidationBuilder expectTheException(final Class<?> expectedExceptionClass) {
        return new SerializedMessageBusValidationBuilder(testEnvironment -> {
            assertExceptionThrownOfType(testEnvironment, expectedExceptionClass);
        });
    }

    public static SerializedMessageBusValidationBuilder expectNoRemainingSubscriber() {
        return new SerializedMessageBusValidationBuilder(testEnvironment -> {
            final MessageBus underlyingMessageBus = testEnvironment.getPropertyAsType(MOCK, MessageBus.class);
            final List<Subscriber<?>> allSubscribers = underlyingMessageBus.getStatusInformation().getAllSubscribers();
            assertCollectionOfSize(allSubscribers, 0);
        });
    }

    private static void assertCorrectDataToBeReceived(final TestEnvironment testEnvironment) {
        final Object expectedPayload = testEnvironment.getProperty(SEND_DATA);
        final Object expectedErrorPayload;
        if (testEnvironment.has(SEND_ERROR_DATA)) {
            expectedErrorPayload = testEnvironment.getProperty(SEND_ERROR_DATA);
        } else {
            expectedErrorPayload = null;
        }
        final List<TestSubscriber<PayloadAndErrorPayload<?, ?>>> receivers = getExpectedPayloadsReceivers(testEnvironment);
        for (final TestSubscriber<PayloadAndErrorPayload<?, ?>> receiver : receivers) {
            final List<PayloadAndErrorPayload<?, ?>> receivedMessages = receiver.getReceivedMessages();
            assertCollectionOfSize(receivedMessages, 1);
            final PayloadAndErrorPayload<?, ?> payloadAndErrorPayload = receivedMessages.get(0);
            final Object payload = payloadAndErrorPayload.getPayload();
            assertEquals(payload, expectedPayload);
            final Object errorPayload = payloadAndErrorPayload.getErrorPayload();
            assertEquals(errorPayload, expectedErrorPayload);
        }
    }

    @SuppressWarnings("unchecked")
    private static List<TestSubscriber<PayloadAndErrorPayload<?, ?>>> getExpectedPayloadsReceivers(
            final TestEnvironment testEnvironment) {
        return (List<TestSubscriber<PayloadAndErrorPayload<?, ?>>>) testEnvironment.getProperty(EXPECTED_RECEIVERS);
    }

    private static void assertReceivedResultEqualsExpected(final TestEnvironment testEnvironment) {
        final Object expectedPayload = testEnvironment.getProperty(SEND_DATA);
        final Object expectedErrorPayload;
        if (testEnvironment.has(SEND_ERROR_DATA)) {
            expectedErrorPayload = testEnvironment.getProperty(SEND_ERROR_DATA);
        } else {
            expectedErrorPayload = null;
        }
        assertReceivedResultEqualsExpected(testEnvironment, expectedPayload, expectedErrorPayload);
    }

    private static void assertReceivedResultEqualsExpected(final TestEnvironment testEnvironment,
                                                           final Object expectedPayload,
                                                           final Object expectedErrorPayload) {
        final PayloadAndErrorPayload<?, ?> payloadAndErrorPayload = (PayloadAndErrorPayload<?, ?>) testEnvironment.getProperty(RESULT);
        final Object payload = payloadAndErrorPayload.getPayload();
        assertEquals(payload, expectedPayload);
        final Object errorPayload = payloadAndErrorPayload.getErrorPayload();
        assertEquals(errorPayload, expectedErrorPayload);
    }

    public TestValidation build() {
        return testValidation;
    }
}
