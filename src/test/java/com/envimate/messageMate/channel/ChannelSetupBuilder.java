package com.envimate.messageMate.channel;

import com.envimate.messageMate.channel.action.Action;
import com.envimate.messageMate.channel.action.Consume;
import com.envimate.messageMate.channel.action.Jump;
import com.envimate.messageMate.filtering.Filter;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.shared.testMessages.TestMessage;

import java.util.List;

import static com.envimate.messageMate.channel.ChannelBuilder.aChannel;
import static com.envimate.messageMate.channel.ChannelBuilder.aChannelWithDefaultAction;
import static com.envimate.messageMate.channel.ChannelPipe.*;
import static com.envimate.messageMate.channel.ChannelTestActions.*;
import static com.envimate.messageMate.channel.ChannelTestProperties.*;
import static com.envimate.messageMate.channel.action.Call.prepareACall;
import static com.envimate.messageMate.channel.action.Consume.consume;
import static com.envimate.messageMate.channel.action.Jump.jumpTo;
import static com.envimate.messageMate.channel.action.Return.aReturn;
import static com.envimate.messageMate.qcec.shared.TestEnvironment.emptyTestEnvironment;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.*;

public final class ChannelSetupBuilder {
    private final TestEnvironment testEnvironment;
    private final ChannelBuilder<TestMessage> channelBuilder;
    private Channel<TestMessage> alreadyBuiltChannel;

    private ChannelSetupBuilder() {
        this.testEnvironment = emptyTestEnvironment();
        final Consume<TestMessage> noopConsume = consume(processingContext -> {
        });
        this.channelBuilder = aChannel(TestMessage.class)
                .withDefaultAction(noopConsume);
    }

    private ChannelSetupBuilder(final TestEnvironment testEnvironment, final Channel<TestMessage> channel) {
        this.testEnvironment = testEnvironment;
        this.channelBuilder = null;
        this.alreadyBuiltChannel = channel;
    }

    static ChannelSetupBuilder aConfiguredChannel() {
        return new ChannelSetupBuilder();
    }

    static ChannelSetupBuilder threeChannelsConnectedWithJumps() {
        final TestEnvironment testEnvironment = emptyTestEnvironment();
        final Channel<TestMessage> thirdChannel = aChannelWithDefaultAction(consumeAsFinalResult(testEnvironment));
        final Jump<TestMessage> actionSecondChannel = jumpTo(thirdChannel);
        final Channel<TestMessage> secondChannel = aChannelWithDefaultAction(actionSecondChannel);
        final Jump<TestMessage> actionFirstChannel = jumpTo(secondChannel);
        final Channel<TestMessage> firstChannel = aChannelWithDefaultAction(actionFirstChannel);

        testEnvironment.addToListProperty(ALL_CHANNELS, firstChannel);
        testEnvironment.addToListProperty(ALL_CHANNELS, secondChannel);
        testEnvironment.addToListProperty(ALL_CHANNELS, thirdChannel);
        return new ChannelSetupBuilder(testEnvironment, firstChannel);
    }

    static ChannelSetupBuilder aChannelCallingASecondThatReturnsBack() {
        final TestEnvironment testEnvironment = emptyTestEnvironment();
        final Channel<TestMessage> thirdChannel = aChannelWithDefaultAction(aReturn());
        final Jump<TestMessage> actionSecondChannel = jumpTo(thirdChannel);
        final Channel<TestMessage> secondChannel = aChannelWithDefaultAction(actionSecondChannel);
        final Channel<TestMessage> firstChannel = aChannelWithDefaultAction(consumeAsFinalResult(testEnvironment));

        testEnvironment.setProperty(CALL_TARGET_CHANNEL, secondChannel);
        testEnvironment.setProperty(RETURNING_CHANNEL, thirdChannel);
        return new ChannelSetupBuilder(testEnvironment, firstChannel);
    }

    static ChannelSetupBuilder aChannelSetupWithNestedCalls() {
        final TestEnvironment testEnvironment = emptyTestEnvironment();
        final Channel<TestMessage> initialChannel = aChannelWithDefaultAction(consumeAsFinalResult(testEnvironment));
        final Channel<TestMessage> firstCallTargetChannel = aChannelWithDefaultAction(aReturn());
        testEnvironment.addToListProperty(CALL_TARGET_CHANNEL, firstCallTargetChannel);

        final Channel<TestMessage> returnChannelAfterSecondCall = aChannelWithDefaultAction(aReturn());
        final Channel<TestMessage> secondCallTargetChannel = aChannelWithDefaultAction(jumpTo(returnChannelAfterSecondCall));
        testEnvironment.addToListProperty(CALL_TARGET_CHANNEL, secondCallTargetChannel);
        testEnvironment.setProperty(RETURNING_CHANNEL, returnChannelAfterSecondCall);

        addFilterExecutingACall(initialChannel, firstCallTargetChannel);
        addFilterExecutingACall(firstCallTargetChannel, secondCallTargetChannel);
        return new ChannelSetupBuilder(testEnvironment, initialChannel);
    }

