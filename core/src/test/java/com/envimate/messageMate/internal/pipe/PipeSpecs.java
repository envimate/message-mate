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

package com.envimate.messageMate.internal.pipe;

import com.envimate.messageMate.exceptions.AlreadyClosedException;
import com.envimate.messageMate.internal.pipe.config.PipeTestConfig;
import com.envimate.messageMate.internal.pipe.givenWhenThen.PipeActionBuilder;
import org.junit.jupiter.api.Test;

import static com.envimate.messageMate.internal.pipe.givenWhenThen.Given.given;
import static com.envimate.messageMate.internal.pipe.givenWhenThen.PipeActionBuilder.*;
import static com.envimate.messageMate.internal.pipe.givenWhenThen.PipeSetupBuilder.aConfiguredPipe;
import static com.envimate.messageMate.internal.pipe.givenWhenThen.PipeValidationBuilder.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public interface PipeSpecs {

    //sending and subscribe
    @Test
    default void testPipe_canSendSingleMessageToOneReceiver(final PipeTestConfig testConfig) throws Exception {
        given(aConfiguredPipe(testConfig)
                .withASingleSubscriber())
                .when(aSingleMessageIsSend())
                .then(expectTheMessageToBeReceived());
    }

    @Test
    default void testPipe_canSendSeveralMessagesToSeveralSubscriber(final PipeTestConfig testConfig) throws Exception {
        given(aConfiguredPipe(testConfig)
                .withSeveralSubscriber(5))
                .when(severalMessagesAreSend(10))
                .then(expectAllMessagesToBeReceivedByAllSubscribers());
    }

    @Test
    default void testPipe_canSendMessagesAsynchronously(final PipeTestConfig testConfig) throws Exception {
        given(aConfiguredPipe(testConfig)
                .withSeveralSubscriber(8))
                .when(severalMessagesAreSendAsynchronously(10, 10))
                .then(expectAllMessagesToBeReceivedByAllSubscribers());
    }

    @Test
    default void testPipe_subscriberCanInterruptDeliveringMessage_whenDeliveryIsSynchronous(final PipeTestConfig testConfig) throws Exception {
        given(aConfiguredPipe(testConfig)
                .withSeveralDeliveryInterruptingSubscriber(5))
                .when(severalMessagesAreSend(10))
                .then(expectEachMessagesToBeReceivedByOnlyOneSubscriber());
    }

    @Test
    default void testPipe_canQueryAllSubscriber(final PipeTestConfig testConfig) throws Exception {
        given(aConfiguredPipe(testConfig)
                .withSeveralSubscriber(8))
                .when(theListOfSubscriberIsQueried())
                .then(expectTheListOfAllSubscriber());
    }

    //unsubscribe
    @Test
    default void testPipe_canUnsubscribe(final PipeTestConfig testConfig) throws Exception {
        given(aConfiguredPipe(testConfig)
                .withSeveralSubscriber(5))
                .when(oneSubscriberUnsubscribes())
                .then(expectAllRemainingSubscribersToStillBeSubscribed());
    }

    @Test
    default void testPipe_canUnsubscribeTwoSubscribers(final PipeTestConfig testConfig) throws Exception {
        given(aConfiguredPipe(testConfig)
                .withSeveralSubscriber(5))
                .when(oneSubscriberUnsubscribes()
                        .andThen(oneSubscriberUnsubscribes()))
                .then(expectAllRemainingSubscribersToStillBeSubscribed());
    }

    @Test
    default void testPipe_canUnsubscribeTheSameSubscriberSeveralTimes(final PipeTestConfig testConfig) throws Exception {
        given(aConfiguredPipe(testConfig)
                .withSeveralSubscriber(5))
                .when(oneSubscriberUnsubscribesSeveralTimes(2))
                .then(expectAllRemainingSubscribersToStillBeSubscribed());
    }

    //pipeStatistics
    @Test
    default void testPipe_returnsCorrectNumberOfAcceptedMessages(final PipeTestConfig testConfig) throws Exception {
        given(aConfiguredPipe(testConfig)
                .withASingleSubscriber())
                .when(severalMessagesAreSendAsynchronously(3, 5)
                        .andThen(theNumberOfAcceptedMessagesIsQueried()))
                .then(expectResultToBe(15));
    }

    @Test
    default void testPipe_returnsCorrectNumberOfSuccessfulMessages(final PipeTestConfig testConfig) throws Exception {
        given(aConfiguredPipe(testConfig)
                .withASingleSubscriber())
                .when(severalMessagesAreSendAsynchronously(3, 5)
                        .andThen(aShortWaitIsDone(10, MILLISECONDS))
                        .andThen(theNumberOfSuccessfulMessagesIsQueried()))
                .then(expectResultToBe(15));
    }

    @Test
    default void testPipe_returnsCorrectNumberOfDeliveryFailedMessages(final PipeTestConfig testConfig) throws Exception {
        given(aConfiguredPipe(testConfig)
                .causingErrorsWhenDelivering())
                .when(severalMessagesAreSendAsynchronously(3, 5)
                        .andThen(aShortWaitIsDone(10, MILLISECONDS))
                        .andThen(theNumberOfFailedMessagesIsQueried()))
                .then(expectResultToBe(15));
    }

    // statistic of waiting message config dependent

    // message statistics with blocking subscribers also config dependent

    @Test
    default void testPipe_returnsAValidTimestampForStatistics(final PipeTestConfig testConfig) throws Exception {
        given(aConfiguredPipe(testConfig)
                .withoutASubscriber())
                .when(theTimestampOfTheStatisticsIsQueried())
                .then(expectTimestampToBeInTheLastXSeconds(3));
    }

    //exceptions handling
    @Test
    default void testPipe_canUseCustomErrorHandler(final PipeTestConfig testConfig) throws Exception {
        given(aConfiguredPipe(testConfig)
                .withACustomErrorHandler())
                .when(aMessageResultingInAnErrorIsSend())
                .then(expectTheExceptionToBeHandled());
    }

    @Test
    default void testPipe_customErrorHandlerCanSuppressExceptionSoThatDeliveryCountsAsSuccessful(final PipeTestConfig testConfig) throws Exception {
        given(aConfiguredPipe(testConfig)
                .withACustomErrorHandlerThatSuppressException())
                .when(aMessageResultingInAnErrorIsSend())
                .then(expectTheDeliveryToBeStillSuccessful());
    }

    //shutdown
    @Test
    default void testPipe_canShutdown_evenIfIsBlocked(final PipeTestConfig testConfig) throws Exception {
        given(aConfiguredPipe(testConfig)
                .withASubscriberThatBlocksWhenAccepting())
                .when(severalMessagesAreSendAsynchronouslyBeforeThePipeIsShutdown(3, 5)
                        .andThen(thePipeShutdownIsExpectedForTimeoutInSeconds(1)))
                .then(expectThePipeToBeShutdownInTime());
    }

    @Test
    default void testPipe_shutdownCallIsIdempotent(final PipeTestConfig testConfig) throws Exception {
        given(aConfiguredPipe(testConfig)
                .withASubscriberThatBlocksWhenAccepting())
                .when(thePipeIsShutdownAsynchronouslyXTimes(6)
                        .andThen(thePipeIsShutdown()))
                .then(expectThePipeToBeShutdown());
    }

    @Test
    default void testPipe_awaitReturnsAlwaysFalse_withoutACloseCall(final PipeTestConfig testConfig) throws Exception {
        given(aConfiguredPipe(testConfig))
                .when(PipeActionBuilder.awaitWithoutACloseIsCalled())
                .then(expectTheResultToAlwaysBeFalse());
    }

    @Test
    default void testPipe_throwsExceptionWhenSendIsCalledOnAClosedPipe(final PipeTestConfig testConfig) throws Exception {
        given(aConfiguredPipe(testConfig))
                .when(messagesAreSendAfterTheShutdown())
                .then(expectTheException(AlreadyClosedException.class));
    }

    // behaviour of clean up of messages is config dependent

    //await
    @Test
    default void testPipe_awaitsSucceedsWhenAllTasksCouldBeDone(final PipeTestConfig testConfig) throws Exception {
        final int numberOfMessagesSend = PipeTestConfig.ASYNCHRONOUS_POOL_SIZE + 3;
        given(aConfiguredPipe(testConfig))
                .when(awaitIsCalledBeforeAllTasksAreFinished(numberOfMessagesSend))
                .then(expectTheAwaitToBeTerminatedSuccessful(numberOfMessagesSend));
    }

    //await with unfinished tasks config dependent

}
