package com.envimate.messageMate.channel;

import com.envimate.messageMate.channel.action.Action;
import com.envimate.messageMate.filtering.Filter;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import com.envimate.messageMate.shared.testMessages.TestMessageOfInterest;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.envimate.messageMate.channel.ChannelTestProperties.MODIFIED_META_DATUM;
import static com.envimate.messageMate.channel.FilterPosition.PROCESS;
import static com.envimate.messageMate.channel.ProcessingContext.processingContext;
import static com.envimate.messageMate.channel.action.Call.callTo;
import static com.envimate.messageMate.shared.testMessages.TestMessageOfInterest.messageOfInterest;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
final class ChannelTestActions {

    final static TestMessageOfInterest DEFAULT_TEST_MESSAGE = messageOfInterest();

    static ProcessingContext<TestMessage> sendMessage(final Channel<TestMessage> channel, final TestMessage testMessage) {
        final ProcessingContext<TestMessage> processingContext = processingContext(testMessage);
        channel.accept(processingContext);
        return processingContext;
    }

    static void addFilterExecutingACall(final Channel<TestMessage> channel, final Channel<TestMessage> targetChannel) {
        channel.addProcessFilter((processingContext, receivers, filterActions) -> {
            callTo(targetChannel, processingContext);
            filterActions.pass(processingContext);
        });
    }

    static void addChangingActionFilterToPipe(final Channel<TestMessage> channel, final FilterPosition filterPosition,
                                              final Action<TestMessage> action) {
        final Filter<ProcessingContext<TestMessage>> filter = (processingContext, receivers, filterActions) -> {
            final ChannelProcessingFrame<TestMessage> currentProcessingFrame = processingContext.getCurrentProcessingFrame();
            currentProcessingFrame.setAction(action);
            filterActions.pass(processingContext);
        };
        addFilterToChannel(channel, filterPosition, filter);
    }

    static void addAFilterChangingMetaData(final Channel<TestMessage> channel, final Object metaDatum) {
        final Filter<ProcessingContext<TestMessage>> filter = (processingContext, receivers, filterActions) -> {
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
            final Filter<ProcessingContext<TestMessage>> filter = addANoopFilterToChannelAtPosition(channel, filterPosition, position);
            expectedFilter.add(position, filter);
        }
        return expectedFilter;
    }

    static Filter<ProcessingContext<TestMessage>> addANoopFilterToChannelAtPosition(final Channel<TestMessage> channel,
                                                                                    final FilterPosition filterPosition,
                                                                                    final int position) {
        final Filter<ProcessingContext<TestMessage>> filter = (processingContext, receivers, filterActions) -> filterActions.pass(processingContext);
        addFilterToChannelAtPosition(channel, filterPosition, filter, position);
        return filter;
    }

    private static void addFilterToChannel(final Channel<TestMessage> channel, final FilterPosition filterPosition,
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

    private static void addFilterToChannelAtPosition(final Channel<TestMessage> channel, final FilterPosition filterPosition,
                                                     final Filter<ProcessingContext<TestMessage>> filter, final int position) {
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

    static List<Filter<ProcessingContext<TestMessage>>> getFilterOf(final Channel<TestMessage> channel, final FilterPosition filterPosition) {
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