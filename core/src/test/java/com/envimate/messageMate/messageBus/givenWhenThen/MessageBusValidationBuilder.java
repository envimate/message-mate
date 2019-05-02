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

package com.envimate.messageMate.messageBus.givenWhenThen;

import com.envimate.messageMate.identification.CorrelationId;
import com.envimate.messageMate.identification.MessageId;
import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.messageBus.exception.MessageBusExceptionListener;
import com.envimate.messageMate.processingContext.EventType;
import com.envimate.messageMate.processingContext.ProcessingContext;
import com.envimate.messageMate.shared.environment.TestEnvironment;
import com.envimate.messageMate.shared.givenWhenThen.TestValidation;
import com.envimate.messageMate.shared.pipeChannelMessageBus.testActions.MessageBusSutActions;
import com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeMessageBusSutActions;
import com.envimate.messageMate.shared.polling.PollingUtils;
import com.envimate.messageMate.shared.subscriber.TestException;
import com.envimate.messageMate.shared.subscriber.TestSubscriber;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import com.envimate.messageMate.shared.validations.SharedTestValidations;
import com.envimate.messageMate.subscribing.Subscriber;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.envimate.messageMate.messageBus.givenWhenThen.MessageBusTestActions.queryListOfDynamicExceptionListener;
import static com.envimate.messageMate.messageBus.givenWhenThen.MessageBusTestActionsOld.messageBusTestActions;
import static com.envimate.messageMate.messageBus.givenWhenThen.MessageBusTestProperties.MESSAGE_RECEIVED_BY_ERROR_LISTENER;
import static com.envimate.messageMate.shared.environment.TestEnvironmentProperty.RESULT;
import static com.envimate.messageMate.shared.environment.TestEnvironmentProperty.SUT;
import static com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeChannelMessageBusSharedTestProperties.*;
import static com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeChannelMessageBusSharedTestValidations.*;
import static com.envimate.messageMate.shared.polling.PollingUtils.pollUntil;
import static com.envimate.messageMate.shared.polling.PollingUtils.pollUntilEquals;
import static com.envimate.messageMate.shared.validations.SharedTestValidations.*;
import static lombok.AccessLevel.PRIVATE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RequiredArgsConstructor(access = PRIVATE)
public final class MessageBusValidationBuilder {
    private final TestValidation testValidation;

