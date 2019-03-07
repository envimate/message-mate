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

import com.envimate.messageMate.channel.config.ChannelTestConfig;
import com.envimate.messageMate.channel.config.SynchronousChannelConfigResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.envimate.messageMate.channel.givenWhenThen.ChannelActionBuilder.*;
import static com.envimate.messageMate.channel.givenWhenThen.ChannelSetupBuilder.aConfiguredChannel;
import static com.envimate.messageMate.channel.givenWhenThen.ChannelValidationBuilder.*;
import static com.envimate.messageMate.channel.givenWhenThen.Given.given;

@ExtendWith(SynchronousChannelConfigResolver.class)
public class SynchronousChannelSpecs implements ChannelSpecs {

    //statistics
    @Test
    public void testChannel_canQueuedMessages(final ChannelTestConfig channelTestConfig) {
        final int numberOfSendMessages = 5;
        given(aConfiguredChannel(channelTestConfig)
                .withABlockingSubscriber())
                .when(severalMessagesAreSendAsynchronously(numberOfSendMessages)
                        .andThen(theNumberOfQueuedMessagesIsQueried()))
                .then(expectTheResult(0));
    }

    //shutdown
    @Test
    public void testChannel_closeWithoutFinishingRemainingTasks_hasNoEffectForSynchronousConfig(final ChannelTestConfig channelTestConfig) {
        final int numberOfSendMessages = 5;
        given(aConfiguredChannel(channelTestConfig)
                .withABlockingSubscriber())
                .when(severalMessagesAreSendAsynchronouslyBeforeTheChannelIsClosedWithoutFinishingRemainingTasks(numberOfSendMessages)
                        .andThen(theSubscriberLockIsReleased()
                                .andThen(theNumberOfSuccessfulDeliveredMessagesIsQueried())))
                .then(expectTheResult(numberOfSendMessages)
                        .and(expectTheChannelToBeShutdown()));
    }

    //await
    @Test
    public void testChannel_awaitsWithoutFinishingTasks_succeedsDespiteNotFinished(final ChannelTestConfig channelTestConfig) {
        final int numberOfMessages = 5;
        given(aConfiguredChannel(channelTestConfig)
                .withABlockingSubscriber())
                .when(severalMessagesAreSendAsynchronouslyBeforeTheChannelIsClosedWithoutFinishingRemainingTasks(numberOfMessages)
                        .andThen(theShutdownIsAwaited()))
                .then(expectTheShutdownToBeSucceededInTime());
    }
}