    private static Consume<TestMessage> consumeAsFinalResult(final TestEnvironment testEnvironment) {
        return consume(processingContext -> testEnvironment.setProperty(RESULT, processingContext));
    }

    ChannelSetupBuilder withDefaultActionConsume() {
        channelBuilder.withDefaultAction(consumeAsFinalResult(testEnvironment));
        return this;
    }

    ChannelSetupBuilder withDefaultActionJumpToDifferentChannel() {
        final Channel<TestMessage> secondChannel = aChannelWithDefaultAction(consumeAsFinalResult(testEnvironment));
        channelBuilder.withDefaultAction(jumpTo(secondChannel));
        return this;
    }

    ChannelSetupBuilder withDefaultActionReturn() {
        channelBuilder.withDefaultAction(aReturn());
        return this;
    }

    ChannelSetupBuilder withDefaultActionCall() {
        channelBuilder.withDefaultAction(prepareACall(null));
        return this;
    }

    ChannelSetupBuilder withAnUnknownAction() {
        final Action<TestMessage> unknownAction = UnknownAction.unknownAction();
        channelBuilder.withDefaultAction(unknownAction);
        return this;
    }

    ChannelSetupBuilder withAPreFilterThatChangesTheAction() {
        final Action<TestMessage> unknownAction = UnknownAction.unknownAction();
        alreadyBuiltChannel = channelBuilder.withDefaultAction(unknownAction)
                .build();
        addChangingActionFilterToPipe(alreadyBuiltChannel, PRE, consumeAsFinalResult(testEnvironment));
        return this;
    }

    ChannelSetupBuilder withAProcessFilterThatChangesTheAction() {
        final Action<TestMessage> unknownAction = UnknownAction.unknownAction();
        alreadyBuiltChannel = channelBuilder.withDefaultAction(unknownAction)
                .build();
        addChangingActionFilterToPipe(alreadyBuiltChannel, PROCESS, consumeAsFinalResult(testEnvironment));
        return this;
    }

    ChannelSetupBuilder withAPostFilterThatChangesTheAction() {
        final Action<TestMessage> unknownAction = UnknownAction.unknownAction();
        alreadyBuiltChannel = channelBuilder.withDefaultAction(unknownAction)
                .build();
        addChangingActionFilterToPipe(alreadyBuiltChannel, POST, consumeAsFinalResult(testEnvironment));
        return this;
    }

    ChannelSetupBuilder withSeveralFilterInThePrePipe() {
        final int[] positions = new int[]{0, 0, 2, 1, 2, 4};
        severalFilterInPipe(positions, PRE);
        return this;
    }

    ChannelSetupBuilder withSeveralFilterInTheProcessPipe() {
        final int[] positions = new int[]{0, 1, 1, 3, 0, 5};
        severalFilterInPipe(positions, PROCESS);
        return this;
    }

    ChannelSetupBuilder withSeveralFilterInThePostPipe() {
        final int[] positions = new int[]{0, 0, 2, 2, 1, 3};
        severalFilterInPipe(positions, POST);
        return this;
    }

    private void severalFilterInPipe(final int[] positions, final ChannelPipe pipe) {
        final Action<TestMessage> unknownAction = UnknownAction.unknownAction();
        alreadyBuiltChannel = channelBuilder.withDefaultAction(unknownAction)
                .build();
        final List<Filter<ProcessingContext<TestMessage>>> expectedFilter = addSeveralNoopFilter(alreadyBuiltChannel, positions, pipe);
        testEnvironment.setProperty(EXPECTED_RESULT, expectedFilter);
        testEnvironment.setProperty(PIPE, pipe);
    }

    public TestEnvironment build() {
        if (alreadyBuiltChannel != null) {
            testEnvironment.setProperty(SUT, alreadyBuiltChannel);
        } else {
            final Channel<TestMessage> channel = channelBuilder.build();
            testEnvironment.setProperty(SUT, channel);
        }
        return testEnvironment;
    }

    private static class UnknownAction implements Action<TestMessage> {
        private static UnknownAction unknownAction() {
            return new UnknownAction();
        }
    }

}
