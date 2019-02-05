package com.envimate.messageMate.chain;

import com.envimate.messageMate.chain.action.Action;
import com.envimate.messageMate.chain.action.Consume;
import com.envimate.messageMate.chain.action.Jump;
import com.envimate.messageMate.filtering.Filter;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.shared.testMessages.TestMessage;

import java.util.List;

import static com.envimate.messageMate.chain.ChainBuilder.aChain;
import static com.envimate.messageMate.chain.ChainBuilder.aChainWithDefaultAction;
import static com.envimate.messageMate.chain.ChainChannel.*;
import static com.envimate.messageMate.chain.ChainTestActions.*;
import static com.envimate.messageMate.chain.ChainTestProperties.*;
import static com.envimate.messageMate.chain.action.Call.prepareACall;
import static com.envimate.messageMate.chain.action.Consume.consume;
import static com.envimate.messageMate.chain.action.Jump.jumpTo;
import static com.envimate.messageMate.chain.action.Return.aReturn;
import static com.envimate.messageMate.qcec.shared.TestEnvironment.emptyTestEnvironment;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.*;

public final class ChainSetupBuilder {
    private final TestEnvironment testEnvironment;
    private final ChainBuilder<TestMessage> chainBuilder;
    private Chain<TestMessage> alreadyBuiltChain;

    private ChainSetupBuilder() {
        this.testEnvironment = emptyTestEnvironment();
        final Consume<TestMessage> noopConsume = consume(processingContext -> {
        });
        this.chainBuilder = aChain(TestMessage.class)
                .withDefaultAction(noopConsume);
    }

    private ChainSetupBuilder(final TestEnvironment testEnvironment, final Chain<TestMessage> chain) {
        this.testEnvironment = testEnvironment;
        this.chainBuilder = null;
        this.alreadyBuiltChain = chain;
    }

    static ChainSetupBuilder aConfiguredChain() {
        return new ChainSetupBuilder();
    }

    static ChainSetupBuilder threeChainsConnectedWithJumps() {
        final TestEnvironment testEnvironment = emptyTestEnvironment();
        final Chain<TestMessage> thirdChain = aChainWithDefaultAction(consumeAsFinalResult(testEnvironment));
        final Jump<TestMessage> actionSecondChain = jumpTo(thirdChain);
        final Chain<TestMessage> secondChain = aChainWithDefaultAction(actionSecondChain);
        final Jump<TestMessage> actionFirstChain = jumpTo(secondChain);
        final Chain<TestMessage> firstChain = aChainWithDefaultAction(actionFirstChain);

        testEnvironment.addToListProperty(ALL_CHAINS, firstChain);
        testEnvironment.addToListProperty(ALL_CHAINS, secondChain);
        testEnvironment.addToListProperty(ALL_CHAINS, thirdChain);
        return new ChainSetupBuilder(testEnvironment, firstChain);
    }

    static ChainSetupBuilder aChainCallingASecondThatReturnsBack() {
        final TestEnvironment testEnvironment = emptyTestEnvironment();
        final Chain<TestMessage> thirdChain = aChainWithDefaultAction(aReturn());
        final Jump<TestMessage> actionSecondChain = jumpTo(thirdChain);
        final Chain<TestMessage> secondChain = aChainWithDefaultAction(actionSecondChain);
        final Chain<TestMessage> firstChain = aChainWithDefaultAction(consumeAsFinalResult(testEnvironment));

        testEnvironment.setProperty(CALL_TARGET_CHAIN, secondChain);
        testEnvironment.setProperty(RETURNING_CHAIN, thirdChain);
        return new ChainSetupBuilder(testEnvironment, firstChain);
    }

    static ChainSetupBuilder aChainSetupWithNestedCalls() {
        final TestEnvironment testEnvironment = emptyTestEnvironment();
        final Chain<TestMessage> initialChain = aChainWithDefaultAction(consumeAsFinalResult(testEnvironment));
        final Chain<TestMessage> firstCallTargetChain = aChainWithDefaultAction(aReturn());
        testEnvironment.addToListProperty(CALL_TARGET_CHAIN, firstCallTargetChain);

        final Chain<TestMessage> returnChainAfterSecondCall = aChainWithDefaultAction(aReturn());
        final Chain<TestMessage> secondCallTargetChain = aChainWithDefaultAction(jumpTo(returnChainAfterSecondCall));
        testEnvironment.addToListProperty(CALL_TARGET_CHAIN, secondCallTargetChain);
        testEnvironment.setProperty(RETURNING_CHAIN, returnChainAfterSecondCall);

        addFilterExecutingACall(initialChain, firstCallTargetChain);
        addFilterExecutingACall(firstCallTargetChain, secondCallTargetChain);
        return new ChainSetupBuilder(testEnvironment, initialChain);
    }

