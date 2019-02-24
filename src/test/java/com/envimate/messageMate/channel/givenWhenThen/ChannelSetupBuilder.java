package com.envimate.messageMate.channel.givenWhenThen;

import com.envimate.messageMate.channel.Channel;
import com.envimate.messageMate.channel.ChannelBuilder;
import com.envimate.messageMate.channel.ChannelType;
import com.envimate.messageMate.channel.ProcessingContext;
import com.envimate.messageMate.channel.action.Action;
import com.envimate.messageMate.channel.action.Consume;
import com.envimate.messageMate.channel.action.Jump;
import com.envimate.messageMate.channel.action.Subscription;
import com.envimate.messageMate.channel.config.ChannelTestConfig;
import com.envimate.messageMate.filtering.Filter;
import com.envimate.messageMate.pipe.configuration.AsynchronousConfiguration;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.shared.subscriber.BlockingTestSubscriber;
import com.envimate.messageMate.shared.subscriber.TestException;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import com.envimate.messageMate.shared.testMessages.TestMessageOfInterest;

import java.util.List;
import java.util.concurrent.Semaphore;

import static com.envimate.messageMate.channel.ChannelBuilder.aChannel;
import static com.envimate.messageMate.channel.ChannelBuilder.aChannelWithDefaultAction;
import static com.envimate.messageMate.channel.ProcessingContext.processingContext;
import static com.envimate.messageMate.channel.action.Call.prepareACall;
import static com.envimate.messageMate.channel.action.Consume.consume;
import static com.envimate.messageMate.channel.action.Jump.jumpTo;
import static com.envimate.messageMate.channel.action.Return.aReturn;
import static com.envimate.messageMate.channel.action.Subscription.subscription;
import static com.envimate.messageMate.channel.givenWhenThen.ChannelTestActions.*;
import static com.envimate.messageMate.channel.givenWhenThen.ChannelTestProperties.*;
import static com.envimate.messageMate.channel.givenWhenThen.FilterPosition.*;
import static com.envimate.messageMate.channel.givenWhenThen.TestChannelErrorHandler.*;
import static com.envimate.messageMate.qcec.shared.TestEnvironment.emptyTestEnvironment;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.*;
import static com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.TestFilter.*;
import static com.envimate.messageMate.shared.subscriber.ErrorThrowingTestSubscriber.errorThrowingTestSubscriber;
import static com.envimate.messageMate.shared.subscriber.SimpleTestSubscriber.deliveryPreemptingSubscriber;

public final class ChannelSetupBuilder {
    private final TestEnvironment testEnvironment;
    private final ChannelBuilder<TestMessage> channelBuilder;
    private final ChannelTestConfig channelTestConfig;
    private Channel<TestMessage> alreadyBuiltChannel;

    private ChannelSetupBuilder(final ChannelTestConfig channelTestConfig) {
        this.channelTestConfig = channelTestConfig;
        this.testEnvironment = emptyTestEnvironment();
        final Consume<TestMessage> noopConsume = consume(processingContext -> {
        });
        final ChannelType type = channelTestConfig.getType();
        final AsynchronousConfiguration asynchronousConfiguration = channelTestConfig.getAsynchronousConfiguration();
        this.channelBuilder = aChannel(TestMessage.class)
                .forType(type)
                .withAsynchronousConfiguration(asynchronousConfiguration)
                .withDefaultAction(noopConsume);
        storeSleepTimesInTestEnvironment(channelTestConfig, testEnvironment);
    }

    private ChannelSetupBuilder(final TestEnvironment testEnvironment, final Channel<TestMessage> channel, final ChannelTestConfig channelTestConfig) {
        this.testEnvironment = testEnvironment;
        this.channelTestConfig = channelTestConfig;
        this.channelBuilder = null;
        this.alreadyBuiltChannel = channel;
        final long millisecondsSleepAfterExecution = channelTestConfig.getMillisecondsSleepAfterExecution();
        if (millisecondsSleepAfterExecution > 0) {
            testEnvironment.setProperty(SLEEP_AFTER_EXECUTION, millisecondsSleepAfterExecution);
        }
    }

    public static ChannelSetupBuilder aConfiguredChannel(final ChannelTestConfig channelTestConfig) {
        return new ChannelSetupBuilder(channelTestConfig);
    }