    private static MessageBusValidationBuilder asValidation(final TestValidation testValidation) {
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

    public static MessageBusValidationBuilder expectTheErrorPayloadToBeReceived() {
        return asValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertExpectedReceiverReceivedMessageWithErrorPayload(testEnvironment);
        });
    }

    public static MessageBusValidationBuilder expectAllRemainingSubscribersToStillBeSubscribed() {
        return asValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            final PipeMessageBusSutActions sutActions = sutActions(testEnvironment); //TODO: müssem hier alle weg
            assertSutStillHasExpectedSubscriber(sutActions, testEnvironment);
        });
    }

    public static MessageBusValidationBuilder expectAllMessagesToHaveTheContentChanged() {
        return asValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            final MessageBusSutActions sutActions = sutActionsNew(testEnvironment);
            assertAllMessagesHaveContentChanged(sutActions, testEnvironment);
        });
    }

    public static MessageBusValidationBuilder expectAllProcessingContextsToBeReplaced() {
        return asValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            final MessageBus messageBus = getMessageBus(testEnvironment);
            assertAllReceivedProcessingContextsWereChanged(messageBus, testEnvironment);
        });
    }

    public static MessageBusValidationBuilder expectTheMessageToHaveTheSameMessageIdAndAMatchingGeneratedCorrelationId() {
        return asValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            final ProcessingContext<?> result = getOnlyMessageFromSingleReceiver(testEnvironment);
            assertTheMessageToHaveTheSameMessageIdAndAMatchingGeneratedCorrelationId(testEnvironment, result);
        });
    }

    public static MessageBusValidationBuilder expectTheCorrelationIdToBeSetWhenReceived() {
        return asValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            final ProcessingContext<?> result = getOnlyMessageFromSingleReceiver(testEnvironment);
            assertTheCorrelationIdToBeSetWhenReceived(testEnvironment, result);
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
            //TODO: reusable
            pollUntil(() -> {

                if (testEnvironment.has(SINGLE_SEND_MESSAGE)) {
                    return true;
                } else {
                    if (testEnvironment.has(NUMBER_OF_MESSAGES_SHOULD_BE_SEND)) {
                        final Object expectedNumberOfMessages = testEnvironment.getProperty(NUMBER_OF_MESSAGES_SHOULD_BE_SEND);
                        final List<?> actualSendMessages = testEnvironment.getPropertyAsType(MESSAGES_SEND, List.class);
                        return testEquals(expectedNumberOfMessages, actualSendMessages.size());
                    } else {
                        return false;
                    }
                }
            });
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

    public static MessageBusValidationBuilder expectSubscriberOfType(final int expectedNumberOfSubscribers,
                                                                     final EventType eventType) {
        return asValidation(testEnvironment -> {
            @SuppressWarnings("unchecked")
            final Map<EventType, List<Subscriber<?>>> resultMap =
                    (Map<EventType, List<Subscriber<?>>>) testEnvironment.getProperty(RESULT);
            final List<Subscriber<?>> subscribersForType = resultMap.get(eventType);
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

    public static MessageBusValidationBuilder expectNoException() {
        return asValidation(SharedTestValidations::assertNoExceptionThrown);
    }

    public static MessageBusValidationBuilder expectTheException(final Class<?> expectedExceptionClass) {
        return asValidation(testEnvironment -> assertExceptionThrownOfType(testEnvironment, expectedExceptionClass));
    }

    public static MessageBusValidationBuilder expectTheExceptionHandled(final Class<?> expectedExceptionClass) {
        return asValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            pollUntil(() -> testEnvironment.has(RESULT));
            assertResultOfClass(testEnvironment, expectedExceptionClass);
            final ProcessingContext<?> processingContext = getReceivedErrorMessage(testEnvironment);
            final Object message = processingContext.getPayload();
            final Object expectedPayload = testEnvironment.getProperty(SINGLE_SEND_MESSAGE);
            assertEquals(message, expectedPayload);
            final MessageId messageId = processingContext.getMessageId();
            final Object expectedMessageId = testEnvironment.getProperty(SEND_MESSAGE_ID);
            assertEquals(messageId, expectedMessageId);
        });
    }

    private static ProcessingContext<?> getReceivedErrorMessage(final TestEnvironment testEnvironment) {
        return testEnvironment.getPropertyAsType(MESSAGE_RECEIVED_BY_ERROR_LISTENER, ProcessingContext.class);
    }

    public static MessageBusValidationBuilder expectTheExceptionHandledAsFilterException(final Class<?> expectedExceptionClass) {
        return expectTheExceptionHandledAs(expectedExceptionClass, EXCEPTION_OCCURRED_INSIDE_FILTER);
    }

    public static MessageBusValidationBuilder expectTheExceptionHandledAsDeliverException(final Class<?> expectedExceptionClass) {
        return expectTheExceptionHandledAs(expectedExceptionClass, EXCEPTION_OCCURRED_DURING_DELIVERY);
    }

    private static MessageBusValidationBuilder expectTheExceptionHandledAs(final Class<?> expectedExceptionClass,
                                                                           final String expectedExceptionProperty) {
        return asValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            pollUntil(() -> testEnvironment.has(RESULT));
            assertResultOfClass(testEnvironment, expectedExceptionClass);
            assertPropertyTrue(testEnvironment, expectedExceptionProperty);
        });
    }

    public static MessageBusValidationBuilder expectTheExceptionHandledOnlyByTheRemainingHandlers(final Class<?> expectedExceptionClass) {
        return expectTheExceptionHandled(expectedExceptionClass);
    }

    public static MessageBusValidationBuilder expectTheExceptionHandledAndTheErrorToBeThrown(
            final Class<?> expectedExceptionClass) {
        return asValidation(testEnvironment -> {
            assertExceptionThrownOfType(testEnvironment, TestException.class, RESULT);
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

    public static MessageBusValidationBuilder expectTheMessageWrappedInProcessingContextWithCorrectCorrelationIdToBeReceived() {
        return asValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertAllReceiverReceivedProcessingContextWithCorrectCorrelationId(testEnvironment);
        });
    }

    public static MessageBusValidationBuilder expectNumberOfErrorListener(final int expectedNumberOfListener) {
        return asValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            final MessageBus messageBus = getMessageBus(testEnvironment);
            final List<MessageBusExceptionListener> listeners = queryListOfDynamicExceptionListener(messageBus);
            assertCollectionOfSize(listeners, expectedNumberOfListener);
        });
    }

    private static void assertAllReceiverReceivedProcessingContextWithCorrectCorrelationId(
            final TestEnvironment testEnvironment) {
        final List<TestSubscriber<ProcessingContext<Object>>> receivers =
                getExpectedReceiverAsCorrelationBasedSubscriberList(testEnvironment);
        for (final TestSubscriber<ProcessingContext<Object>> receiver : receivers) {
            final List<ProcessingContext<Object>> receivedMessages = receiver.getReceivedMessages();
            pollUntilEquals(receivedMessages::size, 1);
            final ProcessingContext<Object> processingContext = receivedMessages.get(0);
            final CorrelationId expectedCorrelationId = getExpectedCorrelationId(testEnvironment);
            assertEquals(processingContext.getCorrelationId(), expectedCorrelationId);
            final Object expectedResult = testEnvironment.getProperty(SINGLE_SEND_MESSAGE);
            assertEquals(processingContext.getPayload(), expectedResult);
        }
    }

    private static CorrelationId getExpectedCorrelationId(final TestEnvironment testEnvironment) {
        return testEnvironment.getPropertyAsType(EXPECTED_CORRELATION_ID, CorrelationId.class);
    }

    @SuppressWarnings("unchecked")
    private static List<TestSubscriber<ProcessingContext<Object>>> getExpectedReceiverAsCorrelationBasedSubscriberList(
            final TestEnvironment testEnvironment) {
        return (List<TestSubscriber<ProcessingContext<Object>>>) testEnvironment.getProperty(EXPECTED_RECEIVERS);
    }

    private static PipeMessageBusSutActions sutActions(final TestEnvironment testEnvironment) {
        final MessageBus messageBus = getMessageBus(testEnvironment);
        return messageBusTestActions(messageBus);
    }


    private static MessageBusSutActions sutActionsNew(final TestEnvironment testEnvironment) {
        final MessageBus messageBus = getMessageBus(testEnvironment);
        return MessageBusSutActions.messageBusSutActions(messageBus);
    }

    private static MessageBus getMessageBus(final TestEnvironment testEnvironment) {
        final MessageBus messageBus = testEnvironment.getPropertyAsType(SUT, MessageBus.class);
        return messageBus;
    }

    private static ProcessingContext<?> getOnlyMessageFromSingleReceiver(final TestEnvironment testEnvironment) {
        final List<TestSubscriber<Object>> testSubscribers = getExpectedReceiver(testEnvironment);
        assertThat(testSubscribers.size(), equalTo(1));
        final TestSubscriber<?> testSubscriber = testSubscribers.get(0);
        final List<?> receivedMessages = testSubscriber.getReceivedMessages();
        pollUntilEquals(receivedMessages::size, 1);
        return (ProcessingContext<?>) receivedMessages.get(0);
    }

    @SuppressWarnings("unchecked")
    private static List<TestSubscriber<Object>> getExpectedReceiver(final TestEnvironment testEnvironment) {
        return (List<TestSubscriber<Object>>) testEnvironment.getProperty(EXPECTED_RECEIVERS);
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