    private static Consume<TestMessage> consumeAsFinalResult(final TestEnvironment testEnvironment) {
        return consume(processingContext -> testEnvironment.setProperty(RESULT, processingContext));
    }

    ChainSetupBuilder withDefaultActionConsume() {
        chainBuilder.withDefaultAction(consumeAsFinalResult(testEnvironment));
        return this;
    }

    ChainSetupBuilder withDefaultActionJumpToDifferentChain() {
        final Chain<TestMessage> secondChain = aChainWithDefaultAction(consumeAsFinalResult(testEnvironment));
        chainBuilder.withDefaultAction(jumpTo(secondChain));
        return this;
    }

    ChainSetupBuilder withDefaultActionReturn() {
        chainBuilder.withDefaultAction(aReturn());
        return this;
    }

    ChainSetupBuilder withDefaultActionCall() {
        chainBuilder.withDefaultAction(prepareACall(null));
        return this;
    }

    ChainSetupBuilder withAnUnknownAction() {
        final Action<TestMessage> unknownAction = UnknownAction.unknownAction();
        chainBuilder.withDefaultAction(unknownAction);
        return this;
    }

    ChainSetupBuilder withAPreFilterThatChangesTheAction() {
        final Action<TestMessage> unknownAction = UnknownAction.unknownAction();
        alreadyBuiltChain = chainBuilder.withDefaultAction(unknownAction)
                .build();
        addChangingActionFilterToChannel(alreadyBuiltChain, PRE, consumeAsFinalResult(testEnvironment));
        return this;
    }

    ChainSetupBuilder withAProcessFilterThatChangesTheAction() {
        final Action<TestMessage> unknownAction = UnknownAction.unknownAction();
        alreadyBuiltChain = chainBuilder.withDefaultAction(unknownAction)
                .build();
        addChangingActionFilterToChannel(alreadyBuiltChain, PROCESS, consumeAsFinalResult(testEnvironment));
        return this;
    }

    ChainSetupBuilder withAPostFilterThatChangesTheAction() {
        final Action<TestMessage> unknownAction = UnknownAction.unknownAction();
        alreadyBuiltChain = chainBuilder.withDefaultAction(unknownAction)
                .build();
        addChangingActionFilterToChannel(alreadyBuiltChain, POST, consumeAsFinalResult(testEnvironment));
        return this;
    }

    ChainSetupBuilder withSeveralFilterInThePreChannel() {
        final int[] positions = new int[]{0, 0, 2, 1, 2, 4};
        severalFilterInChannel(positions, PRE);
        return this;
    }

    ChainSetupBuilder withSeveralFilterInTheProcessChannel() {
        final int[] positions = new int[]{0, 1, 1, 3, 0, 5};
        severalFilterInChannel(positions, PROCESS);
        return this;
    }

    ChainSetupBuilder withSeveralFilterInThePostChannel() {
        final int[] positions = new int[]{0, 0, 2, 2, 1, 3};
        severalFilterInChannel(positions, POST);
        return this;
    }

    private void severalFilterInChannel(final int[] positions, final ChainChannel channel) {
        final Action<TestMessage> unknownAction = UnknownAction.unknownAction();
        alreadyBuiltChain = chainBuilder.withDefaultAction(unknownAction)
                .build();
        final List<Filter<ProcessingContext<TestMessage>>> expectedFilter = addSeveralNoopFilter(alreadyBuiltChain, positions, channel);
        testEnvironment.setProperty(EXPECTED_RESULT, expectedFilter);
        testEnvironment.setProperty(CHANNEL, channel);
    }

    public TestEnvironment build() {
        if (alreadyBuiltChain != null) {
            testEnvironment.setProperty(SUT, alreadyBuiltChain);
        } else {
            final Chain<TestMessage> chain = chainBuilder.build();
            testEnvironment.setProperty(SUT, chain);
        }
        return testEnvironment;
    }

    private static class UnknownAction implements Action<TestMessage> {
        private static UnknownAction unknownAction() {
            return new UnknownAction();
        }
    }

}
