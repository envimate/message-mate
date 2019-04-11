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

package com.envimate.messageMate.messageBus;

import com.envimate.messageMate.messageBus.config.AsynchronousDeliveryMessageBusConfigurationResolver;
import com.envimate.messageMate.messageBus.config.MessageBusTestConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.envimate.messageMate.messageBus.config.MessageBusTestConfig.ASYNCHRONOUS_DELIVERY_POOL_SIZE;
import static com.envimate.messageMate.messageBus.givenWhenThen.Given.given;
import static com.envimate.messageMate.messageBus.givenWhenThen.MessageBusActionBuilder.*;
import static com.envimate.messageMate.messageBus.givenWhenThen.MessageBusSetupBuilder.aConfiguredMessageBus;
import static com.envimate.messageMate.messageBus.givenWhenThen.MessageBusValidationBuilder.expectResultToBe;
import static com.envimate.messageMate.messageBus.givenWhenThen.MessageBusValidationBuilder.expectXMessagesToBeDelivered;

@ExtendWith(AsynchronousDeliveryMessageBusConfigurationResolver.class)
public class AsynchronousDeliveryMessageBusSpecs implements MessageBusSpecs {

    @Test
    public void testMessageBus_queryingNumberOfQueuedMessages(final MessageBusTestConfig config) throws Exception {
        final int expectedQueuedMessages = 5;
        final int messagesSendParallel = MessageBusTestConfig.ASYNCHRONOUS_DELIVERY_POOL_SIZE + expectedQueuedMessages;
        given(aConfiguredMessageBus(config)
                .withASubscriberThatBlocksWhenAccepting())
                .when(severalMessagesAreSendAsynchronouslyButWillBeBlocked(messagesSendParallel, 1)
                        .andThen(theNumberOfQueuedMessagesIsQueried()))
                .then(expectResultToBe(expectedQueuedMessages));
    }

    //shutdown
    @Test
    public void testMessageBus_whenShutdownAllRemainingTasksAreFinished(final MessageBusTestConfig config) throws Exception {
        final int numberOfParallelSendMessages = 10;
        final boolean finishRemainingTasks = true;
        given(aConfiguredMessageBus(config))
                .when(sendSeveralMessagesBeforeTheBusIsShutdown(numberOfParallelSendMessages, finishRemainingTasks))
                .then(expectXMessagesToBeDelivered(10));
    }

    @Test
    public void testMessageBus_whenShutdownWithoutFinishingRemainingTasks_allTasksAreStillFinished(
            final MessageBusTestConfig config) throws Exception {
        final int numberOfParallelSendMessages = ASYNCHRONOUS_DELIVERY_POOL_SIZE + 3;
        final boolean finishRemainingTasks = false;
        given(aConfiguredMessageBus(config))
                .when(sendSeveralMessagesBeforeTheBusIsShutdown(numberOfParallelSendMessages, finishRemainingTasks))
                .then(expectXMessagesToBeDelivered(ASYNCHRONOUS_DELIVERY_POOL_SIZE));
    }
}
