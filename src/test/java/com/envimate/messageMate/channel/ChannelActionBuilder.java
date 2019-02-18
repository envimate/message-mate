package com.envimate.messageMate.channel;

import com.envimate.messageMate.filtering.Filter;
import com.envimate.messageMate.qcec.shared.TestAction;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.envimate.messageMate.channel.FilterPosition.*;
import static com.envimate.messageMate.channel.ChannelTestActions.*;
import static com.envimate.messageMate.channel.ChannelTestProperties.CALL_TARGET_CHANNEL;
import static com.envimate.messageMate.channel.ChannelTestProperties.PIPE;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.EXPECTED_RESULT;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.RESULT;
import static com.envimate.messageMate.shared.testMessages.TestMessageOfInterest.messageOfInterest;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class ChannelActionBuilder {
    private final TestAction<Channel<TestMessage>> testAction;

    private static ChannelActionBuilder anAction(final TestAction<Channel<TestMessage>> testAction) {
        return new ChannelActionBuilder(testAction);
    }

    static ChannelActionBuilder aMessageIsSend() {
        return anAction((channel, testEnvironment) -> {
            final ProcessingContext<TestMessage> sendProcessingFrame = sendMessage(channel, DEFAULT_TEST_MESSAGE);
            testEnvironment.setProperty(EXPECTED_RESULT, sendProcessingFrame);
            return null;
        });
    }

    @SuppressWarnings("unchecked")
    static ChannelActionBuilder aCallToTheSecondChannelIsExecuted() {
        return anAction((channel, testEnvironment) -> {
            final Channel<TestMessage> callTargetChannel = (Channel<TestMessage>) testEnvironment.getProperty(CALL_TARGET_CHANNEL);
            addFilterExecutingACall(channel, callTargetChannel);

            final ProcessingContext<TestMessage> sendProcessingFrame = sendMessage(channel, DEFAULT_TEST_MESSAGE);
            testEnvironment.setProperty(EXPECTED_RESULT, sendProcessingFrame);
            return null;
        });
    }

    static ChannelActionBuilder severalPreFilterOnDifferentPositionAreAdded() {
        final int[] positions = new int[]{0, 1, 0, 0, 3, 2};
        return anAction(actionForAddingSeveralFilter(positions, PRE));
    }

    static ChannelActionBuilder severalProcessFilterOnDifferentPositionAreAdded() {
        final int[] positions = new int[]{0, 0, 1, 0, 2, 4, 4};
        return anAction(actionForAddingSeveralFilter(positions, PROCESS));
    }

    static ChannelActionBuilder severalPostFilterOnDifferentPositionAreAdded() {
        final int[] positions = new int[]{0, 1, 2, 3, 4, 5, 6};
        return anAction(actionForAddingSeveralFilter(positions, POST));
    }

    private static TestAction<Channel<TestMessage>> actionForAddingSeveralFilter(final int[] positions, final FilterPosition pipe) {
        return (channel, testEnvironment) -> {
            final List<Filter<ProcessingContext<TestMessage>>> expectedFilter = addSeveralNoopFilter(channel, positions, pipe);
            testEnvironment.setProperty(EXPECTED_RESULT, expectedFilter);
            testEnvironment.setProperty(PIPE, pipe);
            return null;
        };
    }

    static ChannelActionBuilder theFilterAreQueried() {
        return anAction((channel, testEnvironment) -> {
            final FilterPosition pipe = testEnvironment.getPropertyAsType(PIPE, FilterPosition.class);
            final List<Filter<ProcessingContext<TestMessage>>> filter = getFilterOf(channel, pipe);
            testEnvironment.setProperty(RESULT, filter);
            return null;
        });
    }

    @SuppressWarnings("unchecked")
    static ChannelActionBuilder oneFilterIsRemoved() {
        return anAction((channel, testEnvironment) -> {
            final FilterPosition pipe = testEnvironment.getPropertyAsType(PIPE, FilterPosition.class);
            final List<Filter<ProcessingContext<TestMessage>>> allFilter = (List<Filter<ProcessingContext<TestMessage>>>) testEnvironment.getProperty(EXPECTED_RESULT);
            final Filter<ProcessingContext<TestMessage>> filterToRemove = allFilter.remove(1);
            removeFilter(channel, pipe, filterToRemove);
            return null;
        });
    }

    static ChannelActionBuilder whenTheMetaDataIsModified() {
        return anAction((channel, testEnvironment) -> {
            final String changedMetaDatum = "changed";
            addAFilterChangingMetaData(channel, changedMetaDatum);
            testEnvironment.setProperty(EXPECTED_RESULT, changedMetaDatum);
            sendMessage(channel, messageOfInterest());
            return null;
        });
    }


    public TestAction<Channel<TestMessage>> build() {
        return testAction;
    }
}
