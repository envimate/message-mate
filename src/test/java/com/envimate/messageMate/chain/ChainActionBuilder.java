package com.envimate.messageMate.chain;

import com.envimate.messageMate.filtering.Filter;
import com.envimate.messageMate.qcec.shared.TestAction;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.envimate.messageMate.chain.ChainChannel.*;
import static com.envimate.messageMate.chain.ChainTestActions.*;
import static com.envimate.messageMate.chain.ChainTestProperties.CALL_TARGET_CHAIN;
import static com.envimate.messageMate.chain.ChainTestProperties.CHANNEL;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.EXPECTED_RESULT;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.RESULT;
import static com.envimate.messageMate.shared.testMessages.TestMessageOfInterest.messageOfInterest;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class ChainActionBuilder {
    private final TestAction<Chain<TestMessage>> testAction;

    private static ChainActionBuilder anAction(final TestAction<Chain<TestMessage>> testAction) {
        return new ChainActionBuilder(testAction);
    }

    static ChainActionBuilder aMessageIsSend() {
        return anAction((chain, testEnvironment) -> {
            final ProcessingContext<TestMessage> sendProcessingFrame = sendMessage(chain, DEFAULT_TEST_MESSAGE);
            testEnvironment.setProperty(EXPECTED_RESULT, sendProcessingFrame);
            return null;
        });
    }

    @SuppressWarnings("unchecked")
    static ChainActionBuilder aCallToTheSecondChainIsExecuted() {
        return anAction((chain, testEnvironment) -> {
            final Chain<TestMessage> callTargetChain = (Chain<TestMessage>) testEnvironment.getProperty(CALL_TARGET_CHAIN);
            addFilterExecutingACall(chain, callTargetChain);

            final ProcessingContext<TestMessage> sendProcessingFrame = sendMessage(chain, DEFAULT_TEST_MESSAGE);
            testEnvironment.setProperty(EXPECTED_RESULT, sendProcessingFrame);
            return null;
        });
    }

    static ChainActionBuilder severalFilterOnDifferentPositionAreAddedInPreChannel() {
        final int[] positions = new int[]{0, 1, 0, 0, 3, 2};
        return anAction(actionForAddingSeveralFilter(positions, PRE));
    }

    static ChainActionBuilder severalFilterOnDifferentPositionAreAddedInProcessChannel() {
        final int[] positions = new int[]{0, 0, 1, 0, 2, 4, 4};
        return anAction(actionForAddingSeveralFilter(positions, PROCESS));
    }

    static ChainActionBuilder severalFilterOnDifferentPositionAreAddedInPostChannel() {
        final int[] positions = new int[]{0, 1, 2, 3, 4, 5, 6};
        return anAction(actionForAddingSeveralFilter(positions, POST));
    }

    private static TestAction<Chain<TestMessage>> actionForAddingSeveralFilter(final int[] positions, final ChainChannel channel) {
        return (chain, testEnvironment) -> {
            final List<Filter<ProcessingContext<TestMessage>>> expectedFilter = addSeveralNoopFilter(chain, positions, channel);
            testEnvironment.setProperty(EXPECTED_RESULT, expectedFilter);
            testEnvironment.setProperty(CHANNEL, channel);
            return null;
        };
    }

    static ChainActionBuilder whenTheFilterAreQueried() {
        return anAction((chain, testEnvironment) -> {
            final ChainChannel channel = testEnvironment.getPropertyAsType(CHANNEL, ChainChannel.class);
            final List<Filter<ProcessingContext<TestMessage>>> filter = getFilterOf(chain, channel);
            testEnvironment.setProperty(RESULT, filter);
            return null;
        });
    }

    @SuppressWarnings("unchecked")
    static ChainActionBuilder whenOneFilterIsRemoved() {
        return anAction((chain, testEnvironment) -> {
            final ChainChannel channel = testEnvironment.getPropertyAsType(CHANNEL, ChainChannel.class);
            final List<Filter<ProcessingContext<TestMessage>>> allFilter = (List<Filter<ProcessingContext<TestMessage>>>) testEnvironment.getProperty(EXPECTED_RESULT);
            final Filter<ProcessingContext<TestMessage>> filterToRemove = allFilter.remove(1);
            removeFilter(chain, channel, filterToRemove);
            return null;
        });
    }

    static ChainActionBuilder whenTheMetaDataIsModified() {
        return anAction((chain, testEnvironment) -> {
            final String changedMetaDatum = "changed";
            addAFilterChainingMetaData(chain, changedMetaDatum);
            testEnvironment.setProperty(EXPECTED_RESULT, changedMetaDatum);
            sendMessage(chain, messageOfInterest());
            return null;
        });
    }


    public TestAction<Chain<TestMessage>> build() {
        return testAction;
    }
}
