package com.envimate.messageMate.channel;

import com.envimate.messageMate.filtering.Filter;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.qcec.shared.TestEnvironmentProperty;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

import static com.envimate.messageMate.channel.ChannelTestActions.getFilterOf;
import static com.envimate.messageMate.channel.ChannelTestProperties.MODIFIED_META_DATUM;
import static com.envimate.messageMate.channel.ProcessingFrameHistoryMatcher.aProcessingFrameHistory;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.*;
import static lombok.AccessLevel.PRIVATE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RequiredArgsConstructor(access = PRIVATE)
final class ChannelTestValidations {

    static void assertResultTraversedAllChannelBasedOnTheirDefaultActions(final TestEnvironment testEnvironment,
                                                                          final List<Channel<TestMessage>> expectedTraversedChannels) {
        final ProcessingContext<TestMessage> result = getTestPropertyAsProcessingContext(testEnvironment, RESULT);
        final ProcessingFrameHistoryMatcher processingFrameHistoryMatcher = aProcessingFrameHistory();
        expectedTraversedChannels.forEach(channel -> processingFrameHistoryMatcher.withAFrameFor(channel, channel.getDefaultAction()));
        processingFrameHistoryMatcher.assertCorrect(result);
    }

    static void assertMessageFollowedChannelWithActions(final TestEnvironment testEnvironment,
                                                        final ProcessingFrameHistoryMatcher processingFrameHistoryMatcher) {
        final ProcessingContext<TestMessage> result = getTestPropertyAsProcessingContext(testEnvironment, RESULT);
        processingFrameHistoryMatcher.assertCorrect(result);
    }


    static void assertFilterAsExpected(final TestEnvironment testEnvironment, final List<Filter<ProcessingContext<TestMessage>>> expectedFilter) {
        final FilterPosition filterPosition = testEnvironment.getPropertyAsType(ChannelTestProperties.PIPE, FilterPosition.class);
        final Channel<TestMessage> channel = getTestPropertyAsChannel(testEnvironment, SUT);
        final List<Filter<ProcessingContext<TestMessage>>> actualFilter = getFilterOf(channel, filterPosition);
        assertThat(actualFilter, equalTo(expectedFilter));
    }

    static void assertMetaDatumOfResultSetAsExpected(final TestEnvironment testEnvironment) {
        final String expectedMetaDatum = testEnvironment.getPropertyAsType(EXPECTED_RESULT, String.class);
        final ProcessingContext<TestMessage> result = getTestPropertyAsProcessingContext(testEnvironment, RESULT);
        final Map<Object, Object> contextMetaData = result.getContextMetaData();
        final Object actualMetaDatum = contextMetaData.get(MODIFIED_META_DATUM);
        assertThat(actualMetaDatum, equalTo(expectedMetaDatum));
    }

    @SuppressWarnings("unchecked")
    private static Channel<TestMessage> getTestPropertyAsChannel(final TestEnvironment testEnvironment, final TestEnvironmentProperty property) {
        return (Channel<TestMessage>) testEnvironment.getProperty(property);
    }

    @SuppressWarnings("unchecked")
    private static ProcessingContext<TestMessage> getTestPropertyAsProcessingContext(final TestEnvironment testEnvironment,
                                                                                     final TestEnvironmentProperty property) {
        return (ProcessingContext<TestMessage>) testEnvironment.getProperty(property);
    }

}
