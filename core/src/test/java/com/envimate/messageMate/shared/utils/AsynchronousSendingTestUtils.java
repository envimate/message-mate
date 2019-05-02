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

import com.envimate.messageMate.processingContext.EventType;
import com.envimate.messageMate.shared.environment.TestEnvironment;
import com.envimate.messageMate.shared.eventType.TestEventType;
import com.envimate.messageMate.shared.pipeChannelMessageBus.testActions.SendingActions;
import com.envimate.messageMate.shared.pipeChannelMessageBus.testActions.SubscribeActions;
import com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeMessageBusSutActions;
import com.envimate.messageMate.shared.polling.PollingUtils;
import com.envimate.messageMate.shared.subscriber.BlockingTestSubscriber;
import com.envimate.messageMate.shared.subscriber.TestSubscriber;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import com.envimate.messageMate.shared.testMessages.TestMessageOfInterest;
import com.envimate.messageMate.subscribing.Subscriber;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.envimate.messageMate.messageBus.givenWhenThen.MessageBusTestProperties.EVENT_TYPE;
import static com.envimate.messageMate.shared.eventType.TestEventType.testEventType;
import static com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeChannelMessageBusSharedTestProperties.*;
import static com.envimate.messageMate.shared.subscriber.BlockingTestSubscriber.blockingTestSubscriber;
import static com.envimate.messageMate.shared.testMessages.TestMessageOfInterest.messageOfInterest;
import static com.envimate.messageMate.shared.utils.TestMessageFactory.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class AsynchronousSendingTestUtils {

    public static void sendValidMessagesAsynchronously(final PipeMessageBusSutActions sutActions,
                                                       final TestEnvironment testEnvironment,
                                                       final int numberOfSender,
                                                       final int numberOfMessagesPerSender,
                                                       final boolean expectCleanShutdown) {
        final TestMessageFactory messageFactory = testMessageFactoryForValidMessages(numberOfMessagesPerSender, testEnvironment);
        final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
        sendXMessagesAsynchronously(numberOfSender, messageFactory, eventType, (eventType1, message) -> sutActions.send(message), testEnvironment, expectCleanShutdown);
    }

    public static void sendValidMessagesAsynchronously(final Consumer<TestMessage> sutSend,
                                                       final TestEnvironment testEnvironment,
                                                       final int numberOfSender,
                                                       final int numberOfMessagesPerSender,
                                                       final boolean expectCleanShutdown) {
        final TestMessageFactory messageFactory = testMessageFactoryForValidMessages(numberOfMessagesPerSender, testEnvironment);
        final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
        sendXMessagesAsynchronously(numberOfSender, messageFactory, eventType, (eventType1, message) -> sutSend.accept(message), testEnvironment, expectCleanShutdown);
    }

    public static void sendValidMessagesAsynchronouslyNew(final SendingActions sendingActions,
                                                          final TestEnvironment testEnvironment,
                                                          final int numberOfSender,
                                                          final int numberOfMessagesPerSender,
                                                          final boolean expectCleanShutdown) {
        final TestMessageFactory messageFactory = testMessageFactoryForValidMessages(numberOfMessagesPerSender, testEnvironment);
        final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
        sendXMessagesAsynchronously(numberOfSender, messageFactory, eventType, sendingActions, testEnvironment, expectCleanShutdown);
    }

    public static void sendInvalidMessagesAsynchronously(final Consumer<TestMessage> sendConsumer,
                                                         final TestEnvironment testEnvironment,
                                                         final int numberOfSender, final int numberOfMessagesPerSender) {

        final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
        sendXMessagesAsynchronously(numberOfSender, testMessageFactoryForInvalidMessages(numberOfMessagesPerSender), eventType,
                (eventType1, message) -> sendConsumer.accept(message), testEnvironment, true);
    }

    public static void sendInvalidMessagesAsynchronouslyNew(final SendingActions sendingActions,
                                                            final TestEnvironment testEnvironment,
                                                            final int numberOfSender, final int numberOfMessagesPerSender) {

        final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
        sendXMessagesAsynchronously(numberOfSender, testMessageFactoryForInvalidMessages(numberOfMessagesPerSender), eventType,
                sendingActions, testEnvironment, true);
    }

    public static void sendMixtureOfValidAndInvalidMessagesAsynchronously(final Consumer<TestMessage> sutSend,
                                                                          final TestEnvironment testEnvironment,
                                                                          final int numberOfSender,
                                                                          final int numberOfMessagesPerSender) {
        final TestMessageFactory messageFactory = testMessageFactoryForRandomValidOrInvalidTestMessages(numberOfMessagesPerSender,
                testEnvironment);

        final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
        sendXMessagesAsynchronously(numberOfSender, messageFactory, eventType, (eventType1, message) -> sutSend.accept(message), testEnvironment, true);
    }

    public static void sendMixtureOfValidAndInvalidMessagesAsynchronouslyNew(final SendingActions sendingActions,
                                                                             final TestEnvironment testEnvironment,
                                                                             final int numberOfSender,
                                                                             final int numberOfMessagesPerSender) {
        final TestMessageFactory messageFactory = testMessageFactoryForRandomValidOrInvalidTestMessages(numberOfMessagesPerSender,
                testEnvironment);

        final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
        sendXMessagesAsynchronously(numberOfSender, messageFactory, eventType, sendingActions, testEnvironment, true);
    }


    public static void sendMessagesBeforeAndAfterShutdownAsynchronously(
            final BiConsumer<EventType, Subscriber<Object>> subscriberConsumer,
            final BiConsumer<EventType, Object> sendConsumer,
            final Consumer<Boolean> closeConsumer,
            final TestEnvironment testEnvironment,
            final int numberOfMessagesBeforeShutdown,
            final int numberOfMessagesAfterShutdown,
            final boolean finishRemainingTask) {
        final Semaphore semaphore = new Semaphore(0);
        final TestSubscriber<Object> subscriber = blockingTestSubscriber(semaphore);

        final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
        subscriberConsumer.accept(eventType, subscriber);
        testEnvironment.setProperty(SINGLE_RECEIVER, subscriber);

        final TestMessageFactory messageFactory = testMessageFactoryForValidMessages(1, testEnvironment);
        sendXMessagesAsynchronously(numberOfMessagesBeforeShutdown, messageFactory, eventType, sendConsumer::accept, testEnvironment, false);
        try {
            MILLISECONDS.sleep(100);
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }
        closeConsumer.accept(finishRemainingTask);
        semaphore.release(1000);
        for (int i = 0; i < numberOfMessagesAfterShutdown; i++) {
            final TestMessageOfInterest message = messageOfInterest();
            testEnvironment.addToListProperty(MESSAGES_SEND, message);
            sendConsumer.accept(eventType, message);
        }
    }

    public static void sendMessagesBeforeShutdownAsynchronouslyClassBased(
            final BiConsumer<Class<TestMessageOfInterest>, Subscriber<TestMessageOfInterest>> subscriberConsumer,
            final Consumer<TestMessage> sendConsumer,
            final Consumer<Boolean> closeConsumer,
            final TestEnvironment testEnvironment,
            final int numberOfSenders,
            final int numberOfMessages) {
        final Semaphore semaphore = new Semaphore(0);
        testEnvironment.setProperty(EXECUTION_END_SEMAPHORE, semaphore);
        final BlockingTestSubscriber<TestMessageOfInterest> subscriber = blockingTestSubscriber(semaphore);
        subscriberConsumer.accept(TestMessageOfInterest.class, subscriber);
        testEnvironment.setProperty(SINGLE_RECEIVER, subscriber);

        final ExecutorService executorService = Executors.newFixedThreadPool(numberOfSenders);
        for (int i = 0; i < numberOfSenders; i++) {
            executorService.execute(() -> {
                for (int j = 0; j < numberOfMessages; j++) {
                    final TestMessageOfInterest message = messageOfInterest();
                    testEnvironment.addToListProperty(MESSAGES_SEND, message);
                    sendConsumer.accept(message);
                }
            });
        }
        try {
            MILLISECONDS.sleep(20);
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }
        closeConsumer.accept(false);
        semaphore.release(1337);
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
        //TODO: nutze überall anders und checke if PollUntil(MESSAGE.SEND.size()) ersetzbar
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

        final ExecutorService executorService = Executors.newFixedThreadPool(numberOfSenders);
        for (int i = 0; i < numberOfSenders; i++) {
            executorService.execute(() -> {
                for (int j = 0; j < numberOfMessagesPerSender; j++) {
                    final TestMessageOfInterest message = messageOfInterest();
                    testEnvironment.addToListProperty(MESSAGES_SEND, message);
                    System.out.println("message send = ");
                    sutActions.send(eventType, message);
                }
            });
        }
        PollingUtils.pollUntilEquals(() -> {
            final int blockedThreads = subscriber.getBlockedThreads();
            System.out.println("blockedThreads = " + blockedThreads + ", expected: " + numberOfSenders);
            return blockedThreads;
        }, expectedNumberOfBlockedThreads);
    }

}
