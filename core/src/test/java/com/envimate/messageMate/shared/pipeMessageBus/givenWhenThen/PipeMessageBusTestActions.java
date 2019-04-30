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

package com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen;

import com.envimate.messageMate.identification.MessageId;
import com.envimate.messageMate.shared.environment.TestEnvironment;
import com.envimate.messageMate.shared.pipeChannelMessageBus.testActions.SendingActions;
import com.envimate.messageMate.shared.pipeChannelMessageBus.testActions.SendingAndReceivingActions;
import com.envimate.messageMate.shared.pipeChannelMessageBus.testActions.SubscribeActions;
import com.envimate.messageMate.shared.subscriber.BlockingTestSubscriber;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import com.envimate.messageMate.shared.testMessages.TestMessageOfInterest;
import com.envimate.messageMate.shared.utils.ShutdownTestUtils;
import com.envimate.messageMate.subscribing.Subscriber;
import com.envimate.messageMate.subscribing.SubscriptionId;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static com.envimate.messageMate.shared.environment.TestEnvironmentProperty.RESULT;
import static com.envimate.messageMate.shared.utils.AsynchronousSendingTestUtils.addABlockingSubscriberAndThenSendXMessagesInEachThread;
import static com.envimate.messageMate.shared.utils.AsynchronousSendingTestUtils.sendValidMessagesAsynchronously;
import static com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeChannelMessageBusSharedTestProperties.*;
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
        testEnvironment.setProperty(MESSAGES_SEND, messages);
    }

    public static void sendSeveralMessagesInTheirOwnThread(final PipeMessageBusSutActions sutActions,
                                                           final TestEnvironment testEnvironment,
                                                           final int numberOfSender,
                                                           final int numberOfMessagesPerSender,
                                                           final boolean expectCleanShutdown) {
        sendValidMessagesAsynchronously(sutActions, testEnvironment, numberOfSender,
                numberOfMessagesPerSender, expectCleanShutdown);
    }

    public static <T extends SendingActions & SubscribeActions> void sendSeveralMessagesInTheirOwnThreadThatWillBeBlocked(
            final T sutActions,
            final TestEnvironment testEnvironment,
            final int numberOfMessages) {
        final Semaphore semaphore = addABlockingSubscriberAndThenSendXMessagesInEachThread(sutActions, numberOfMessages, testEnvironment);
        testEnvironment.setPropertyIfNotSet(EXECUTION_END_SEMAPHORE, semaphore);
    }

    public static <T extends SendingActions & SubscribeActions> void sendSeveralMessagesInTheirOwnThreadThatWillBeBlocked(
            final T sutActions,
            final TestEnvironment testEnvironment,
            final int numberOfSenders,
            final int numberOfMessagesPerSender,
            final int expectedNumberOfBlockedThreads) {
        final Semaphore semaphore = new Semaphore(0);
        final BlockingTestSubscriber<TestMessage> subscriber = BlockingTestSubscriber.blockingTestSubscriber(semaphore);
        addABlockingSubscriberAndThenSendXMessagesInEachThread(sutActions, subscriber, numberOfSenders, numberOfMessagesPerSender, testEnvironment, expectedNumberOfBlockedThreads);
        testEnvironment.setPropertyIfNotSet(EXECUTION_END_SEMAPHORE, semaphore);
    }

    public static void sendXMessagesAShutdownsIsCalledThenSendsYMessage(final SendingAndReceivingActions sutActions,
                                                                        final TestEnvironment testEnvironment,
                                                                        final int numberOfMessagesBeforeShutdown,
                                                                        final int numberOfMessagesAfterShutdown,
                                                                        final boolean finishRemainingTask) {
        ShutdownTestUtils.sendMessagesBeforeAndAfterShutdownAsynchronously(sutActions, testEnvironment, numberOfMessagesBeforeShutdown,
                numberOfMessagesAfterShutdown, finishRemainingTask);
    }

    //TODO: all shutdowns should be moved to ShutdownTestUtils
    public static void shutdownTheSut(final PipeMessageBusSutActions sutActions, final boolean finishRemainingTasks) {
        sutActions.close(finishRemainingTasks);
    }

    public static void shutdownTheObjectAsynchronouslyXTimes(final PipeMessageBusSutActions sutActions,
                                                             final int numberOfThreads) {
        final ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        for (int i = 0; i < numberOfThreads; i++) {
            executorService.execute(() -> sutActions.close(false));
        }
        executorService.shutdown();
        try {
            final boolean terminationSuccessful = executorService.awaitTermination(1, SECONDS);
            if (!terminationSuccessful) {
                throw new RuntimeException("Executor service did not shutdown in a clean way.");
            }
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
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
