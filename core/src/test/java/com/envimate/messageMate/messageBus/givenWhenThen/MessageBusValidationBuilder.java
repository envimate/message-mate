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

package com.envimate.messageMate.messageBus.givenWhenThen;


import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.qcec.shared.TestValidation;
import com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeMessageBusSutActions;
import com.envimate.messageMate.shared.subscriber.TestException;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import com.envimate.messageMate.subscribing.Subscriber;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.envimate.messageMate.messageBus.givenWhenThen.MessageBusTestActions.messageBusTestActions;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.RESULT;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.SUT;
import static com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeMessageBusTestValidations.*;
import static com.envimate.messageMate.shared.validations.SharedTestValidations.*;
import static lombok.AccessLevel.PRIVATE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RequiredArgsConstructor(access = PRIVATE)
public final class MessageBusValidationBuilder {
    private final TestValidation testValidation;

    public static MessageBusValidationBuilder asValidation(final TestValidation testValidation) {
        return new MessageBusValidationBuilder(testValidation);
    }

    public static MessageBusValidationBuilder expectTheMessageToBeReceived() {
        return asValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertExpectedReceiverReceivedSingleMessage(testEnvironment);
        });
    }

    public static MessageBusValidationBuilder expectTheMessagesToBeReceivedByAllSubscriber(final TestMessage... testMessages) {
        return asValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            final List<TestMessage> expectedResult = Arrays.asList(testMessages);
            assertExpectedReceiverReceivedAllMessages(testEnvironment, expectedResult);
        });
    }

    public static MessageBusValidationBuilder expectAllMessagesToBeReceivedByAllSubscribers() {
        return asValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertExpectedReceiverReceivedAllMessages(testEnvironment);
        });
    }

    public static MessageBusValidationBuilder expectAllRemainingSubscribersToStillBeSubscribed() {
        return asValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            final PipeMessageBusSutActions sutActions = sutActions(testEnvironment);
            assertSutStillHasExpectedSubscriber(sutActions, testEnvironment);
        });
    }

    public static MessageBusValidationBuilder expectAllMessagesToHaveTheContentChanged() {
        return asValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            final PipeMessageBusSutActions sutActions = sutActions(testEnvironment);
            assertAllMessagesHaveContentChanged(sutActions, testEnvironment);
        });
    }

    public static MessageBusValidationBuilder expectOnlyValidMessageToBeReceived() {
        return asValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertReceiverReceivedOnlyValidMessages(testEnvironment);
        });
    }

    public static MessageBusValidationBuilder expectXMessagesToBeDelivered(final int expectedNumberOfDeliveredMessages) {
        return asValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertNumberOfMessagesReceived(testEnvironment, expectedNumberOfDeliveredMessages);
        });
    }


    public static MessageBusValidationBuilder expectNoMessagesToBeDelivered() {
        return asValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertNumberOfMessagesReceived(testEnvironment, 0);
        });
    }

    public static MessageBusValidationBuilder expectResultToBe(final Object expectedResult) {
        return asValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertResultEqualsExpected(testEnvironment, expectedResult);
        });
    }

    public static MessageBusValidationBuilder expectTimestampToBeInTheLastXSeconds(final long maximumSecondsDifference) {
        return asValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertTimestampToBeInTheLastXSeconds(testEnvironment, maximumSecondsDifference);
        });
    }

    public static MessageBusValidationBuilder expectAListOfSize(final int expectedSize) {
        return asValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertListOfSize(testEnvironment, expectedSize);
        });
    }

    public static MessageBusValidationBuilder expectTheCorrectChannel() {
        return asValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertResultAndExpectedResultAreEqual(testEnvironment);
        });
    }

    public static MessageBusValidationBuilder expectSubscriberOfType(final int expectedNumberOfSubscribers, final Class<?> messageClass) {
        return asValidation(testEnvironment -> {
            @SuppressWarnings("unchecked")
            final Map<Object, List<Subscriber<Object>>> resultMap = (Map<Object, List<Subscriber<Object>>>) testEnvironment.getProperty(RESULT);
            final List<Subscriber<Object>> subscribersForType = resultMap.get(messageClass);
            assertThat(subscribersForType.size(), equalTo(expectedNumberOfSubscribers));
        });
    }

    public static MessageBusValidationBuilder expectTheMessageBusToBeShutdownInTime() {
        return asValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            final PipeMessageBusSutActions sutActions = sutActions(testEnvironment);
            assertSutWasShutdownInTime(sutActions, testEnvironment);
        });
    }

    public static MessageBusValidationBuilder expectTheMessageBusToBeShutdown() {
        return asValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            final PipeMessageBusSutActions sutActions = sutActions(testEnvironment);
            assertSutIsShutdown(sutActions, testEnvironment);
        });
    }


    public static MessageBusValidationBuilder expectTheException(final Class<?> expectedExceptionClass) {
        return asValidation(testEnvironment -> assertExceptionThrownOfType(testEnvironment, expectedExceptionClass));
    }

    public static MessageBusValidationBuilder expectTheExceptionHandled(final Class<?> expectedExceptionClass) {
        return asValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertResultOfClass(testEnvironment, expectedExceptionClass);
        });
    }

    public static MessageBusValidationBuilder expectTheExceptionHandledAndTheErrorToBeThrown(final Class<?> expectedExceptionClass) {
        return asValidation(testEnvironment -> {
            assertExceptionThrownOfType(testEnvironment, TestException.class);
            assertResultOfClass(testEnvironment, expectedExceptionClass);
        });
    }

    public static MessageBusValidationBuilder expectAListWithAllFilters() {
        return asValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertResultEqualToExpectedFilter(testEnvironment);
        });
    }

    public static MessageBusValidationBuilder expectTheRemainingFilter() {
        return asValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            final PipeMessageBusSutActions sutActions = sutActions(testEnvironment);
            assertSutHasExpectedFilter(sutActions, testEnvironment);
        });
    }

    public static MessageBusValidationBuilder expectTheDynamicHandlerToNotBeCalled() {
        return asValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertNoResultSet(testEnvironment);
        });
    }

    private static PipeMessageBusSutActions sutActions(final TestEnvironment testEnvironment) {
        final MessageBus messageBus = getMessageBus(testEnvironment);
        return messageBusTestActions(messageBus);
    }

    private static MessageBus getMessageBus(final TestEnvironment testEnvironment) {
        final MessageBus messageBus = testEnvironment.getPropertyAsType(SUT, MessageBus.class);
        return messageBus;
    }

    public MessageBusValidationBuilder and(final MessageBusValidationBuilder messageBusValidationBuilder) {
        return new MessageBusValidationBuilder(testEnvironment -> {
            testValidation.validate(testEnvironment);
            messageBusValidationBuilder.testValidation.validate(testEnvironment);
        });
    }

    public TestValidation build() {
        return testValidation;
    }
}