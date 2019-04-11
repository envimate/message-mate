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

package com.envimate.messageMate.channel;

import com.envimate.messageMate.channel.config.AsynchronousChannelConfigResolver;
import com.envimate.messageMate.channel.config.ChannelTestConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.envimate.messageMate.channel.config.ChannelTestConfig.ASYNCHRONOUS_CHANNEL_CONFIG_POOL_SIZE;
import static com.envimate.messageMate.channel.givenWhenThen.ChannelActionBuilder.*;
import static com.envimate.messageMate.channel.givenWhenThen.ChannelSetupBuilder.aConfiguredChannel;
import static com.envimate.messageMate.channel.givenWhenThen.ChannelValidationBuilder.*;
import static com.envimate.messageMate.channel.givenWhenThen.Given.given;

@ExtendWith(AsynchronousChannelConfigResolver.class)
public class AsynchronousChannelSpecs implements ChannelSpecs {

    //statistics
    @Test
    public void testChannel_canQueuedMessages(final ChannelTestConfig channelTestConfig) {
        final int expectedQueuedMessage = 3;
        final int numberOfSendMessages = ASYNCHRONOUS_CHANNEL_CONFIG_POOL_SIZE + expectedQueuedMessage;
        given(aConfiguredChannel(channelTestConfig)
                .withABlockingSubscriber())
                .when(severalMessagesAreSendAsynchronously(numberOfSendMessages)
                        .andThen(theNumberOfQueuedMessagesIsQueried()))
                .then(expectTheResult(expectedQueuedMessage));
    }

    //shutdown
    @Test
    public void testChannel_closeWithoutFinishingRemainingTasks_hasNoEffectForSynchronousConfig(final ChannelTestConfig config) {
        final int expectedQueuedMessage = 3;
        final int numberOfMessages = ASYNCHRONOUS_CHANNEL_CONFIG_POOL_SIZE + expectedQueuedMessage;
        given(aConfiguredChannel(config)
                .withABlockingSubscriber())
                .when(severalMessagesAreSendAsynchronouslyBeforeTheChannelIsClosedWithoutFinishingRemainingTasks(numberOfMessages)
                        .andThen(theSubscriberLockIsReleased()
                                .andThen(theNumberOfSuccessfulDeliveredMessagesIsQueried())))
                .then(expectTheResult(ASYNCHRONOUS_CHANNEL_CONFIG_POOL_SIZE)
                        .and(expectTheChannelToBeShutdown()));
    }

    //await
    @Test
    public void testChannel_awaitsWithoutFinishingTasks_succeedsDespiteNotFinished(final ChannelTestConfig config) {
        final int numberOfMessages = ASYNCHRONOUS_CHANNEL_CONFIG_POOL_SIZE + 5;
        given(aConfiguredChannel(config)
                .withABlockingSubscriber())
                .when(sendMessagesBeforeTheShutdownIsAwaitedWithoutFinishingTasks(numberOfMessages))
                .then(expectTheShutdownToBeFailed());
    }
}
