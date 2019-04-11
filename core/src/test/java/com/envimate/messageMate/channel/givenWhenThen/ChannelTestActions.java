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

package com.envimate.messageMate.channel.givenWhenThen;

import com.envimate.messageMate.channel.Channel;
import com.envimate.messageMate.channel.ChannelProcessingFrame;
import com.envimate.messageMate.channel.ChannelStatusInformation;
import com.envimate.messageMate.channel.action.Action;
import com.envimate.messageMate.channel.statistics.ChannelStatistics;
import com.envimate.messageMate.filtering.Filter;
import com.envimate.messageMate.processingContext.EventType;
import com.envimate.messageMate.processingContext.ProcessingContext;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import com.envimate.messageMate.shared.testMessages.TestMessageOfInterest;
import lombok.RequiredArgsConstructor;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.envimate.messageMate.channel.action.Call.callTo;
import static com.envimate.messageMate.channel.givenWhenThen.ChannelTestProperties.MODIFIED_META_DATUM;
import static com.envimate.messageMate.channel.givenWhenThen.FilterPosition.PROCESS;
import static com.envimate.messageMate.processingContext.ProcessingContext.processingContext;
import static com.envimate.messageMate.shared.testMessages.TestMessageOfInterest.messageOfInterest;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
final class ChannelTestActions {
    static final TestMessageOfInterest DEFAULT_TEST_MESSAGE = messageOfInterest();
    static final EventType DEFAULT_EVENT_TYPE = EventType.eventTypeFromString("defaultEventType");

    static ProcessingContext<TestMessage> sendMessage(final Channel<TestMessage> channel, final TestMessage testMessage) {
        final ProcessingContext<TestMessage> processingContext = processingContext(DEFAULT_EVENT_TYPE, testMessage);
        channel.send(processingContext);
        return processingContext;
    }

    static void addFilterExecutingACall(final Channel<TestMessage> channel, final Channel<TestMessage> targetChannel) {
        channel.addProcessFilter((processingContext, filterActions) -> {
            callTo(targetChannel, processingContext);
            filterActions.pass(processingContext);
        });
    }

    static void addChangingActionFilterToPipe(final Channel<TestMessage> channel, final FilterPosition filterPosition,
                                              final Action<TestMessage> action) {
        final Filter<ProcessingContext<TestMessage>> filter = (processingContext, filterActions) -> {
            final ChannelProcessingFrame<TestMessage> currentProcessingFrame = processingContext.getCurrentProcessingFrame();
            currentProcessingFrame.setAction(action);
            filterActions.pass(processingContext);
        };
        addFilterToChannel(channel, filterPosition, filter);
    }

    static void addAFilterChangingMetaData(final Channel<TestMessage> channel, final Object metaDatum) {
        final Filter<ProcessingContext<TestMessage>> filter = (processingContext, filterActions) -> {
            final Map<Object, Object> metaData = processingContext.getContextMetaData();
            metaData.put(MODIFIED_META_DATUM, metaDatum);
            filterActions.pass(processingContext);
        };
        addFilterToChannel(channel, PROCESS, filter);
    }

    static List<Filter<ProcessingContext<TestMessage>>> addSeveralNoopFilter(final Channel<TestMessage> channel,
                                                                             final int[] positions,
                                                                             final FilterPosition filterPosition) {
        final List<Filter<ProcessingContext<TestMessage>>> expectedFilter = new LinkedList<>();
        for (final int position : positions) {
            final Filter<ProcessingContext<TestMessage>> filter = addANoopFilterAtPosition(channel, filterPosition, position);
            expectedFilter.add(position, filter);
        }
        return expectedFilter;
    }

    static Filter<ProcessingContext<TestMessage>> addANoopFilterAtPosition(final Channel<TestMessage> channel,
                                                                           final FilterPosition filterPosition,
                                                                           final int position) {
        final Filter<ProcessingContext<TestMessage>> filter = (processingContext, filterActions) -> {
            filterActions.pass(processingContext);
        };
        addFilterToChannelAtPosition(channel, filterPosition, filter, position);
        return filter;
    }

    static long queryChannelStatistics(final Channel<TestMessage> channel,
                                       final Function<ChannelStatistics, BigInteger> extraction) {
        final ChannelStatusInformation statusInformation = channel.getStatusInformation();
        final ChannelStatistics statistics = statusInformation.getChannelStatistics();
        final BigInteger result = extraction.apply(statistics);
        return result.longValueExact();
    }

    private static void addFilterToChannel(final Channel<TestMessage> channel,
                                           final FilterPosition filterPosition,
                                           final Filter<ProcessingContext<TestMessage>> filter) {
        switch (filterPosition) {
            case PRE:
                channel.addPreFilter(filter);
                break;
            case PROCESS:
                channel.addProcessFilter(filter);
                break;
            case POST:
                channel.addPostFilter(filter);
                break;
            default:
                throw new UnsupportedOperationException("Unknown filterPosition " + filterPosition + ".");
        }
    }

    private static void addFilterToChannelAtPosition(final Channel<TestMessage> channel,
                                                     final FilterPosition filterPosition,
                                                     final Filter<ProcessingContext<TestMessage>> filter,
                                                     final int position) {
        switch (filterPosition) {
            case PRE:
                channel.addPreFilter(filter, position);
                break;
            case PROCESS:
                channel.addProcessFilter(filter, position);
                break;
            case POST:
                channel.addPostFilter(filter, position);
                break;
            default:
                throw new UnsupportedOperationException("Unknown filterPosition " + filterPosition + ".");
        }
    }

    static List<Filter<ProcessingContext<TestMessage>>> getFilterOf(final Channel<TestMessage> channel,
                                                                    final FilterPosition filterPosition) {
        switch (filterPosition) {
            case PRE:
                return channel.getPreFilter();
            case PROCESS:
                return channel.getProcessFilter();
            case POST:
                return channel.getPostFilter();
            default:
                throw new UnsupportedOperationException("Unknown filterPosition " + filterPosition + ".");
        }
    }

    static void removeFilter(final Channel<TestMessage> channel,
                             final FilterPosition filterPosition,
                             final Filter<ProcessingContext<TestMessage>> filter) {
        switch (filterPosition) {
            case PRE:
                channel.removePreFilter(filter);
                break;
            case PROCESS:
                channel.removeProcessFilter(filter);
                break;
            case POST:
                channel.removePostFilter(filter);
                break;
            default:
                throw new UnsupportedOperationException("Unknown filterPosition " + filterPosition + ".");
        }
    }
}
