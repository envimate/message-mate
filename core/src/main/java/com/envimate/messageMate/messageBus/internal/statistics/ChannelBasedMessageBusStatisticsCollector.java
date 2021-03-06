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

package com.envimate.messageMate.messageBus.internal.statistics;

import com.envimate.messageMate.channel.Channel;
import com.envimate.messageMate.channel.statistics.ChannelStatistics;
import com.envimate.messageMate.channel.ChannelStatusInformation;
import com.envimate.messageMate.messageBus.statistics.MessageBusStatistics;
import lombok.RequiredArgsConstructor;

import java.math.BigInteger;
import java.util.Date;

import static com.envimate.messageMate.messageBus.statistics.MessageBusStatistics.messageBusStatistics;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class ChannelBasedMessageBusStatisticsCollector implements MessageBusStatisticsCollector {
    private final Channel<?> channel;

    public static ChannelBasedMessageBusStatisticsCollector channelBasedMessageBusStatisticsCollector(final Channel<?> channel) {
        return new ChannelBasedMessageBusStatisticsCollector(channel);
    }

    @Override
    public MessageBusStatistics getStatistics() {
        final ChannelStatusInformation statusInformation = channel.getStatusInformation();
        final ChannelStatistics channelStatistics = statusInformation.getChannelStatistics();
        final MessageBusStatistics messageBusStatistics = getMessageBusStatistics(channelStatistics);
        return messageBusStatistics;
    }

    private MessageBusStatistics getMessageBusStatistics(final ChannelStatistics channelStatistics) {
        final Date timestamp = channelStatistics.getTimestamp();
        final BigInteger acceptedMessages = channelStatistics.getAcceptedMessages();
        final BigInteger successfulMessages = channelStatistics.getSuccessfulMessages();
        final BigInteger failedMessages = channelStatistics.getFailedMessages();
        final BigInteger blockedMessages = channelStatistics.getBlockedMessages();
        final BigInteger forgottenMessages = channelStatistics.getForgottenMessages();
        final BigInteger queuedMessages = channelStatistics.getQueuedMessages();
        return messageBusStatistics(timestamp, acceptedMessages, successfulMessages, failedMessages, blockedMessages,
                forgottenMessages, queuedMessages);
    }
}