    public static ChannelSetupBuilder threeChannelsConnectedWithJumps(final ChannelTestConfig channelTestConfig) {
        final TestEnvironment testEnvironment = emptyTestEnvironment();
        final Channel<TestMessage> thirdChannel = aChannelWithDefaultAction(consumeAsFinalResult(testEnvironment));
        final Jump<TestMessage> actionSecondChannel = jumpTo(thirdChannel);
        final Channel<TestMessage> secondChannel = aChannelWithDefaultAction(actionSecondChannel);
        final Jump<TestMessage> actionFirstChannel = jumpTo(secondChannel);
        final Channel<TestMessage> firstChannel = aChannel(TestMessage.class)
                .withDefaultAction(actionFirstChannel)
                .forType(channelTestConfig.getType())
                .withAsynchronousConfiguration(channelTestConfig.getAsynchronousConfiguration())
                .build();

        testEnvironment.addToListProperty(ALL_CHANNELS, firstChannel);
        testEnvironment.addToListProperty(ALL_CHANNELS, secondChannel);
        testEnvironment.addToListProperty(ALL_CHANNELS, thirdChannel);
        return new ChannelSetupBuilder(testEnvironment, firstChannel, channelTestConfig);
    }

    public static ChannelSetupBuilder aChannelCallingASecondThatReturnsBack(final ChannelTestConfig channelTestConfig) {
        final TestEnvironment testEnvironment = emptyTestEnvironment();
        final Channel<TestMessage> thirdChannel = aChannelWithDefaultAction(aReturn());
        final Jump<TestMessage> actionSecondChannel = jumpTo(thirdChannel);
        final Channel<TestMessage> secondChannel = aChannelWithDefaultAction(actionSecondChannel);
        final Channel<TestMessage> firstChannel = aChannel(TestMessage.class)
                .withDefaultAction(consumeAsFinalResult(testEnvironment))
                .forType(channelTestConfig.getType())
                .withAsynchronousConfiguration(channelTestConfig.getAsynchronousConfiguration())
                .build();

        testEnvironment.setProperty(CALL_TARGET_CHANNEL, secondChannel);
        testEnvironment.setProperty(RETURNING_CHANNEL, thirdChannel);
        return new ChannelSetupBuilder(testEnvironment, firstChannel, channelTestConfig);
    }

    public static ChannelSetupBuilder aChannelSetupWithNestedCalls(final ChannelTestConfig channelTestConfig) {
        final TestEnvironment testEnvironment = emptyTestEnvironment();
        final Channel<TestMessage> initialChannel = aChannel(TestMessage.class)
                .withDefaultAction(consumeAsFinalResult(testEnvironment))
                .forType(channelTestConfig.getType())
                .withAsynchronousConfiguration(channelTestConfig.getAsynchronousConfiguration())
                .build();
        final Channel<TestMessage> firstCallTargetChannel = aChannelWithDefaultAction(aReturn());
        testEnvironment.addToListProperty(CALL_TARGET_CHANNEL, firstCallTargetChannel);

        final Channel<TestMessage> returnChannelAfterSecondCall = aChannelWithDefaultAction(aReturn());
        final Channel<TestMessage> secondCallTargetChannel = aChannelWithDefaultAction(jumpTo(returnChannelAfterSecondCall));
        testEnvironment.addToListProperty(CALL_TARGET_CHANNEL, secondCallTargetChannel);
        testEnvironment.setProperty(RETURNING_CHANNEL, returnChannelAfterSecondCall);

        addFilterExecutingACall(initialChannel, firstCallTargetChannel);
        addFilterExecutingACall(firstCallTargetChannel, secondCallTargetChannel);
        return new ChannelSetupBuilder(testEnvironment, initialChannel, channelTestConfig);
    }

    private static Consume<TestMessage> consumeAsFinalResult(final TestEnvironment testEnvironment) {
        return consume(processingContext -> testEnvironment.setProperty(RESULT, processingContext));
    }

    private void storeSleepTimesInTestEnvironment(final ChannelTestConfig channelTestConfig, final TestEnvironment testEnvironment) {
        final long millisecondsSleepAfterExecution = channelTestConfig.getMillisecondsSleepAfterExecution();
        if (millisecondsSleepAfterExecution > 0) {
            testEnvironment.setProperty(SLEEP_AFTER_EXECUTION, millisecondsSleepAfterExecution);
        }
        final long millisecondsSleepBetweenExecutionActionSteps = channelTestConfig.getMillisecondsSleepBetweenExecutionActionSteps();
        if (millisecondsSleepBetweenExecutionActionSteps > 0) {
            testEnvironment.setProperty(SLEEP_BETWEEN_EXECUTION_STEPS, millisecondsSleepAfterExecution);
        }
    }

