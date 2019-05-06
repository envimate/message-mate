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

package com.envimate.messageMate.shared.utils;

import com.envimate.messageMate.identification.CorrelationId;
import com.envimate.messageMate.identification.MessageId;
import com.envimate.messageMate.processingContext.EventType;
import com.envimate.messageMate.processingContext.ProcessingContext;
import com.envimate.messageMate.shared.environment.TestEnvironment;
import com.envimate.messageMate.shared.eventType.TestEventType;
import com.envimate.messageMate.shared.pipeChannelMessageBus.testActions.CorrelationIdSendingActions;
import com.envimate.messageMate.shared.pipeChannelMessageBus.testActions.ProcessingContextSendingActions;
import com.envimate.messageMate.shared.pipeChannelMessageBus.testActions.SendingActions;
import com.envimate.messageMate.shared.pipeChannelMessageBus.testActions.SubscribeActions;
import com.envimate.messageMate.shared.subscriber.BlockingTestSubscriber;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import com.envimate.messageMate.shared.testMessages.TestMessageOfInterest;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static com.envimate.messageMate.identification.CorrelationId.newUniqueCorrelationId;
import static com.envimate.messageMate.processingContext.ProcessingContext.processingContext;
import static com.envimate.messageMate.processingContext.ProcessingContext.processingContextForError;
import static com.envimate.messageMate.shared.eventType.TestEventType.testEventType;
import static com.envimate.messageMate.shared.properties.SharedTestProperties.*;
import static com.envimate.messageMate.shared.polling.PollingUtils.pollUntilEquals;
import static com.envimate.messageMate.shared.subscriber.BlockingTestSubscriber.blockingTestSubscriber;
import static com.envimate.messageMate.shared.testMessages.TestMessageOfInterest.messageOfInterest;
import static com.envimate.messageMate.shared.testMessages.TestMessageOfInterest.messageWithErrorContent;
import static com.envimate.messageMate.shared.utils.TestMessageFactory.*;
import static java.util.concurrent.TimeUnit.SECONDS;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class SendingTestUtils {

    public static void sendSingleMessage(final SendingActions sendingActions,
                                         final TestEnvironment testEnvironment) {
        final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
        sendSingleMessage(sendingActions, testEnvironment, eventType);
    }

    public static void sendSingleMessage(final SendingActions sendingActions,
                                         final TestEnvironment testEnvironment,
                                         final EventType eventType) {
        final TestMessageOfInterest message = messageOfInterest();
        sendSingleMessage(sendingActions, testEnvironment, eventType, message);
    }

    public static void sendSingleMessage(final SendingActions sendingActions,
                                         final TestEnvironment testEnvironment,
                                         final EventType eventType,
                                         final TestMessage message) {
        testEnvironment.setProperty(SINGLE_SEND_MESSAGE, message);
        final MessageId messageId = sendingActions.send(eventType, message);
        testEnvironment.setProperty(SEND_MESSAGE_ID, messageId);
        testEnvironment.setProperty(NUMBER_OF_MESSAGES_SHOULD_BE_SEND, 1);
    }

    public static void sendSeveralMessages(final SendingActions sendingActions,
                                           final int numberOfMessages,
                                           final TestEnvironment testEnvironment) {
        final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
        testEnvironment.setProperty(NUMBER_OF_MESSAGES_SHOULD_BE_SEND, numberOfMessages);
        for (int i = 0; i < numberOfMessages; i++) {
            final TestMessageOfInterest message = messageOfInterest();
            sendingActions.send(eventType, message);
            testEnvironment.addToListProperty(MESSAGES_SEND, message);
        }
    }

    public static void sendMessageWithCorrelationId(final CorrelationIdSendingActions sendingActions,
                                                    final TestEnvironment testEnvironment) {
        final CorrelationId corId = newUniqueCorrelationId();
        final CorrelationId correlationId = testEnvironment.getPropertyOrSetDefault(EXPECTED_CORRELATION_ID, corId);

        sendMessageWithCorrelationId(sendingActions, testEnvironment, correlationId);
    }

    public static void sendMessageWithErrorPayloadIsSend(final ProcessingContextSendingActions sendingActions,
                                                         final TestEnvironment testEnvironment) {
        final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
        final TestMessageOfInterest errorPayload = messageWithErrorContent();
        testEnvironment.setProperty(SEND_ERROR_PAYLOAD, errorPayload);
        final ProcessingContext<TestMessage> processingContext = processingContextForError(eventType, errorPayload);
        sendProcessingContext(sendingActions, testEnvironment, processingContext);
    }

    public static void sendMessageWithCorrelationId(final CorrelationIdSendingActions sendingActions,
                                                    final TestEnvironment testEnvironment,
                                                    final CorrelationId correlationId) {
        final TestMessageOfInterest message = messageOfInterest();
        testEnvironment.setProperty(SINGLE_SEND_MESSAGE, message);

        final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
        final MessageId messageId = sendingActions.send(eventType, message, correlationId);
        testEnvironment.setProperty(SEND_MESSAGE_ID, messageId);
        testEnvironment.setProperty(NUMBER_OF_MESSAGES_SHOULD_BE_SEND, 1);
    }

    public static void sendMessageAsProcessingContext(final ProcessingContextSendingActions sendingActions,
                                                      final TestEnvironment testEnvironment,
                                                      final TestMessage message) {
        final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
        sendMessageAsProcessingContext(sendingActions, testEnvironment, message, eventType);
    }

    public static void sendMessageAsProcessingContext(final ProcessingContextSendingActions sendingActions,
                                                      final TestEnvironment testEnvironment,
                                                      final TestMessage message,
                                                      final EventType eventType) {
        final ProcessingContext<TestMessage> processingContext = processingContext(eventType, message);
        sendProcessingContext(sendingActions, testEnvironment, processingContext);
    }

    public static void sendProcessingContext(final ProcessingContextSendingActions sendingActions,
                                             final TestEnvironment testEnvironment,
                                             final ProcessingContext<TestMessage> processingContext) {
        testEnvironment.setProperty(SINGLE_SEND_MESSAGE, processingContext);
        final MessageId messageId = sendingActions.send(processingContext);
        testEnvironment.setProperty(SEND_MESSAGE_ID, messageId);
        testEnvironment.setProperty(NUMBER_OF_MESSAGES_SHOULD_BE_SEND, 1);
    }

    public static void sendValidMessagesAsynchronouslyNew(final SendingActions sendingActions,
                                                          final TestEnvironment testEnvironment,
                                                          final int numberOfSender,
                                                          final int numberOfMessagesPerSender,
                                                          final boolean expectCleanShutdown) {
        final TestMessageFactory messageFactory = messageFactoryForValidMessages(numberOfMessagesPerSender);
        final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
        sendXMessagesAsynchronously(numberOfSender, messageFactory, eventType, sendingActions, testEnvironment, expectCleanShutdown);
    }

    public static void sendInvalidMessagesAsynchronouslyNew(final SendingActions sendingActions,
                                                            final TestEnvironment testEnvironment,
                                                            final int numberOfSender, final int numberOfMessagesPerSender) {

        final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
        sendXMessagesAsynchronously(numberOfSender, messageFactoryForInvalidMessages(numberOfMessagesPerSender), eventType,
                sendingActions, testEnvironment, true);
    }

    public static void sendMixtureOfValidAndInvalidMessagesAsynchronouslyNew(final SendingActions sendingActions,
                                                                             final TestEnvironment testEnvironment,
                                                                             final int numberOfSender,
                                                                             final int numberOfMessagesPerSender) {
        final TestMessageFactory messageFactory = messageFactoryForRandomValidOrInvalidTestMessages(numberOfMessagesPerSender);

        final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
        sendXMessagesAsynchronously(numberOfSender, messageFactory, eventType, sendingActions, testEnvironment, true);
    }


    private static void sendXMessagesAsynchronously(final int numberOfSender,
                                                    final MessageFactory messageFactory,
                                                    final EventType eventType,
                                                    final SendingActions sendingActions,
                                                    final TestEnvironment testEnvironment,
                                                    final boolean expectCleanShutdown) {
        if (numberOfSender <= 0) {
            return;
        }
        final int numberOfMessages = messageFactory.numberOfMessages();
        final int expectedNumberOfMessagesSend = numberOfSender * numberOfMessages;
        testEnvironment.setProperty(NUMBER_OF_MESSAGES_SHOULD_BE_SEND, expectedNumberOfMessagesSend);
        final CyclicBarrier sendingStartBarrier = new CyclicBarrier(numberOfSender);
        final ExecutorService executorService = Executors.newFixedThreadPool(numberOfSender);
        for (int i = 0; i < numberOfSender; i++) {
            executorService.execute(() -> {
                final List<TestMessage> messagesToSend = new ArrayList<>();

                for (int j = 0; j < numberOfMessages; j++) {
                    final TestMessage message = messageFactory.createMessage();
                    messagesToSend.add(message);
                    testEnvironment.addToListProperty(MESSAGES_SEND, message);
                }
                try {
                    sendingStartBarrier.await(3, SECONDS);
                } catch (final InterruptedException | BrokenBarrierException | TimeoutException e) {
                    throw new RuntimeException(e);
                }
                for (final TestMessage message : messagesToSend) {
                    sendingActions.send(eventType, message);
                }
            });
        }
        executorService.shutdown();
        if (expectCleanShutdown) {
            try {
                final boolean isTerminated = executorService.awaitTermination(3, SECONDS);
                if (!isTerminated) {
                    throw new RuntimeException("ExecutorService did not shutdown within timeout.");
                }
            } catch (final InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void sendXMessagesAsynchronouslyThatWillFail(final SendingActions sendingActions,
                                                               final int numberOfMessages,
                                                               final TestEnvironment testEnvironment) {
        final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
        final ExecutorService executorService = Executors.newFixedThreadPool(numberOfMessages);
        for (int i = 0; i < numberOfMessages; i++) {
            executorService.execute(() -> {
                try {
                    final TestMessageOfInterest message = messageOfInterest();
                    sendingActions.send(eventType, message);
                } catch (final Exception ignored) {
                    //ignore
                }
            });
        }
        executorService.shutdown();
    }

    public static void sendXMessagesInTheirOwnThreadThatWillBeBlocked(final SendingActions sendingActions,
                                                                      final int numberOfMessages,
                                                                      final TestEnvironment testEnvironment) {
        final ExecutorService executorService = Executors.newFixedThreadPool(numberOfMessages);
        final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
        for (int i = 0; i < numberOfMessages; i++) {
            executorService.execute(() -> {
                final TestMessageOfInterest message = messageOfInterest();
                testEnvironment.addToListProperty(MESSAGES_SEND, message);
                System.out.println("message send = ");
                sendingActions.send(eventType, message);
            });
        }
    }

    public static <T extends SendingActions & SubscribeActions> Semaphore addABlockingSubscriberAndThenSendXMessagesInEachThread(
            final T sutActions,
            final int numberOfMessages,
            final TestEnvironment testEnvironment) {
        final Semaphore semaphore = new Semaphore(0);
        final BlockingTestSubscriber<TestMessage> subscriber = blockingTestSubscriber(semaphore);
        addABlockingSubscriberAndThenSendXMessagesInEachThread(sutActions, subscriber, numberOfMessages, testEnvironment);
        return semaphore;
    }

    public static <T extends SendingActions & SubscribeActions> Semaphore addABlockingSubscriberAndThenSendXMessagesInEachThread(
            final T sutActions,
            final int numberOfMessages,
            final int expectedNumberOfBlockedThreads,
            final TestEnvironment testEnvironment) {
        final Semaphore semaphore = new Semaphore(0);
        final BlockingTestSubscriber<TestMessage> subscriber = blockingTestSubscriber(semaphore);
        addABlockingSubscriberAndThenSendXMessagesInEachThread(sutActions, subscriber, numberOfMessages, 1, testEnvironment, expectedNumberOfBlockedThreads);
        return semaphore;
    }

    public static <T extends SendingActions & SubscribeActions> void addABlockingSubscriberAndThenSendXMessagesInEachThread(
            final T sutActions,
            final BlockingTestSubscriber<TestMessage> subscriber,
            final int numberOfMessages,
            final TestEnvironment testEnvironment) {
        addABlockingSubscriberAndThenSendXMessagesInEachThread(sutActions, subscriber, numberOfMessages, 1, testEnvironment, numberOfMessages);
    }

    public static <T extends SendingActions & SubscribeActions> void addABlockingSubscriberAndThenSendXMessagesInEachThread(
            final T sutActions,
            final BlockingTestSubscriber<TestMessage> subscriber,
            final int numberOfSenders,
            final int numberOfMessagesPerSender,
            final TestEnvironment testEnvironment,
            final int expectedNumberOfBlockedThreads) {
        final EventType eventType = TestEventType.testEventType();
        sutActions.subscribe(eventType, subscriber);
        testEnvironment.setProperty(SINGLE_RECEIVER, subscriber);

        final TestMessageFactory messageFactory = messageFactoryForValidMessages(numberOfMessagesPerSender);
        sendXMessagesAsynchronously(numberOfSenders, messageFactory, eventType, sutActions, testEnvironment, false);
        pollUntilEquals(() -> {
            final int blockedThreads = subscriber.getNumberOfBlockedThreads();
            System.out.println("blockedThreads = " + blockedThreads + ", expected: " + numberOfSenders);
            return blockedThreads;
        }, expectedNumberOfBlockedThreads);
    }
}
