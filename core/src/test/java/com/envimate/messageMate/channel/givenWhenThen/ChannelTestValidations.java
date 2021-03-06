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
import com.envimate.messageMate.channel.action.Action;
import com.envimate.messageMate.filtering.Filter;
import com.envimate.messageMate.processingContext.ProcessingContext;
import com.envimate.messageMate.shared.environment.TestEnvironment;
import com.envimate.messageMate.shared.environment.TestEnvironmentProperty;
import com.envimate.messageMate.shared.subscriber.TestSubscriber;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

import static com.envimate.messageMate.channel.givenWhenThen.ChannelTestActions.channelTestActions;
import static com.envimate.messageMate.channel.givenWhenThen.ChannelTestProperties.MODIFIED_META_DATUM;
import static com.envimate.messageMate.channel.givenWhenThen.ProcessingFrameHistoryMatcher.aProcessingFrameHistory;
import static com.envimate.messageMate.shared.environment.TestEnvironmentProperty.*;
import static com.envimate.messageMate.shared.properties.SharedTestProperties.ERROR_SUBSCRIBER;
import static com.envimate.messageMate.shared.properties.SharedTestProperties.FILTER_POSITION;
import static com.envimate.messageMate.shared.polling.PollingUtils.pollUntil;
import static com.envimate.messageMate.shared.polling.PollingUtils.pollUntilListHasSize;
import static com.envimate.messageMate.shared.validations.SharedTestValidations.assertListOfSize;
import static lombok.AccessLevel.PRIVATE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RequiredArgsConstructor(access = PRIVATE)
final class ChannelTestValidations {

    static void assertResultTraversedAllChannelBasedOnTheirDefaultActions(
            final TestEnvironment testEnvironment,
            final List<Channel<TestMessage>> expectedTraversedChannels) {
        final ProcessingContext<TestMessage> result = getTestPropertyAsProcessingContext(testEnvironment, RESULT);
        final ProcessingFrameHistoryMatcher processingFrameHistoryMatcher = aProcessingFrameHistory();
        expectedTraversedChannels.forEach(channel -> {
            final Action<TestMessage> defaultAction = channel.getDefaultAction();
            processingFrameHistoryMatcher.withAFrameFor(channel, defaultAction);
        });
        processingFrameHistoryMatcher.assertCorrect(result);
    }

    static void assertMessageFollowedChannelWithActions(final TestEnvironment testEnvironment,
                                                        final ProcessingFrameHistoryMatcher processingFrameHistoryMatcher) {
        final ProcessingContext<TestMessage> result = getTestPropertyAsProcessingContext(testEnvironment, RESULT);
        processingFrameHistoryMatcher.assertCorrect(result);
    }

    static void assertFilterAsExpected(final TestEnvironment testEnvironment,
                                       final List<Filter<ProcessingContext<TestMessage>>> expectedFilter) {
        final FilterPosition filterPosition = testEnvironment.getPropertyAsType(FILTER_POSITION, FilterPosition.class);
        final Channel<TestMessage> channel = getTestPropertyAsChannel(testEnvironment, SUT);
        final ChannelTestActions testActions = channelTestActions(channel);
        final List<?> actualFilter = testActions.getFilter(filterPosition);
        assertThat(actualFilter, equalTo(expectedFilter));
    }

    static void assertMetaDatumOfResultSetAsExpected(final TestEnvironment testEnvironment) {
        final String expectedMetaDatum = testEnvironment.getPropertyAsType(EXPECTED_RESULT, String.class);
        final ProcessingContext<TestMessage> result = getTestPropertyAsProcessingContext(testEnvironment, RESULT);
        final Map<Object, Object> contextMetaData = result.getContextMetaData();
        final Object actualMetaDatum = contextMetaData.get(MODIFIED_META_DATUM);
        assertThat(actualMetaDatum, equalTo(expectedMetaDatum));
    }

    static void assertOnlyFirstSubscriberReceivedMessage(final TestEnvironment testEnvironment) {
        final TestSubscriber<?> subscriber = testEnvironment.getPropertyAsType(EXPECTED_RECEIVERS, TestSubscriber.class);
        pollUntilListHasSize(subscriber::getReceivedMessages, 1);
        final TestSubscriber<?> subscriberNotCalled = testEnvironment.getPropertyAsType(ERROR_SUBSCRIBER, TestSubscriber.class);
        final List<?> receivedMessages = subscriberNotCalled.getReceivedMessages();
        assertListOfSize(receivedMessages, 0);
    }

    @SuppressWarnings("unchecked")
    private static Channel<TestMessage> getTestPropertyAsChannel(final TestEnvironment testEnvironment,
                                                                 final TestEnvironmentProperty property) {
        pollUntil(() -> testEnvironment.has(property));
        return (Channel<TestMessage>) testEnvironment.getProperty(property);
    }

    @SuppressWarnings("unchecked")
    private static ProcessingContext<TestMessage> getTestPropertyAsProcessingContext(final TestEnvironment testEnvironment,
                                                                                     final TestEnvironmentProperty property) {
        pollUntil(() -> testEnvironment.has(property));
        return (ProcessingContext<TestMessage>) testEnvironment.getProperty(property);
    }

}
