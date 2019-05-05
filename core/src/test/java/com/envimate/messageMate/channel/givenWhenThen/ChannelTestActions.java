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
import com.envimate.messageMate.channel.action.Subscription;
import com.envimate.messageMate.channel.statistics.ChannelStatistics;
import com.envimate.messageMate.filtering.Filter;
import com.envimate.messageMate.identification.CorrelationId;
import com.envimate.messageMate.identification.MessageId;
import com.envimate.messageMate.processingContext.EventType;
import com.envimate.messageMate.processingContext.ProcessingContext;
import com.envimate.messageMate.shared.environment.TestEnvironment;
import com.envimate.messageMate.shared.pipeChannelMessageBus.testActions.CorrelationIdSendingActions;
import com.envimate.messageMate.shared.pipeChannelMessageBus.testActions.ProcessingContextSendingActions;
import com.envimate.messageMate.shared.pipeChannelMessageBus.testActions.RawSubscribeActions;
import com.envimate.messageMate.shared.pipeChannelMessageBus.testActions.SendingAndReceivingActions;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import com.envimate.messageMate.shared.testMessages.TestMessageOfInterest;
import com.envimate.messageMate.shared.utils.SendingTestUtils;
import com.envimate.messageMate.subscribing.Subscriber;
import com.envimate.messageMate.subscribing.SubscriptionId;
import lombok.RequiredArgsConstructor;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.envimate.messageMate.channel.action.Call.callTo;
import static com.envimate.messageMate.channel.givenWhenThen.ChannelTestProperties.MODIFIED_META_DATUM;
import static com.envimate.messageMate.channel.givenWhenThen.FilterPosition.PROCESS;
import static com.envimate.messageMate.processingContext.ProcessingContext.processingContext;
import static com.envimate.messageMate.shared.testMessages.TestMessageOfInterest.messageOfInterest;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class ChannelTestActions implements SendingAndReceivingActions, ProcessingContextSendingActions,
        CorrelationIdSendingActions, RawSubscribeActions {
    static final TestMessageOfInterest DEFAULT_TEST_MESSAGE = messageOfInterest();
    static final EventType DEFAULT_EVENT_TYPE = EventType.eventTypeFromString("defaultEventType");

    private final Channel<TestMessage> channel;

    public static ChannelTestActions channelTestActions(final Channel<TestMessage> channel) {
        return new ChannelTestActions(channel);
    }

    static ProcessingContext<TestMessage> sendMessage(final Channel<TestMessage> channel,
                                                      final TestEnvironment testEnvironment,
                                                      final TestMessage message) {
        final ChannelTestActions testActions = ChannelTestActions.channelTestActions(channel);
        final ProcessingContext<TestMessage> processingContext = processingContext(DEFAULT_EVENT_TYPE, message);
        SendingTestUtils.sendProcessingContext(testActions, testEnvironment, processingContext);
        return processingContext;
    }

    static void addFilterExecutingACall(final Channel<TestMessage> channel, final Channel<TestMessage> targetChannel) {
        channel.addProcessFilter((processingContext, filterActions) -> {
            callTo(targetChannel, processingContext);
            filterActions.pass(processingContext);
        });
    }

    static void addChangingActionFilterToPipe(final Channel<TestMessage> channel,
                                              final FilterPosition filterPosition,
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

    @Override
    public void close(final boolean finishRemainingTasks) {
        channel.close(finishRemainingTasks);
    }

    @Override
    public boolean await(final int timeout, final TimeUnit timeUnit) throws InterruptedException {
        return channel.awaitTermination(timeout, timeUnit);
    }

    @Override
    public boolean isClosed() {
        return channel.isClosed();
    }

    @Override
    public MessageId send(final EventType eventType, final TestMessage message) {
        return channel.send(message);
    }

    @Override
    public MessageId send(final ProcessingContext<TestMessage> processingContext) {
        return channel.send(processingContext);
    }

    @Override
    public MessageId send(final EventType eventType, final TestMessage message, final CorrelationId correlationId) {
        return channel.send(message, correlationId);
    }

    @Override
    public void subscribe(final EventType eventType, final Subscriber<TestMessage> subscriber) {
        final Subscription<TestMessage> subscription = getActionAsSubscription();
        subscription.addSubscriber(subscriber);
    }

    @Override
    public void unsubscribe(final SubscriptionId subscriptionId) {
        final Subscription<TestMessage> subscription = getActionAsSubscription();
        subscription.removeSubscriber(subscriptionId);
    }

    @Override
    public List<Subscriber<?>> getAllSubscribers() {
        final Subscription<TestMessage> subscription = getActionAsSubscription();
        return subscription.getAllSubscribers();
    }

    @Override
    public SubscriptionId subscribeRaw(final EventType eventType, final Subscriber<ProcessingContext<TestMessage>> subscriber) {
        final Subscription<TestMessage> subscription = getActionAsSubscription();
        return subscription.addRawSubscriber(subscriber);
    }

    private Subscription<TestMessage> getActionAsSubscription() {
        return (Subscription<TestMessage>) channel.getDefaultAction();
    }
}
