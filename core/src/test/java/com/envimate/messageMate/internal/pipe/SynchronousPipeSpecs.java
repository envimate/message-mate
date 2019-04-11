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

import com.envimate.messageMate.internal.pipe.config.PipeTestConfig;
import com.envimate.messageMate.internal.pipe.config.SynchronisedPipeConfigurationResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.envimate.messageMate.internal.pipe.givenWhenThen.Given.given;
import static com.envimate.messageMate.internal.pipe.givenWhenThen.PipeActionBuilder.*;
import static com.envimate.messageMate.internal.pipe.givenWhenThen.PipeSetupBuilder.aConfiguredPipe;
import static com.envimate.messageMate.internal.pipe.givenWhenThen.PipeValidationBuilder.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@ExtendWith(SynchronisedPipeConfigurationResolver.class)
public class SynchronousPipeSpecs implements PipeSpecs {

    //messageStatistics
    @Test
    public void testPipe_withBlockingSubscriber_whenNumberOfSuccessfulDeliveredMessagesIsQueried_returnsZero(
            final PipeTestConfig testConfig) throws Exception {
        given(aConfiguredPipe(testConfig)
                .withASubscriberThatBlocksWhenAccepting())
                .when(severalMessagesAreSendAsynchronouslyButWillBeBlocked(3, 5)
                        .andThen(theNumberOfSuccessfulMessagesIsQueried()))
                .then(expectResultToBe(0));
    }

    @Test
    public void testPipe_withBlockingSubscriber_whenNumberOfAcceptedMessagesIsQueried_returnsNumberOfThreads(
            final PipeTestConfig testConfig) throws Exception {
        final int numberOfParallelSends = 3;
        given(aConfiguredPipe(testConfig)
                .withASubscriberThatBlocksWhenAccepting())
                .when(severalMessagesAreSendAsynchronouslyButWillBeBlocked(numberOfParallelSends, 5)
                        .andThen(aShortWaitIsDone(10, MILLISECONDS))
                        .andThen(theNumberOfAcceptedMessagesIsQueriedAsynchronously()))
                .then(expectResultToBe(numberOfParallelSends));
    }

    @Test
    public void testPipe_withBlockingSubscriber_whenNumberOfQueuedMessagesIsQueried_returnsNumberOfThreads(
            final PipeTestConfig testConfig) throws Exception {
        given(aConfiguredPipe(testConfig)
                .withASubscriberThatBlocksWhenAccepting())
                .when(severalMessagesAreSendAsynchronouslyButWillBeBlocked(3, 5)
                        .andThen(theNumberOfQueuedMessagesIsQueried()))
                .then(expectResultToBe(0));
    }

    //shutdown
    @Test
    public void testPipe_whenShutdown_deliversRemainingMessagesButNoNewAdded(final PipeTestConfig testConfig) throws Exception {
        final int numberOfParallelSendMessagesBeforeShutdown = 5;
        given(aConfiguredPipe(testConfig))
                .when(thePipeIsShutdownAfterHalfOfTheMessagesWereDelivered(10))
                .then(expectXMessagesToBeDelivered_despiteTheChannelClosed(numberOfParallelSendMessagesBeforeShutdown));
        //because waiting senders wait on synchronised send -> they never entered the Pipe and do not count as remaining
    }

    @Test
    public void testPipe_whenShutdownWithoutFinishingRemainingTasksIsCalled_noTasksAreFinished(
            final PipeTestConfig testConfig) throws Exception {
        final int numberOfParallelSendMessagesBeforeShutdown = 5;
        given(aConfiguredPipe(testConfig))
                .when(thePipeIsShutdownAfterHalfOfTheMessagesWereDelivered_withoutFinishingRemainingTasks(10))
                .then(expectXMessagesToBeDelivered_despiteTheChannelClosed(numberOfParallelSendMessagesBeforeShutdown));
    }

    //await
    @Test
    public void testPipe_awaitsFailsWhenAllTasksCouldBeDone(final PipeTestConfig testConfig) throws Exception {
        final int numberOfMessagesSend = 5;
        given(aConfiguredPipe(testConfig))
                .when(awaitIsCalledWithoutExpectingTasksToFinish(numberOfMessagesSend))
                .then(expectTheAwaitToBeTerminatedSuccessful(numberOfMessagesSend));
        //await returns always true, as it's not feasible to check whether there are still Threads waiting in the pipe
    }

}
