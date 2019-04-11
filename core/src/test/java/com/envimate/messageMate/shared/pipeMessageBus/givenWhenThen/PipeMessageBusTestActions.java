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

package com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen;

import com.envimate.messageMate.identification.MessageId;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.shared.subscriber.BlockingTestSubscriber;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import com.envimate.messageMate.shared.testMessages.TestMessageOfInterest;
import com.envimate.messageMate.subscribing.Subscriber;
import com.envimate.messageMate.subscribing.SubscriptionId;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.EXPECTED_RESULT;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.RESULT;
import static com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.AsynchronousSendingTestUtils.*;
import static com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeChannelMessageBusSharedTestProperties.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class PipeMessageBusTestActions {

    public static MessageId sendASingleMessage(final PipeMessageBusSutActions sutActions,
                                               final TestEnvironment testEnvironment) {
        final TestMessageOfInterest message = TestMessageOfInterest.messageOfInterest();
        testEnvironment.setProperty(SINGLE_SEND_MESSAGE, message);
        return sutActions.send(message);
    }


    public static void sendSeveralMessages(final PipeMessageBusSutActions sutActions,
                                           final TestEnvironment testEnvironment,
                                           final int numberOfMessages) {
        final List<TestMessageOfInterest> messages = new LinkedList<>();
        for (int i = 0; i < numberOfMessages; i++) {
            final TestMessageOfInterest message = TestMessageOfInterest.messageOfInterest();
            sutActions.send(message);
            messages.add(message);
        }
        testEnvironment.setProperty(MESSAGES_SEND_OF_INTEREST, messages);
    }

    public static void sendSeveralMessagesInTheirOwnThread(final PipeMessageBusSutActions sutActions,
                                                           final TestEnvironment testEnvironment,
                                                           final int numberOfSender,
                                                           final int numberOfMessagesPerSender,
                                                           final boolean expectCleanShutdown) {
        sendValidMessagesAsynchronously(sutActions, testEnvironment, numberOfSender,
                numberOfMessagesPerSender, expectCleanShutdown);
    }

    public static void sendXMessagesAShutdownsIsCalledThenSendsYMessage(final PipeMessageBusSutActions sutActions,
                                                                        final TestEnvironment testEnvironment,
                                                                        final int numberOfMessagesBeforeShutdown,
                                                                        final int numberOfMessagesAfterShutdown,
                                                                        final boolean finishRemainingTask) {
        sendMessagesBeforeAndAfterShutdownAsynchronously(sutActions, testEnvironment, numberOfMessagesBeforeShutdown,
                numberOfMessagesAfterShutdown, finishRemainingTask);
    }

    public static void sendSeveralMessagesAsynchronouslyBeforeTheObjectIsShutdown(final PipeMessageBusSutActions sutActions,
                                                                                  final TestEnvironment testEnvironment,
                                                                                  final int numberOfSenders,
                                                                                  final int numberOfMessages) {
        sendMessagesBeforeShutdownAsynchronouslyClassBased(sutActions::subscribe, sutActions::send, sutActions::close,
                testEnvironment, numberOfSenders, numberOfMessages);
    }

    public static void shutdownTheSut(final PipeMessageBusSutActions sutActions) {
        shutdownTheSut(sutActions, true);
    }

    public static void shutdownTheSut(final PipeMessageBusSutActions sutActions, final boolean finishRemainingTasks) {
        sutActions.close(finishRemainingTasks);
    }


    public static void shutdownTheObjectAsynchronouslyXTimes(final PipeMessageBusSutActions sutActions,
                                                             final int numberOfThreads) {
        final ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        for (int i = 0; i < numberOfThreads; i++) {
            executorService.execute(() -> sutActions.close(false));
        }
    }

    public static void awaitTheShutdownTimeoutInSeconds(final PipeMessageBusSutActions sutActions,
                                                        final TestEnvironment testEnvironment,
                                                        final int timeoutInSeconds) {
        try {
            final boolean terminatedSuccessful = sutActions.awaitTermination(timeoutInSeconds, SECONDS);
            testEnvironment.setProperty(RESULT, terminatedSuccessful);
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void callAwaitWithoutACloseIsCalled(final PipeMessageBusSutActions sutActions,
                                                      final TestEnvironment testEnvironment) {
        try {
            final boolean terminatedSuccessful = sutActions.awaitTermination(0, SECONDS);
            testEnvironment.setProperty(RESULT, terminatedSuccessful);
            testEnvironment.setProperty(EXPECTED_RESULT, false);
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void callCloseThenAwaitWithBlockedSubscriberButReleaseLockAfterAwait(final PipeMessageBusSutActions sutActions,
                                                                                       final TestEnvironment testEnvironment,
                                                                                       final int numberOfMessagesSend) {
        try {
            final Semaphore semaphore = new Semaphore(0);
            final BlockingTestSubscriber<TestMessage> testSubscriber = BlockingTestSubscriber.blockingTestSubscriber(semaphore);
            sutActions.subscribe(TestMessage.class, testSubscriber);
            sendSeveralMessagesInTheirOwnThread(sutActions, testEnvironment, numberOfMessagesSend, 1, false);
            MILLISECONDS.sleep(10);
            new Thread(() -> {
                try {
                    MILLISECONDS.sleep(10);
                    semaphore.release(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
            sutActions.close(true);
            final boolean terminatedSuccessful = sutActions.awaitTermination(15, MILLISECONDS);
            MILLISECONDS.sleep(10); //Second sleep necessary for synchronous config
            testEnvironment.setProperty(RESULT, terminatedSuccessful);
            testEnvironment.setProperty(SINGLE_RECEIVER, testSubscriber);
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void callCloseThenAwaitWithBlockedSubscriberWithoutReleasingLock(final PipeMessageBusSutActions sutActions,
                                                                                   final TestEnvironment testEnvironment,
                                                                                   final int numberOfMessagesSend) {
        try {
            final Semaphore semaphore = new Semaphore(0);
            final BlockingTestSubscriber<TestMessage> testSubscriber = BlockingTestSubscriber.blockingTestSubscriber(semaphore);
            sutActions.subscribe(TestMessage.class, testSubscriber);
            sendSeveralMessagesInTheirOwnThread(sutActions, testEnvironment, numberOfMessagesSend, 1, false);
            MILLISECONDS.sleep(10);
            sutActions.close(true);
            final boolean terminatedSuccessful = sutActions.awaitTermination(15, MILLISECONDS);
            MILLISECONDS.sleep(10); //Second sleep necessary for synchronous config
            testEnvironment.setProperty(RESULT, terminatedSuccessful);
            testEnvironment.setProperty(SINGLE_RECEIVER, testSubscriber);
            testEnvironment.setProperty(EXECUTION_END_SEMAPHORE, semaphore);
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static void unsubscribeASubscriberXTimes(final PipeMessageBusSutActions sutActions,
                                                    final TestEnvironment testEnvironment,
                                                    final int numberOfUnsubscriptions) {
        final List<Subscriber<?>> currentSubscriber;
        if (testEnvironment.has(EXPECTED_SUBSCRIBER)) {
            currentSubscriber = (List<Subscriber<?>>) testEnvironment.getProperty(EXPECTED_SUBSCRIBER);
        } else {
            currentSubscriber = (List<Subscriber<?>>) testEnvironment.getProperty(INITIAL_SUBSCRIBER);
        }
        final Subscriber<?> firstSubscriber = currentSubscriber.get(0);
        final SubscriptionId subscriptionId = firstSubscriber.getSubscriptionId();
        for (int i = 0; i < numberOfUnsubscriptions; i++) {
            sutActions.unsubscribe(subscriptionId);
        }
        final List<Subscriber<?>> remainingSubscriber = currentSubscriber.subList(1, currentSubscriber.size());
        testEnvironment.setProperty(EXPECTED_SUBSCRIBER, remainingSubscriber);
    }

    public static void performAShortWait(final long timeout, final TimeUnit timeUnit) {
        try {
            timeUnit.sleep(timeout);
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void queryTheListOfFilters(final PipeMessageBusSutActions sutActions,
                                             final TestEnvironment testEnvironment) {
        final List<?> filter = sutActions.getFilter();
        testEnvironment.setProperty(RESULT, filter);
    }

    public static void removeAFilter(final PipeMessageBusSutActions sutActions,
                                     final TestEnvironment testEnvironment) {
        final Object removedFilter = sutActions.removeAFilter();
        final List<?> expectedFilter = testEnvironment.getPropertyAsType(EXPECTED_FILTER, List.class);
        expectedFilter.remove(removedFilter);
    }
}
