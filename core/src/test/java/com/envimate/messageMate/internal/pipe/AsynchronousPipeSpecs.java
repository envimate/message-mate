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

package com.envimate.messageMate.internal.pipe;

import com.envimate.messageMate.internal.pipe.config.AsynchronousPipeConfigurationProvider;
import com.envimate.messageMate.internal.pipe.config.PipeTestConfig;
import com.envimate.messageMate.internal.pipe.transport.PipeWaitingQueueIsFullException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.envimate.messageMate.internal.pipe.config.PipeTestConfig.*;
import static com.envimate.messageMate.internal.pipe.givenWhenThen.Given.given;
import static com.envimate.messageMate.internal.pipe.givenWhenThen.PipeActionBuilder.*;
import static com.envimate.messageMate.internal.pipe.givenWhenThen.PipeSetupBuilder.aConfiguredPipe;
import static com.envimate.messageMate.internal.pipe.givenWhenThen.PipeValidationBuilder.*;

@ExtendWith(AsynchronousPipeConfigurationProvider.class)
public class AsynchronousPipeSpecs implements PipeSpecs {

    //send
    @Test
    public void testPipe_doesNotFailForFullWaitingQueue() {
        final int completeCapacity = ASYNCHRONOUS_QUEUED_BOUND + ASYNCHRONOUS_PIPE_POOL_SIZE;
        given(aConfiguredPipe(anAsynchronousBoundedPipe())
                .withASubscriberThatBlocksWhenAccepting())
                .when(severalMessagesAreSend(completeCapacity))
                .then(expectNoException());
    }

    @Test
    public void testPipe_failsWhenBoundedQueueOverflows() {
        final int completeCapacity = ASYNCHRONOUS_QUEUED_BOUND + ASYNCHRONOUS_PIPE_POOL_SIZE;
        final int messagesSend = completeCapacity + 1;
        given(aConfiguredPipe(anAsynchronousBoundedPipe())
                .withASubscriberThatBlocksWhenAccepting())
                .when(severalMessagesAreSend(messagesSend))
                .then(expectTheException(PipeWaitingQueueIsFullException.class));
    }

    //statistics
    @Test
    public void testPipe_withBlockingSubscriber_whenNumberOfSuccessfulDeliveredMessagesIsQueried_returnsZero(
            final PipeTestConfig testConfig) {
        final int numberOfMessages = ASYNCHRONOUS_PIPE_POOL_SIZE;
        given(aConfiguredPipe(testConfig))
                .when(severalMessagesAreSendAsynchronouslyButWillBeBlocked(numberOfMessages)
                        .andThen(theNumberOfSuccessfulMessagesIsQueriedWhenSubscriberBlocked()))
                .then(expectResultToBe(0));
    }

    @Test
    public void testPipe_withBlockingSubscriber_whenNumberOfFailedDeliveredMessagesIsQueried_returnsZero(
            final PipeTestConfig testConfig) {
        final int numberOfMessages = ASYNCHRONOUS_PIPE_POOL_SIZE;
        final int expectedResult = 0;
        given(aConfiguredPipe(testConfig))
                .when(severalMessagesAreSendAsynchronouslyButWillBeBlocked(numberOfMessages)
                        .andThen(theNumberOfFailedMessagesIsQueried(expectedResult)))
                .then(expectResultToBe(expectedResult));
    }

    @Test
    public void testPipe_withBlockingSubscriber_whenNumberOfAcceptedMessagesIsQueried_returnsAll(final PipeTestConfig testConfig) {
        final int numberOfParallelSender = 3;
        final int numberOfMessagesPerSender = 5;
        final int expectedAcceptedMessages = numberOfParallelSender * numberOfMessagesPerSender;
        given(aConfiguredPipe(testConfig))
                .when(severalMessagesAreSendAsynchronouslyButWillBeBlocked(numberOfParallelSender, numberOfMessagesPerSender)
                        .andThen(theNumberOfAcceptedMessagesIsQueried()))
                .then(expectResultToBe(expectedAcceptedMessages));
    }

    @Test
    public void testPipe_withBlockingSubscriber_whenNumberOfQueuedMessagesIsQueried(final PipeTestConfig testConfig) {
        final int numberOfParallelSender = 3;
        final int numberOfMessagesPerSender = 5;
        final int sumOfMessages = numberOfParallelSender * numberOfMessagesPerSender;
        final int expectedQueuedMessages = sumOfMessages - ASYNCHRONOUS_PIPE_POOL_SIZE;
        given(aConfiguredPipe(testConfig))
                .when(severalMessagesAreSendAsynchronouslyButWillBeBlocked(numberOfParallelSender, numberOfMessagesPerSender)
                        .andThen(theNumberOfQueuedMessagesIsQueried()))
                .then(expectResultToBe(expectedQueuedMessages));
    }

    //shutdown
    @Test
    public void testPipe_whenShutdown_deliversRemainingMessagesButNoNewAdded(final PipeTestConfig testConfig) {
        given(aConfiguredPipe(testConfig))
                .when(thePipeIsShutdownAfterHalfOfTheMessagesWereDelivered(10))
                .then(expectXMessagesToBeDelivered_despiteTheChannelClosed(5));
    }

    @Test
    public void testPipe_whenShutdownWithoutFinishingRemainingTasksIsCalled_noTasksAreFinished(final PipeTestConfig testConfig) {
        final int numberOfParallelSendMessage = 5;
        given(aConfiguredPipe(testConfig))
                .when(thePipeIsShutdownAfterHalfOfTheMessagesWereDelivered_withoutFinishingRemainingTasks(10))
                .then(expectXMessagesToBeDelivered_despiteTheChannelClosed(numberOfParallelSendMessage));
    }

    //await
    @Test
    public void testPipe_awaitsFailsWhenAllTasksCouldBeDone(final PipeTestConfig testConfig) {
        final int numberOfMessagesSend = ASYNCHRONOUS_PIPE_POOL_SIZE + 3;
        given(aConfiguredPipe(testConfig))
                .when(awaitIsCalledWithoutAllowingRemainingTasksToFinish(numberOfMessagesSend))
                .then(expectTheAwaitToBeTerminatedWithFailure());
    }

}