    public ChannelSetupBuilder withDefaultActionConsume() {
        channelBuilder.withDefaultAction(consumeAsFinalResult(testEnvironment));
        return this;
    }

    public ChannelSetupBuilder withDefaultActionJumpToDifferentChannel() {
        final Channel<TestMessage> secondChannel = aChannelWithDefaultAction(consumeAsFinalResult(testEnvironment));
        channelBuilder.withDefaultAction(jumpTo(secondChannel));
        return this;
    }

    public ChannelSetupBuilder withDefaultActionReturn() {
        channelBuilder.withDefaultAction(aReturn());
        return this;
    }

    public ChannelSetupBuilder withDefaultActionCall() {
        channelBuilder.withDefaultAction(prepareACall(null));
        return this;
    }

    public ChannelSetupBuilder withSubscriptionAsAction() {
        channelBuilder.withDefaultAction(subscription());
        return this;
    }

    public ChannelSetupBuilder withSubscriptionAsActionWithOnPreemptiveSubscriberAndOneErrorThrowingSubscriberThatShouldNeverBeCalled() {
        final Subscription<TestMessage> subscription = subscription();
        subscription.addSubscriber(deliveryPreemptingSubscriber());
        subscription.addSubscriber(errorThrowingTestSubscriber());
        channelBuilder.withDefaultAction(subscription);
        return this;
    }

    public ChannelSetupBuilder withAnUnknownAction() {
        final Action<TestMessage> unknownAction = UnknownAction.unknownAction();
        channelBuilder.withDefaultAction(unknownAction);
        return this;
    }

    public ChannelSetupBuilder withAnExceptionInFinalAction() {
        channelBuilder.withDefaultAction(Consume.consume(testMessageProcessingContext -> {
            throw new TestException();
        }));
        return this;
    }

    public ChannelSetupBuilder withAPreFilterThatChangesTheAction() {
        final Action<TestMessage> unknownAction = UnknownAction.unknownAction();
        alreadyBuiltChannel = channelBuilder.withDefaultAction(unknownAction)
                .build();
        addChangingActionFilterToPipe(alreadyBuiltChannel, PRE, consumeAsFinalResult(testEnvironment));
        return this;
    }

    public ChannelSetupBuilder withAPreFilterThatReplacesTheMessage() {
        addAFilterThatReplacesTheMessage(PRE);
        return this;
    }

    public ChannelSetupBuilder withAProcessFilterThatReplacesTheMessage() {
        addAFilterThatReplacesTheMessage(PROCESS);
        return this;
    }

    public ChannelSetupBuilder withAPostFilterThatReplacesTheMessage() {
        addAFilterThatReplacesTheMessage(POST);
        return this;
    }

    public ChannelSetupBuilder withAPreFilterThatBlocksMessages() {
        addFilterThatBlocksMessages(PRE);
        return this;
    }

    public ChannelSetupBuilder withAProcessFilterThatBlocksMessages() {
        addFilterThatBlocksMessages(PROCESS);
        return this;
    }

    public ChannelSetupBuilder withAPostFilterThatBlocksMessages() {
        addFilterThatBlocksMessages(POST);
        return this;
    }

