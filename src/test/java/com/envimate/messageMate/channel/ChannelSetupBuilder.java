package com.envimate.messageMate.channel;

import com.envimate.messageMate.channel.action.Action;
import com.envimate.messageMate.channel.action.Consume;
import com.envimate.messageMate.channel.action.Jump;
import com.envimate.messageMate.filtering.Filter;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import com.envimate.messageMate.shared.testMessages.TestMessageOfInterest;

import java.util.List;

import static com.envimate.messageMate.channel.ChannelBuilder.aChannel;
import static com.envimate.messageMate.channel.ChannelBuilder.aChannelWithDefaultAction;
import static com.envimate.messageMate.channel.ChannelTestActions.*;
import static com.envimate.messageMate.channel.ChannelTestProperties.*;
import static com.envimate.messageMate.channel.FilterPosition.*;
import static com.envimate.messageMate.channel.ProcessingContext.processingContext;
import static com.envimate.messageMate.channel.action.Call.prepareACall;
import static com.envimate.messageMate.channel.action.Consume.consume;
import static com.envimate.messageMate.channel.action.Jump.jumpTo;
import static com.envimate.messageMate.channel.action.Return.aReturn;
import static com.envimate.messageMate.qcec.shared.TestEnvironment.emptyTestEnvironment;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.*;
import static com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.TestFilter.aMessageDroppingFilter;
import static com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.TestFilter.aMessageFilterThatDoesNotCallAnyMethod;
import static com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.TestFilter.aMessageReplacingFilter;

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

    ChannelSetupBuilder withAPreFilterThatReplacesTheMessage() {
        addAFilterThatReplacesTheMessage(PRE);
        return this;
    }

    ChannelSetupBuilder withAProcessFilterThatReplacesTheMessage() {
        addAFilterThatReplacesTheMessage(PROCESS);
        return this;
    }

    ChannelSetupBuilder withAPostFilterThatReplacesTheMessage() {
        addAFilterThatReplacesTheMessage(POST);
        return this;
    }

    ChannelSetupBuilder withAPreFilterThatBlocksMessages() {
        addFilterThatBlocksMessages(PRE);
        return this;
    }

    ChannelSetupBuilder withAProcessFilterThatBlocksMessages() {
        addFilterThatBlocksMessages(PROCESS);
        return this;
    }

    ChannelSetupBuilder withAPostFilterThatBlocksMessages() {
        addFilterThatBlocksMessages(POST);
        return this;
    }

    private void addAFilterThatReplacesTheMessage(final FilterPosition filterPosition) {
        alreadyBuiltChannel = channelBuilder.withDefaultAction(consumeAsFinalResult(testEnvironment))
                .build();
        final TestMessageOfInterest replacedMessage = TestMessageOfInterest.messageOfInterest();
        final ProcessingContext<TestMessage> replacement = processingContext(replacedMessage);
        testEnvironment.setProperty(REPLACED_MESSAGE, replacement);
        final Filter<ProcessingContext<TestMessage>> filter = aMessageReplacingFilter(replacement);
        addAFilterToPipe(alreadyBuiltChannel, filterPosition, filter);
    }

    private void addFilterThatBlocksMessages(final FilterPosition filterPosition) {
        alreadyBuiltChannel = channelBuilder.withDefaultAction(consumeAsFinalResult(testEnvironment))
                .build();
        final Filter<ProcessingContext<TestMessage>> filter = aMessageDroppingFilter();
        addAFilterToPipe(alreadyBuiltChannel, filterPosition, filter);
    }

    ChannelSetupBuilder withAPreFilterThatForgetsMessages() {
        addFilterThatForgetsMessages(PRE);
        return this;
    }

    ChannelSetupBuilder withAProcessFilterThatForgetsMessages() {
        addFilterThatForgetsMessages(PROCESS);
        return this;
    }

    ChannelSetupBuilder withAPostFilterThatForgetsMessages() {
        addFilterThatForgetsMessages(POST);
        return this;
    }

    private void addFilterThatForgetsMessages(final FilterPosition filterPosition) {
        alreadyBuiltChannel = channelBuilder.withDefaultAction(consumeAsFinalResult(testEnvironment))
                .build();
        final Filter<ProcessingContext<TestMessage>> filter = aMessageFilterThatDoesNotCallAnyMethod();
        addAFilterToPipe(alreadyBuiltChannel, filterPosition, filter);
    }

    private void addAFilterToPipe(final Channel<TestMessage> channel, final FilterPosition filterPosition, final Filter<ProcessingContext<TestMessage>> filter) {
        switch (filterPosition) {
            case PRE:
                channel.addPreFilter(filter);
                break;
            case PROCESS:
                channel.addPostFilter(filter);
                break;
            case POST:
                channel.addPostFilter(filter);
                break;
            default:
                throw new IllegalArgumentException();
        }
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

    ChannelSetupBuilder withSeveralPreFilter() {
        final int[] positions = new int[]{0, 0, 2, 1, 2, 4};
        severalFilterInPipe(positions, PRE);
        return this;
    }

    ChannelSetupBuilder withSeveralProcessFilter() {
        final int[] positions = new int[]{0, 1, 1, 3, 0, 5};
        severalFilterInPipe(positions, PROCESS);
        return this;
    }

    ChannelSetupBuilder withSeveralPostFilter() {
        final int[] positions = new int[]{0, 0, 2, 2, 1, 3};
        severalFilterInPipe(positions, POST);
        return this;
    }

    ChannelSetupBuilder withAPreFilterAtAnInvalidPosition(final int position) {
        addAFilterAtPosition(position, PRE);
        return this;
    }

    ChannelSetupBuilder withAProcessFilterAtAnInvalidPosition(final int position) {
        addAFilterAtPosition(position, PROCESS);
        return this;
    }

    ChannelSetupBuilder withAPostFilterAtAnInvalidPosition(final int position) {
        addAFilterAtPosition(position, POST);
        return this;
    }

    private void addAFilterAtPosition(final int position, final FilterPosition filterPosition) {
        try {
            alreadyBuiltChannel = channelBuilder.withDefaultAction(consumeAsFinalResult(testEnvironment))
                    .build();
            addANoopFilterToChannelAtPosition(alreadyBuiltChannel, filterPosition, position);
        } catch (final Exception e) {
            testEnvironment.setProperty(EXCEPTION, e);
        }
    }

    private void severalFilterInPipe(final int[] positions, final FilterPosition pipe) {
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
