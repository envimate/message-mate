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

package com.envimate.messageMate.pipe.givenWhenThen;


import com.envimate.messageMate.pipe.Pipe;
import com.envimate.messageMate.qcec.shared.TestAction;
import com.envimate.messageMate.qcec.shared.TestEnvironmentProperty;
import com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeMessageBusSutActions;
import com.envimate.messageMate.shared.subscriber.TestSubscriber;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import com.envimate.messageMate.shared.testMessages.TestMessageOfInterest;
import com.envimate.messageMate.subscribing.Subscriber;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.envimate.messageMate.pipe.givenWhenThen.PipeTestActions.pipeTestActions;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.RESULT;
import static com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeMessageBusSetupActions.addAnExceptionThrowingSubscriber;
import static com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeMessageBusTestActions.*;
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

    public static PipeActionBuilder severalMessagesAreSendAsynchronously(final int numberOfSender, final int numberOfMessagesPerSender) {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = pipeTestActions(pipe);
            sendSeveralMessagesInTheirOwnThread(sutActions, testEnvironment, numberOfSender, numberOfMessagesPerSender, true);
            return null;
        });
    }

    public static PipeActionBuilder aMessageResultingInAnErrorIsSend() {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = pipeTestActions(pipe);
            final TestSubscriber<TestMessageOfInterest> subscriber = addAnExceptionThrowingSubscriber(sutActions, testEnvironment);
            testEnvironment.addToListProperty(TestEnvironmentProperty.EXPECTED_RECEIVERS, subscriber);
            sendASingleMessage(sutActions, testEnvironment);
            return null;
        });
    }

    public static PipeActionBuilder severalMessagesAreSendAsynchronouslyButWillBeBlocked(final int numberOfSender, final int numberOfMessagesPerSender) {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = pipeTestActions(pipe);
            sendSeveralMessagesInTheirOwnThread(sutActions, testEnvironment, numberOfSender, numberOfMessagesPerSender, false);
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
            pipeTestActions(pipe)
                    .queryTheNumberOfAcceptedMessages(testEnvironment);
            return null;
        });
    }

    public static PipeActionBuilder theNumberOfAcceptedMessagesIsQueriedAsynchronously() {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            pipeTestActions(pipe)
                    .queryTheNumberOfAcceptedMessagesAsynchronously(testEnvironment);
            return null;
        });
    }

    public static PipeActionBuilder theNumberOfQueuedMessagesIsQueried() {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            pipeTestActions(pipe)
                    .queryTheNumberOfQueuedMessages(testEnvironment);
            return null;
        });
    }

    public static PipeActionBuilder theNumberOfSuccessfulMessagesIsQueried() {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            pipeTestActions(pipe)
                    .queryTheNumberOfSuccessfulDeliveredMessages(testEnvironment);
            return null;
        });
    }

    public static PipeActionBuilder theNumberOfFailedMessagesIsQueried() {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            pipeTestActions(pipe)
                    .queryTheNumberOfFailedDeliveredMessages(testEnvironment);
            return null;
        });
    }

    public static PipeActionBuilder theTimestampOfTheStatisticsIsQueried() {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            pipeTestActions(pipe)
                    .queryTheTimestampOfTheMessageStatistics(testEnvironment);
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

    public static PipeActionBuilder severalMessagesAreSendAsynchronouslyBeforeThePipeIsShutdown(final int numberOfSenders, final int numberOfMessages) {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = pipeTestActions(pipe);
            sendSeveralMessagesAsynchronouslyBeforeTheObjectIsShutdown(sutActions, testEnvironment, numberOfSenders, numberOfMessages);
            return null;
        });
    }

    public static PipeActionBuilder thePipeIsShutdownAsynchronouslyXTimes(final int numberOfThreads) {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = pipeTestActions(pipe);
            shutdownTheObjectAsynchronouslyXTimes(sutActions, numberOfThreads);
            return null;
        });
    }

    public static PipeActionBuilder thePipeIsShutdown() {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = pipeTestActions(pipe);
            shutdownTheSut(sutActions);
            return null;
        });
    }

    public static PipeActionBuilder thePipeIsShutdownAfterHalfOfTheMessagesWereDelivered(final int numberOfMessages) {
        final int numberOfMessagesBeforeShutdown = numberOfMessages / 2;
        final int remainingMessages = numberOfMessages - numberOfMessagesBeforeShutdown;
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = pipeTestActions(pipe);
            sendXMessagesAShutdownsIsCalledThenSendsYMessage(sutActions, testEnvironment, numberOfMessagesBeforeShutdown, remainingMessages, true);
            return null;
        });
    }

    public static PipeActionBuilder thePipeIsShutdownAfterHalfOfTheMessagesWereDelivered_withoutFinishingRemainingTasks(final int numberOfMessages) {
        final int numberOfMessagesBeforeShutdown = numberOfMessages / 2;
        final int remainingMessages = numberOfMessages - numberOfMessagesBeforeShutdown;
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = pipeTestActions(pipe);
            sendXMessagesAShutdownsIsCalledThenSendsYMessage(sutActions, testEnvironment, numberOfMessagesBeforeShutdown, remainingMessages, false);
            return null;
        });
    }

    public static PipeActionBuilder messagesAreSendAfterTheShutdown() {
        final int numberOfMessagesBeforeShutdown = 0;
        final int messagesSendAfterShutdown = 3;
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = pipeTestActions(pipe);
            sendXMessagesAShutdownsIsCalledThenSendsYMessage(sutActions, testEnvironment, numberOfMessagesBeforeShutdown, messagesSendAfterShutdown, true);
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
            final PipeMessageBusSutActions sutActions = pipeTestActions(pipe);
            callAwaitWithoutACloseIsCalled(sutActions, testEnvironment);
            return null;
        });
    }

    public static PipeActionBuilder awaitIsCalledBeforeAllTasksAreFinished(final int numberOfMessagesSend) {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = pipeTestActions(pipe);
            callCloseThenAwaitWithBlockedSubscriberButReleaseLockAfterAwait(sutActions, testEnvironment, numberOfMessagesSend);
            return null;
        });
    }

    public static PipeActionBuilder awaitIsCalledWithoutExpectingTasksToFinish(final int numberOfMessagesSend) {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = pipeTestActions(pipe);
            callCloseThenAwaitWithBlockedSubscriberWithoutReleasingLock(sutActions, testEnvironment, numberOfMessagesSend);
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
