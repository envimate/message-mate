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
import com.envimate.messageMate.shared.environment.TestEnvironment;
import com.envimate.messageMate.shared.givenWhenThen.TestAction;
import com.envimate.messageMate.shared.subscriber.BlockingTestSubscriber;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import com.envimate.messageMate.shared.utils.ShutdownTestUtils;
import com.envimate.messageMate.subscribing.Subscriber;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Semaphore;

import static com.envimate.messageMate.internal.pipe.config.PipeTestConfig.ASYNCHRONOUS_PIPE_POOL_SIZE;
import static com.envimate.messageMate.internal.pipe.givenWhenThen.PipeTestActions.pipeTestActions;
import static com.envimate.messageMate.shared.environment.TestEnvironmentProperty.RESULT;
import static com.envimate.messageMate.shared.properties.SharedTestProperties.*;
import static com.envimate.messageMate.shared.polling.PollingUtils.pollUntilEquals;
import static com.envimate.messageMate.shared.subscriber.BlockingTestSubscriber.blockingTestSubscriber;
import static com.envimate.messageMate.shared.utils.SendingTestUtils.*;
import static com.envimate.messageMate.shared.utils.ShutdownTestUtils.*;
import static com.envimate.messageMate.shared.utils.SubscriptionTestUtils.addAnExceptionThrowingSubscriber;
import static com.envimate.messageMate.shared.utils.SubscriptionTestUtils.unsubscribeASubscriberXTimes;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class PipeActionBuilder {
    private List<TestAction<Pipe<TestMessage>>> actions = new ArrayList<>();

    private PipeActionBuilder(final TestAction<Pipe<TestMessage>> action) {
        this.actions.add(action);
    }

    public static PipeActionBuilder aSingleMessageIsSend() {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeTestActions testActions = pipeTestActions(pipe);
            sendSingleMessage(testActions, testEnvironment);
            return null;
        });
    }

    public static PipeActionBuilder severalMessagesAreSend(final int numberOfMessages) {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeTestActions testActions = pipeTestActions(pipe);
            sendSeveralMessages(testActions, numberOfMessages, testEnvironment);
            return null;
        });
    }

    public static PipeActionBuilder severalMessagesAreSendAsynchronously(final int numberOfSender,
                                                                         final int numberOfMessagesPerSender) {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeTestActions testActions = pipeTestActions(pipe);
            sendValidMessagesAsynchronouslyNew(testActions, testEnvironment, numberOfSender, numberOfMessagesPerSender, true);
            return null;
        });
    }

    public static PipeActionBuilder aMessageResultingInAnErrorIsSend() {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeTestActions testActions = pipeTestActions(pipe);
            addAnExceptionThrowingSubscriber(testActions, testEnvironment);
            sendSingleMessage(testActions, testEnvironment);
            return null;
        });
    }

    public static PipeActionBuilder severalMessagesAreSendAsynchronouslyButWillBeBlocked(final int numberOfMessages) {
        return severalMessagesAreSendAsynchronouslyButWillBeBlocked(numberOfMessages, 1);
    }

    public static PipeActionBuilder severalMessagesAreSendAsynchronouslyButWillBeBlocked(final int numberOfSender,
                                                                                         final int numberOfMessagesPerSender) {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeTestActions testActions = pipeTestActions(pipe);
            final Semaphore semaphore = new Semaphore(0);
            final BlockingTestSubscriber<TestMessage> subscriber = blockingTestSubscriber(semaphore);
            final int expectedNumberOfBlockedThreads = determineExpectedNumberOfBlockedThreads(numberOfSender, testEnvironment);
            addABlockingSubscriberAndThenSendXMessagesInEachThread(testActions, subscriber, numberOfSender,
                    numberOfMessagesPerSender, testEnvironment, expectedNumberOfBlockedThreads);
            testEnvironment.setPropertyIfNotSet(EXECUTION_END_SEMAPHORE, semaphore);
            return null;
        });
    }

    private static int determineExpectedNumberOfBlockedThreads(final int numberOfMessages,
                                                               final TestEnvironment testEnvironment) {
        final int expectedBlockedThreads;
        if (testEnvironment.getPropertyAsType(IS_ASYNCHRONOUS, Boolean.class)) {
            expectedBlockedThreads = ASYNCHRONOUS_PIPE_POOL_SIZE;
        } else {
            expectedBlockedThreads = numberOfMessages;
        }
        return expectedBlockedThreads;
    }

    public static PipeActionBuilder oneSubscriberUnsubscribesSeveralTimes(final int numberOfUnsubscriptions) {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeTestActions testActions = pipeTestActions(pipe);
            unsubscribeASubscriberXTimes(testActions, testEnvironment, numberOfUnsubscriptions);
            return null;
        });
    }

    public static PipeActionBuilder oneSubscriberUnsubscribes() {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeTestActions testActions = pipeTestActions(pipe);
            unsubscribeASubscriberXTimes(testActions, testEnvironment, 1);
            return null;
        });
    }

    public static PipeActionBuilder theNumberOfAcceptedMessagesIsQueried() {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final List<?> sendMessages = testEnvironment.getPropertyAsType(MESSAGES_SEND, List.class);
            final PipeTestActions pipeTestActions = pipeTestActions(pipe);
            pollUntilEquals(pipeTestActions::getTheNumberOfAcceptedMessages, sendMessages.size());
            final long result = pipeTestActions.getTheNumberOfAcceptedMessages();
            testEnvironment.setProperty(RESULT, result);
            return null;
        });
    }

    public static PipeActionBuilder theNumberOfQueuedMessagesIsQueried() {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeTestActions pipeTestActions = pipeTestActions(pipe);
            final long result = pipeTestActions.getTheNumberOfQueuedMessages();
            testEnvironment.setProperty(RESULT, result);
            return null;
        });
    }

    public static PipeActionBuilder theNumberOfSuccessfulMessagesIsQueried() {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final List<?> sendMessages = testEnvironment.getPropertyAsType(MESSAGES_SEND, List.class);
            final PipeTestActions pipeTestActions = pipeTestActions(pipe);
            pollUntilEquals(pipeTestActions::getTheNumberOfSuccessfulDeliveredMessages, sendMessages.size());
            final long result = pipeTestActions.getTheNumberOfSuccessfulDeliveredMessages();
            testEnvironment.setProperty(RESULT, result);
            return null;
        });
    }

    public static PipeActionBuilder theNumberOfSuccessfulMessagesIsQueriedWhenSubscriberBlocked() {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeTestActions pipeTestActions = pipeTestActions(pipe);
            final long result = pipeTestActions.getTheNumberOfSuccessfulDeliveredMessages();
            testEnvironment.setProperty(RESULT, result);
            return null;
        });
    }

    public static PipeActionBuilder theNumberOfFailedMessagesIsQueried() {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final List<?> sendMessages = testEnvironment.getPropertyAsType(MESSAGES_SEND, List.class);
            final PipeTestActions pipeTestActions = pipeTestActions(pipe);
            pollUntilEquals(pipeTestActions::getTheNumberOfFailedDeliveredMessages, sendMessages.size());
            final long result = pipeTestActions.getTheNumberOfFailedDeliveredMessages();
            testEnvironment.setProperty(RESULT, result);
            return null;
        });
    }

    public static PipeActionBuilder theNumberOfFailedMessagesIsQueried(final int expectedResultToPollFor) {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeTestActions pipeTestActions = pipeTestActions(pipe);
            pollUntilEquals(pipeTestActions::getTheNumberOfFailedDeliveredMessages, expectedResultToPollFor);
            final long result = pipeTestActions.getTheNumberOfFailedDeliveredMessages();
            testEnvironment.setProperty(RESULT, result);
            return null;
        });
    }

    public static PipeActionBuilder theTimestampOfTheStatisticsIsQueried() {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeTestActions pipeTestActions = pipeTestActions(pipe);
            final Date result = pipeTestActions.getTheTimestampOfTheMessageStatistics();
            testEnvironment.setProperty(RESULT, result);
            return null;
        });
    }

    public static PipeActionBuilder theListOfSubscriberIsQueried() {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeTestActions testActions = pipeTestActions(pipe);
            final List<Subscriber<?>> subscribers = testActions.getAllSubscribers();
            testEnvironment.setProperty(RESULT, subscribers);
            return null;
        });
    }

    public static PipeActionBuilder severalMessagesAreSendAsynchronouslyBeforeThePipeIsShutdown() {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeTestActions testActions = pipeTestActions(pipe);
            sendMessagesBeforeShutdownAsynchronously(testActions, testEnvironment, ASYNCHRONOUS_PIPE_POOL_SIZE, true);
            return null;
        });
    }

    public static PipeActionBuilder thePipeIsShutdownAsynchronouslyXTimes(final int numberOfThreads) {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeTestActions testActions = pipeTestActions(pipe);
            shutdownTheSutAsynchronouslyXTimes(testActions, numberOfThreads);
            return null;
        });
    }

    public static PipeActionBuilder thePipeIsShutdown() {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeTestActions testActions = pipeTestActions(pipe);
            shutdownTheSut(testActions);
            return null;
        });
    }

    public static PipeActionBuilder thePipeIsShutdownAfterHalfOfTheMessagesWereDelivered(final int numberOfMessages) {
        final int numberOfMessagesBeforeShutdown = numberOfMessages / 2;
        final int remainingMessages = numberOfMessages - numberOfMessagesBeforeShutdown;
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeTestActions testActions = pipeTestActions(pipe);
            sendMessagesBeforeAndAfterShutdownAsynchronously(testActions, testEnvironment, numberOfMessagesBeforeShutdown,
                    remainingMessages, true);
            return null;
        });
    }

    public static PipeActionBuilder thePipeIsShutdownAfterHalfOfTheMessagesWereDelivered_withoutFinishingRemainingTasks(
            final int numberOfMessages) {
        final int numberOfMessagesBeforeShutdown = numberOfMessages / 2;
        final int remainingMessages = numberOfMessages - numberOfMessagesBeforeShutdown;
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeTestActions testActions = pipeTestActions(pipe);
            sendMessagesBeforeAndAfterShutdownAsynchronously(testActions, testEnvironment, numberOfMessagesBeforeShutdown,
                    remainingMessages, false);
            return null;
        });
    }

    public static PipeActionBuilder aMessageIsSendAfterTheShutdown() {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeTestActions testActions = pipeTestActions(pipe);
            shutDownTheSutThenSendAMessage(testActions, testEnvironment);
            return null;
        });
    }

    public static PipeActionBuilder thePipeShutdownIsExpectedForTimeoutInSeconds(final int timeoutInSeconds) {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeTestActions testActions = pipeTestActions(pipe);
            awaitTermination(testActions, testEnvironment, timeoutInSeconds);
            return null;
        });
    }

    public static PipeActionBuilder awaitWithoutACloseIsCalled() {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeTestActions testActions = pipeTestActions(pipe);
            callAwaitWithoutACloseIsCalled(testActions, testEnvironment);
            return null;
        });
    }

    public static PipeActionBuilder closeAndThenWaitForPendingTasksToFinished(final int numberOfMessagesSend) {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeTestActions testActions = pipeTestActions(pipe);
            ShutdownTestUtils.closeAndThenWaitForPendingTasksToFinished(testActions, numberOfMessagesSend, testEnvironment);
            return null;
        });
    }

    public static PipeActionBuilder awaitIsCalledWithoutAllowingRemainingTasksToFinish(final int numberOfMessagesSend) {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeTestActions testActions = pipeTestActions(pipe);
            callCloseThenAwaitWithBlockedSubscriberWithoutReleasingLock(testActions, testEnvironment, numberOfMessagesSend,
                    ASYNCHRONOUS_PIPE_POOL_SIZE);
            return null;
        });
    }

    public PipeActionBuilder andThen(final PipeActionBuilder followUpBuilder) {
        actions.addAll(followUpBuilder.actions);
        return this;
    }

    public List<TestAction<Pipe<TestMessage>>> build() {
        return actions;
    }

}
