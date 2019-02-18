package com.envimate.messageMate.chain;

import com.envimate.messageMate.chain.action.Action;
import com.envimate.messageMate.filtering.Filter;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import com.envimate.messageMate.shared.testMessages.TestMessageOfInterest;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.envimate.messageMate.chain.ChainChannel.PROCESS;
import static com.envimate.messageMate.chain.ChainTestProperties.MODIFIED_META_DATUM;
import static com.envimate.messageMate.chain.ProcessingContext.processingContext;
import static com.envimate.messageMate.chain.action.Call.callTo;
import static com.envimate.messageMate.shared.testMessages.TestMessageOfInterest.messageOfInterest;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
final class ChainTestActions {

    final static TestMessageOfInterest DEFAULT_TEST_MESSAGE = messageOfInterest();

    static ProcessingContext<TestMessage> sendMessage(final Chain<TestMessage> chain, final TestMessage testMessage) {
        final ProcessingContext<TestMessage> processingContext = processingContext(testMessage);
        chain.accept(processingContext);
        return processingContext;
    }

    static void addFilterExecutingACall(final Chain<TestMessage> chain, final Chain<TestMessage> targetChain) {
        chain.addProcessFilter((processingContext, receivers, filterActions) -> {
            callTo(targetChain, processingContext);
            filterActions.pass(processingContext);
        });
    }

    static void addChangingActionFilterToChannel(final Chain<TestMessage> chain, final ChainChannel channel,
                                                 final Action<TestMessage> action) {
        final Filter<ProcessingContext<TestMessage>> filter = (processingContext, receivers, filterActions) -> {
            final ChainProcessingFrame<TestMessage> currentProcessingFrame = processingContext.getCurrentProcessingFrame();
            currentProcessingFrame.setAction(action);
            filterActions.pass(processingContext);
        };
        addFilterToChain(chain, channel, filter);
    }

    static void addAFilterChainingMetaData(final Chain<TestMessage> chain, final Object metaDatum) {
        final Filter<ProcessingContext<TestMessage>> filter = (processingContext, receivers, filterActions) -> {
            final Map<Object, Object> metaData = processingContext.getContextMetaData();
            metaData.put(MODIFIED_META_DATUM, metaDatum);
            filterActions.pass(processingContext);
        };
        addFilterToChain(chain, PROCESS, filter);
    }

    static List<Filter<ProcessingContext<TestMessage>>> addSeveralNoopFilter(final Chain<TestMessage> chain,
                                                                             final int[] positions,
                                                                             final ChainChannel channel) {
        final List<Filter<ProcessingContext<TestMessage>>> expectedFilter = new LinkedList<>();
        for (final int position : positions) {
            final Filter<ProcessingContext<TestMessage>> filter = addANoopFilterToChainAtPosition(chain, channel, position);
            expectedFilter.add(position, filter);
        }
        return expectedFilter;
    }

    private static Filter<ProcessingContext<TestMessage>> addANoopFilterToChainAtPosition(final Chain<TestMessage> chain,
                                                                                          final ChainChannel channel,
                                                                                          final int position) {
        final Filter<ProcessingContext<TestMessage>> filter = (processingContext, receivers, filterActions) -> filterActions.pass(processingContext);
        addFilterToChainAtPosition(chain, channel, filter, position);
        return filter;
    }

    private static void addFilterToChain(final Chain<TestMessage> chain, final ChainChannel channel,
                                         final Filter<ProcessingContext<TestMessage>> filter) {
        switch (channel) {
            case PRE:
                chain.addPreFilter(filter);
                break;
            case PROCESS:
                chain.addProcessFilter(filter);
                break;
            case POST:
                chain.addPostFilter(filter);
                break;
            default:
                throw new UnsupportedOperationException("Unknown pipe " + channel + ".");
        }
    }

    private static void addFilterToChainAtPosition(final Chain<TestMessage> chain, final ChainChannel channel,
                                                   final Filter<ProcessingContext<TestMessage>> filter, final int position) {
        switch (channel) {
            case PRE:
                chain.addPreFilter(filter, position);
                break;
            case PROCESS:
                chain.addProcessFilter(filter, position);
                break;
            case POST:
                chain.addPostFilter(filter, position);
                break;
            default:
                throw new UnsupportedOperationException("Unknown pipe " + channel + ".");
        }
    }

    static List<Filter<ProcessingContext<TestMessage>>> getFilterOf(final Chain<TestMessage> chain, final ChainChannel channel) {
        switch (channel) {
            case PRE:
                return chain.getPreFilter();
            case PROCESS:
                return chain.getProcessFilter();
            case POST:
                return chain.getPostFilter();
            default:
                throw new UnsupportedOperationException("Unknown pipe " + channel + ".");
        }
    }

    static void removeFilter(final Chain<TestMessage> chain,
                             final ChainChannel channel,
                             final Filter<ProcessingContext<TestMessage>> filter) {
        switch (channel) {
            case PRE:
                chain.removePreFilter(filter);
                break;
            case PROCESS:
                chain.removeProcessFilter(filter);
                break;
            case POST:
                chain.removePostFilter(filter);
                break;
            default:
                throw new UnsupportedOperationException("Unknown pipe " + channel + ".");
        }
    }
}
