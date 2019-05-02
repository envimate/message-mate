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

package com.envimate.messageMate.internal.pipe.givenWhenThen;

import com.envimate.messageMate.internal.pipe.Pipe;
import com.envimate.messageMate.internal.pipe.PipeStatusInformation;
import com.envimate.messageMate.internal.pipe.statistics.PipeStatistics;
import com.envimate.messageMate.shared.environment.TestEnvironment;
import com.envimate.messageMate.shared.givenWhenThen.TestValidation;
import com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeMessageBusSutActions;
import com.envimate.messageMate.shared.polling.PollingUtils;
import com.envimate.messageMate.shared.subscriber.TestException;
import com.envimate.messageMate.shared.subscriber.TestSubscriber;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import com.envimate.messageMate.shared.validations.SharedTestValidations;
import lombok.RequiredArgsConstructor;

import java.math.BigInteger;
import java.util.List;

import static com.envimate.messageMate.internal.pipe.givenWhenThen.PipeTestActions.pipeTestActions;
import static com.envimate.messageMate.shared.environment.TestEnvironmentProperty.RESULT;
import static com.envimate.messageMate.shared.environment.TestEnvironmentProperty.SUT;
import static com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeChannelMessageBusSharedTestProperties.*;
import static com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeChannelMessageBusSharedTestValidations.*;
import static com.envimate.messageMate.shared.validations.SharedTestValidations.*;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class PipeValidationBuilder {
    private final TestValidation testValidation;

    public static PipeValidationBuilder expectTheMessageToBeReceived() {
        return new PipeValidationBuilder(testEnvironment -> {

            final List<TestSubscriber<?>> subscribers = getAsSubscriberList(testEnvironment, EXPECTED_RECEIVERS);
            PollingUtils.pollUntil(() -> subscribers.stream().allMatch(s -> s.getReceivedMessages().size() == 1));
            assertNoExceptionThrown(testEnvironment);
            assertExpectedReceiverReceivedSingleMessage(testEnvironment);
        });
    }

    public static PipeValidationBuilder expectAllMessagesToBeReceivedByAllSubscribers() {
        return new PipeValidationBuilder(testEnvironment -> {
            final List<TestSubscriber<?>> subscribers = getAsSubscriberList(testEnvironment, EXPECTED_RECEIVERS);
            final List<?> sendMessages = testEnvironment.getPropertyAsListOfType(MESSAGES_SEND, Object.class);
            final int expectedNumberOfMessages = sendMessages.size();
            PollingUtils.pollUntil(() -> subscribers.stream().allMatch(s -> s.getReceivedMessages().size() == expectedNumberOfMessages));
            assertNoExceptionThrown(testEnvironment);
            assertExpectedReceiverReceivedAllMessages(testEnvironment);
        });
    }

    public static PipeValidationBuilder expectEachMessagesToBeReceivedByOnlyOneSubscriber() {
        return new PipeValidationBuilder(testEnvironment -> {
            final List<TestSubscriber<?>> subscribers = getAsSubscriberList(testEnvironment, POTENTIAL_RECEIVERS);
            final List<?> sendMessages = testEnvironment.getPropertyAsListOfType(MESSAGES_SEND, Object.class);
            final int expectedNumberOfMessages = sendMessages.size();
            PollingUtils.pollUntilEquals(() -> subscribers.stream().mapToInt(s -> s.getReceivedMessages().size()).sum(), expectedNumberOfMessages);
            assertNoExceptionThrown(testEnvironment);
            assertEachMessagesToBeReceivedByOnlyOneSubscriber(testEnvironment);
        });
    }

    public static PipeValidationBuilder expectAllRemainingSubscribersToStillBeSubscribed() {
        return new PipeValidationBuilder(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            final PipeMessageBusSutActions sutActions = sutActions(testEnvironment);
            assertSutStillHasExpectedSubscriber(sutActions, testEnvironment);
        });
    }

    public static PipeValidationBuilder expectTheListOfAllSubscriber() {
        return new PipeValidationBuilder(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertResultEqualsCurrentSubscriber(testEnvironment);
        });
    }

    public static PipeValidationBuilder expectXMessagesToBeDelivered_despiteTheChannelClosed(final int expectedNumberOfMessages) {
        return new PipeValidationBuilder(testEnvironment -> {
            final TestSubscriber<?> subscriber = testEnvironment.getPropertyAsType(SINGLE_RECEIVER, TestSubscriber.class);
            PollingUtils.pollUntilEquals(() -> subscriber.getReceivedMessages().size(), expectedNumberOfMessages);
            assertNoExceptionThrown(testEnvironment);
            assertNumberOfMessagesReceived(testEnvironment, expectedNumberOfMessages);
        });
    }

    public static PipeValidationBuilder expectResultToBe(final Object expectedResult) {
        return new PipeValidationBuilder(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertResultEqualsExpected(testEnvironment, expectedResult);
        });
    }

    public static PipeValidationBuilder expectTimestampToBeInTheLastXSeconds(final long maximumSecondsDifference) {
        return new PipeValidationBuilder(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertTimestampToBeInTheLastXSeconds(testEnvironment, maximumSecondsDifference);
        });
    }

    public static PipeValidationBuilder expectThePipeToBeShutdownInTime() {
        return new PipeValidationBuilder(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            final PipeMessageBusSutActions sutActions = sutActions(testEnvironment);
            assertSutWasShutdownInTime(sutActions, testEnvironment);
        });
    }

    public static PipeValidationBuilder expectThePipeToBeShutdown() {
        return new PipeValidationBuilder(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            final PipeMessageBusSutActions sutActions = sutActions(testEnvironment);
            assertSutIsShutdown(sutActions, testEnvironment);
        });
    }

    public static PipeValidationBuilder expectTheException(final Class<?> expectedExceptionClass) {
        return new PipeValidationBuilder(testEnvironment -> assertExceptionThrownOfType(testEnvironment, expectedExceptionClass));
    }

    public static PipeValidationBuilder expectNoException() {
        return new PipeValidationBuilder(SharedTestValidations::assertNoExceptionThrown);
    }

    public static PipeValidationBuilder expectTheResultToAlwaysBeFalse() {
        return new PipeValidationBuilder(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertResultEqualsExpected(testEnvironment, false);
        });
    }

    public static PipeValidationBuilder expectTheAwaitToBeTerminatedSuccessful(final int expectedNumberOfReceivedMessages) {
        return new PipeValidationBuilder(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertResultEqualsExpected(testEnvironment, true);
            assertNumberOfMessagesReceived(testEnvironment, expectedNumberOfReceivedMessages);
        });
    }

    public static PipeValidationBuilder expectTheAwaitToBeTerminatedSuccessful() {
        return new PipeValidationBuilder(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertResultEqualsExpected(testEnvironment, true);
        });
    }

    public static PipeValidationBuilder expectTheAwaitToBeTerminatedWithFailure() {
        return new PipeValidationBuilder(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertResultEqualsExpected(testEnvironment, false);
        });
    }

    public static PipeValidationBuilder expectTheExceptionToBeHandled() {
        return new PipeValidationBuilder(testEnvironment -> {
            PollingUtils.pollUntil(() -> testEnvironment.has(RESULT));
            assertNoExceptionThrown(testEnvironment);
            assertResultOfClass(testEnvironment, TestException.class);
        });
    }

    public static PipeValidationBuilder expectTheDeliveryToBeStillSuccessful() {
        return new PipeValidationBuilder(testEnvironment -> {
            PollingUtils.pollUntil(() -> testEnvironment.has(EXPECTED_AND_IGNORED_EXCEPTION));
            assertNoExceptionThrown(testEnvironment);
            assertNumberOfMessagesReceived(testEnvironment, 0);
            final Pipe<TestMessage> pipe = getPipe(testEnvironment);
            final PipeStatusInformation<TestMessage> statusInformation = pipe.getStatusInformation();
            final PipeStatistics currentMessageStatistics = statusInformation.getCurrentMessageStatistics();
            final BigInteger successfulMessages = currentMessageStatistics.getSuccessfulMessages();
            final long result = successfulMessages.longValueExact();
            assertEquals(result, 1L);
        });
    }

    private static PipeMessageBusSutActions sutActions(final TestEnvironment testEnvironment) {
        final Pipe<TestMessage> pipe = getPipe(testEnvironment);
        return pipeTestActions(pipe);
    }

    @SuppressWarnings("unchecked")
    private static Pipe<TestMessage> getPipe(final TestEnvironment testEnvironment) {
        return (Pipe<TestMessage>) testEnvironment.getProperty(SUT);
    }

    @SuppressWarnings("unchecked")
    private static List<TestSubscriber<?>> getAsSubscriberList(final TestEnvironment testEnvironment,
                                                               final String property) {
        return (List<TestSubscriber<?>>) testEnvironment.getProperty(property);
    }

    public TestValidation build() {
        return testValidation;
    }
}
