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
import com.envimate.messageMate.internal.pipe.statistics.PipeStatistics;
import com.envimate.messageMate.shared.environment.TestEnvironmentProperty;
import com.envimate.messageMate.shared.givenWhenThen.TestAction;
import com.envimate.messageMate.shared.pipeChannelMessageBus.testActions.PipeSutActions;
import com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeMessageBusSutActions;
import com.envimate.messageMate.shared.utils.ShutdownTestUtils;
import com.envimate.messageMate.shared.polling.PollingUtils;
import com.envimate.messageMate.shared.subscriber.TestSubscriber;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import com.envimate.messageMate.shared.testMessages.TestMessageOfInterest;
import com.envimate.messageMate.subscribing.Subscriber;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.envimate.messageMate.internal.pipe.config.PipeTestConfig.ASYNCHRONOUS_POOL_SIZE;
import static com.envimate.messageMate.internal.pipe.givenWhenThen.PipeTestActions.pipeTestActions;
import static com.envimate.messageMate.shared.environment.TestEnvironmentProperty.RESULT;
import static com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeChannelMessageBusSharedTestProperties.MESSAGES_SEND;
import static com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeMessageBusSetupActions.addAnExceptionThrowingSubscriber;
import static com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeMessageBusTestActions.*;
import static com.envimate.messageMate.shared.utils.ShutdownTestUtils.*;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class PipeActionBuilder {
    private List<TestAction<Pipe<TestMessage>>> actions = new ArrayList<>();

    private PipeActionBuilder(final TestAction<Pipe<TestMessage>> action) {
        this.actions.add(action);
    }

    public static PipeActionBuilder aSingleMessageIsSend() {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = pipeTestActions(pipe);
            sendASingleMessage(sutActions, testEnvironment);
            return null;
        });
    }

    public static PipeActionBuilder severalMessagesAreSend(final int numberOfMessages) {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = pipeTestActions(pipe);
            sendSeveralMessages(sutActions, testEnvironment, numberOfMessages);
            return null;
        });
    }

    public static PipeActionBuilder severalMessagesAreSendAsynchronously(final int numberOfSender,
                                                                         final int numberOfMessagesPerSender) {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = pipeTestActions(pipe);
            sendSeveralMessagesInTheirOwnThread(sutActions, testEnvironment, numberOfSender, numberOfMessagesPerSender, true);
            return null;
        });
    }

    public static PipeActionBuilder aMessageResultingInAnErrorIsSend() {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeMessageBusSutActions sutAction = pipeTestActions(pipe);
            final TestSubscriber<TestMessageOfInterest> subscriber = addAnExceptionThrowingSubscriber(sutAction, testEnvironment);
            testEnvironment.addToListProperty(TestEnvironmentProperty.EXPECTED_RECEIVERS, subscriber);
            sendASingleMessage(sutAction, testEnvironment);
            return null;
        });
    }

    public static PipeActionBuilder severalMessagesAreSendAsynchronouslyButWillBeBlocked(final int numberOfMessages) {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeSutActions testActions = PipeSutActions.pipeSutActions(pipe);
            sendSeveralMessagesInTheirOwnThreadThatWillBeBlocked(testActions, testEnvironment, numberOfMessages);
            return null;
        });
    }

    public static PipeActionBuilder severalMessagesAreSendAsynchronouslyButWillBeBlocked(final int numberOfSender,
                                                                                         final int numberOfMessagesPerSender) {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeSutActions testActions = PipeSutActions.pipeSutActions(pipe);
            sendSeveralMessagesInTheirOwnThreadThatWillBeBlocked(testActions, testEnvironment, numberOfSender, numberOfMessagesPerSender, ASYNCHRONOUS_POOL_SIZE);
            return null;
        });
    }

    public static PipeActionBuilder oneSubscriberUnsubscribesSeveralTimes(final int numberOfUnsubscriptions) {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = pipeTestActions(pipe);
            unsubscribeASubscriberXTimes(sutActions, testEnvironment, numberOfUnsubscriptions);
            return null;
        });
    }

    public static PipeActionBuilder oneSubscriberUnsubscribes() {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = pipeTestActions(pipe);
            unsubscribeASubscriberXTimes(sutActions, testEnvironment, 1);
            return null;
        });
    }

    public static PipeActionBuilder theNumberOfAcceptedMessagesIsQueried() {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final List<?> sendMessages = testEnvironment.getPropertyAsType(MESSAGES_SEND, List.class);
            final PipeTestActions pipeTestActions = pipeTestActions(pipe);
            PollingUtils.pollUntilEquals(() -> pipeTestActions.getMessageStatistics(PipeStatistics::getAcceptedMessages), sendMessages.size());
            pipeTestActions.queryTheNumberOfAcceptedMessages(testEnvironment);
            return null;
        });
    }

    public static PipeActionBuilder theNumberOfAcceptedMessagesIsQueriedAsynchronously() {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            pipeTestActions(pipe).queryTheNumberOfAcceptedMessagesAsynchronously(testEnvironment);
            return null;
        });
    }

    public static PipeActionBuilder theNumberOfQueuedMessagesIsQueried() {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            pipeTestActions(pipe).queryTheNumberOfQueuedMessages(testEnvironment);
            return null;
        });
    }

    public static PipeActionBuilder theNumberOfSuccessfulMessagesIsQueried() {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final List<?> sendMessages = testEnvironment.getPropertyAsType(MESSAGES_SEND, List.class);
            final PipeTestActions pipeTestActions = pipeTestActions(pipe);
            PollingUtils.pollUntilEquals(() -> pipeTestActions.getMessageStatistics(PipeStatistics::getAcceptedMessages), sendMessages.size());
            pipeTestActions.queryTheNumberOfSuccessfulDeliveredMessages(testEnvironment);
            return null;
        });
    }

    public static PipeActionBuilder theNumberOfFailedMessagesIsQueried() {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final List<?> sendMessages = testEnvironment.getPropertyAsType(MESSAGES_SEND, List.class);
            final PipeTestActions pipeTestActions = pipeTestActions(pipe);
            PollingUtils.pollUntilEquals(() -> pipeTestActions.getMessageStatistics(PipeStatistics::getFailedMessages), sendMessages.size());
            pipeTestActions.queryTheNumberOfFailedDeliveredMessages(testEnvironment);
            return null;
        });
    }

    public static PipeActionBuilder theNumberOfFailedMessagesIsQueried(final int expectedResultToPollFor) {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeTestActions pipeTestActions = pipeTestActions(pipe);
            PollingUtils.pollUntilEquals(() -> pipeTestActions.getMessageStatistics(PipeStatistics::getFailedMessages), expectedResultToPollFor);
            pipeTestActions.queryTheNumberOfFailedDeliveredMessages(testEnvironment);
            return null;
        });
    }

    public static PipeActionBuilder theTimestampOfTheStatisticsIsQueried() {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            pipeTestActions(pipe).queryTheTimestampOfTheMessageStatistics(testEnvironment);
            return null;
        });
    }

    public static PipeActionBuilder theListOfSubscriberIsQueried() {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = pipeTestActions(pipe);
            final List<Subscriber<?>> subscribers = sutActions.getAllSubscribers();
            testEnvironment.setProperty(RESULT, subscribers);
            return null;
        });
    }

    public static PipeActionBuilder aShortWaitIsDone(final long timeout, final TimeUnit timeUnit) {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            performAShortWait(timeout, timeUnit);
            return null;
        });
    }

    public static PipeActionBuilder severalMessagesAreSendAsynchronouslyBeforeThePipeIsShutdown() {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeSutActions testActions = PipeSutActions.pipeSutActions(pipe);
            sendMessagesBeforeShutdownAsynchronously(testActions, testEnvironment, ASYNCHRONOUS_POOL_SIZE, true);
            return null;
        });
    }

    public static PipeActionBuilder thePipeIsShutdownAsynchronouslyXTimes(final int numberOfThreads) {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeSutActions testActions = PipeSutActions.pipeSutActions(pipe);
            shutdownTheSutAsynchronouslyXTimes(testActions, numberOfThreads);
            return null;
        });
    }

    public static PipeActionBuilder thePipeIsShutdown() {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeSutActions testActions = PipeSutActions.pipeSutActions(pipe);
            ShutdownTestUtils.shutdownTheSut(testActions);
            return null;
        });
    }

    public static PipeActionBuilder thePipeIsShutdownAfterHalfOfTheMessagesWereDelivered(final int numberOfMessages) {
        final int numberOfMessagesBeforeShutdown = numberOfMessages / 2;
        final int remainingMessages = numberOfMessages - numberOfMessagesBeforeShutdown;
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeSutActions testActions = PipeSutActions.pipeSutActions(pipe);
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
            final PipeSutActions testActions = PipeSutActions.pipeSutActions(pipe);
            sendMessagesBeforeAndAfterShutdownAsynchronously(testActions, testEnvironment, numberOfMessagesBeforeShutdown,
                    remainingMessages, false);
            return null;
        });
    }

    public static PipeActionBuilder aMessageIsSendAfterTheShutdown() {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeSutActions testActions = PipeSutActions.pipeSutActions(pipe);
            shutDownTheSutThenSendAMessage(testActions, testEnvironment);
            return null;
        });
    }

    public static PipeActionBuilder thePipeShutdownIsExpectedForTimeoutInSeconds(final int timeoutInSeconds) {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = pipeTestActions(pipe);
            awaitTheShutdownTimeoutInSeconds(sutActions, testEnvironment, timeoutInSeconds);
            return null;
        });
    }

    public static PipeActionBuilder awaitWithoutACloseIsCalled() {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeSutActions testActions = PipeSutActions.pipeSutActions(pipe);
            ShutdownTestUtils.callAwaitWithoutACloseIsCalled(testActions, testEnvironment);
            return null;
        });
    }

    public static PipeActionBuilder closeAndThenWaitForPendingTasksToFinished(final int numberOfMessagesSend) {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeSutActions testActions = PipeSutActions.pipeSutActions(pipe);
            ShutdownTestUtils.closeAndThenWaitForPendingTasksToFinished(testActions, numberOfMessagesSend, testEnvironment);
            return null;
        });
    }

    public static PipeActionBuilder awaitIsCalledWithoutAllowingRemainingTasksToFinish(final int numberOfMessagesSend) {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeSutActions testActions = PipeSutActions.pipeSutActions(pipe);
            callCloseThenAwaitWithBlockedSubscriberWithoutReleasingLock(testActions, testEnvironment, numberOfMessagesSend, ASYNCHRONOUS_POOL_SIZE);
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