    private void addAFilterThatReplacesTheMessage(final FilterPosition filterPosition) {
        if (alreadyBuiltChannel == null) {
            alreadyBuiltChannel = channelBuilder.withDefaultAction(consumeAsFinalResult(testEnvironment))
                    .build();
        }
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

    public ChannelSetupBuilder withAPreFilterThatForgetsMessages() {
        addFilterThatForgetsMessages(PRE);
        return this;
    }

    public ChannelSetupBuilder withAProcessFilterThatForgetsMessages() {
        addFilterThatForgetsMessages(PROCESS);
        return this;
    }

    public ChannelSetupBuilder withAPostFilterThatForgetsMessages() {
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
                channel.addProcessFilter(filter);
                break;
            case POST:
                channel.addPostFilter(filter);
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    public ChannelSetupBuilder withAProcessFilterThatChangesTheAction() {
        final Action<TestMessage> unknownAction = UnknownAction.unknownAction();
        alreadyBuiltChannel = channelBuilder.withDefaultAction(unknownAction)
                .build();
        addChangingActionFilterToPipe(alreadyBuiltChannel, PROCESS, consumeAsFinalResult(testEnvironment));
        return this;
    }

    public ChannelSetupBuilder withAPostFilterThatChangesTheAction() {
        final Action<TestMessage> unknownAction = UnknownAction.unknownAction();
        alreadyBuiltChannel = channelBuilder.withDefaultAction(unknownAction)
                .build();
        addChangingActionFilterToPipe(alreadyBuiltChannel, POST, consumeAsFinalResult(testEnvironment));
        return this;
    }

    public ChannelSetupBuilder withSeveralPreFilter() {
        final int[] positions = new int[]{0, 0, 2, 1, 2, 4};
        severalFilterInPipe(positions, PRE);
        return this;
    }

    public ChannelSetupBuilder withSeveralProcessFilter() {
        final int[] positions = new int[]{0, 1, 1, 3, 0, 5};
        severalFilterInPipe(positions, PROCESS);
        return this;
    }

    public ChannelSetupBuilder withSeveralPostFilter() {
        final int[] positions = new int[]{0, 0, 2, 2, 1, 3};
        severalFilterInPipe(positions, POST);
        return this;
    }

    public ChannelSetupBuilder withAPreFilterAtAnInvalidPosition(final int position) {
        addAFilterAtPosition(position, PRE);
        return this;
    }

    public ChannelSetupBuilder withAProcessFilterAtAnInvalidPosition(final int position) {
        addAFilterAtPosition(position, PROCESS);
        return this;
    }

    public ChannelSetupBuilder withAPostFilterAtAnInvalidPosition(final int position) {
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

    public ChannelSetupBuilder withAnErrorThrowingFilter() {
        alreadyBuiltChannel = channelBuilder.withDefaultAction(consumeAsFinalResult(testEnvironment))
                .build();
        addAFilterToPipe(alreadyBuiltChannel, PROCESS, (message, filterActions) -> {
            throw new TestException();
        });
        return this;
    }

    private void severalFilterInPipe(final int[] positions, final FilterPosition pipe) {
        final Action<TestMessage> unknownAction = UnknownAction.unknownAction();
        alreadyBuiltChannel = channelBuilder.withDefaultAction(unknownAction)
                .build();
        final List<Filter<ProcessingContext<TestMessage>>> expectedFilter = addSeveralNoopFilter(alreadyBuiltChannel, positions, pipe);
        testEnvironment.setProperty(EXPECTED_RESULT, expectedFilter);
        testEnvironment.setProperty(PIPE, pipe);
    }

    public ChannelSetupBuilder withAnExceptionHandlerIgnoringExceptions() {
        channelBuilder.withChannelExceptionHandler(ignoringChannelExceptionHandler());
        return this;
    }

    public ChannelSetupBuilder withACustomErrorHandler() {
        channelBuilder.withChannelExceptionHandler(exceptionInResultStoringChannelExceptionHandler(testEnvironment));
        return this;
    }

    public ChannelSetupBuilder withAnExceptionCatchingHandler_inCaseOfAsynchronousExecution() {
        if (channelTestConfig.getType().equals(ChannelType.ASYNCHRONOUS)) {
            channelBuilder.withChannelExceptionHandler(catchingChannelExceptionHandler(testEnvironment));
        }
        return this;
    }

    public ChannelSetupBuilder withAnErrorHandlerDeclaringErrorsInDeliveryAsNotDeliveryAborting() {
        channelBuilder.withChannelExceptionHandler(testExceptionIgnoringChannelExceptionHandler(testEnvironment));
        return this;
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

    public ChannelSetupBuilder withABlockingSubscriber() {
        final Subscription<TestMessage> subscription = subscription();
        final Semaphore semaphore = new Semaphore(0);
        subscription.addSubscriber(BlockingTestSubscriber.blockingTestSubscriber(semaphore));
        channelBuilder.withDefaultAction(subscription);
        testEnvironment.setProperty(SEMAPHORE_TO_CLEAN_UP, semaphore);
        return this;
    }

    private static class UnknownAction implements Action<TestMessage> {
        private static UnknownAction unknownAction() {
            return new UnknownAction();
        }
    }

}
