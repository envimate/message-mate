package com.envimate.messageMate.channel.givenWhenThen;

import com.envimate.messageMate.channel.Channel;
import com.envimate.messageMate.channel.ChannelStatistics;
import com.envimate.messageMate.channel.ProcessingContext;
import com.envimate.messageMate.channel.action.Subscription;
import com.envimate.messageMate.filtering.Filter;
import com.envimate.messageMate.qcec.shared.TestAction;
import com.envimate.messageMate.shared.subscriber.SimpleTestSubscriber;
import com.envimate.messageMate.shared.subscriber.TestSubscriber;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import com.envimate.messageMate.shared.testMessages.TestMessageOfInterest;
import com.envimate.messageMate.subscribing.Subscriber;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.function.BiConsumer;

import static com.envimate.messageMate.channel.givenWhenThen.ChannelTestActions.*;
import static com.envimate.messageMate.channel.givenWhenThen.ChannelTestProperties.*;
import static com.envimate.messageMate.channel.givenWhenThen.FilterPosition.*;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.*;
import static com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.AsynchronousSendingTestUtils.sendMessagesBeforeShutdownAsynchronously;
import static com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.AsynchronousSendingTestUtils.sendValidMessagesAsynchronously;
import static com.envimate.messageMate.shared.subscriber.SimpleTestSubscriber.testSubscriber;
import static com.envimate.messageMate.shared.testMessages.TestMessageOfInterest.messageOfInterest;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public final class ChannelActionBuilder {
    private final List<TestAction<Channel<TestMessage>>> testActions;

    public ChannelActionBuilder(final TestAction<Channel<TestMessage>> testAction) {
        this.testActions = new LinkedList<>();
        testActions.add(testAction);
    }

    private static ChannelActionBuilder anAction(final TestAction<Channel<TestMessage>> testAction) {
        return new ChannelActionBuilder(testAction);
    }

    public static ChannelActionBuilder aMessageIsSend() {
        return anAction((channel, testEnvironment) -> {
            final ProcessingContext<TestMessage> sendProcessingFrame = sendMessage(channel, DEFAULT_TEST_MESSAGE);
            testEnvironment.setProperty(EXPECTED_RESULT, sendProcessingFrame);
            return null;
        });
    }

    public static ChannelActionBuilder severalMessagesAreSend(final int numberOfMessages) {
        return anAction((channel, testEnvironment) -> {
            for (int i = 0; i < numberOfMessages; i++) {
                final TestMessageOfInterest testMessage = messageOfInterest();
                sendMessage(channel, testMessage);
                testEnvironment.addToListProperty(EXPECTED_MESSAGES, testMessage);
            }
            return null;
        });
    }

    public static ChannelActionBuilder severalMessagesAreSendAsynchronously(final int numberOfMessages) {
        return anAction((channel, testEnvironment) -> {
            sendValidMessagesAsynchronously(testMessage -> channel.accept(ProcessingContext.processingContext(testMessage)), testEnvironment,
                    numberOfMessages, 1, false);
            final long millisecondsToLetThreadsFInishAfterReleasingSemaphoreBeforeCloseIsCalled = 5;
            testEnvironment.setProperty(SLEEP_BEFORE_CLOSE, millisecondsToLetThreadsFInishAfterReleasingSemaphoreBeforeCloseIsCalled);
            return null;
        });
    }

    public static ChannelActionBuilder severalMessagesAreSendAsynchronouslyBeforeTheChannelIsClosedWithoutFinishingRemainingTasks(final int numberOfMessages) {
        return anAction((channel, testEnvironment) -> {
            sendMessagesBeforeShutdownAsynchronously(addSubscriber(channel), testMessage -> channel.accept(ProcessingContext.processingContext(testMessage)),
                    channel::close, testEnvironment, numberOfMessages, 1);
            return null;
        });
    }
    public static ChannelActionBuilder sendMessagesBeforeTheShutdownIsAwaitedWithoutFinishingTasks(final int numberOfMessages) {
        return anAction((channel, testEnvironment) -> {
            sendMessagesBeforeShutdownAsynchronously(addSubscriber(channel), testMessage -> channel.accept(ProcessingContext.processingContext(testMessage)),
                    ignored -> {
                        try {
                            channel.close(false);
                            final boolean awaitSucceeded = channel.awaitTermination(2, MILLISECONDS);
                            testEnvironment.setProperty(RESULT, awaitSucceeded);
                        } catch (final InterruptedException e) {
                            testEnvironment.setProperty(EXCEPTION, e);
                        }
                    }, testEnvironment, numberOfMessages, 1);
            return null;
        });
    }

    private static BiConsumer<Class<TestMessageOfInterest>, Subscriber<TestMessageOfInterest>> addSubscriber(final Channel<TestMessage> channel) {
        return (testMessageOfInterestClass, subscriber) -> {
            final Subscription<TestMessageOfInterest> subscription = (Subscription) channel.getDefaultAction();
            subscription.addSubscriber(subscriber);
        };
    }

    @SuppressWarnings("unchecked")
    public static ChannelActionBuilder aCallToTheSecondChannelIsExecuted() {
        return anAction((channel, testEnvironment) -> {
            final Channel<TestMessage> callTargetChannel = (Channel<TestMessage>) testEnvironment.getProperty(CALL_TARGET_CHANNEL);
            addFilterExecutingACall(channel, callTargetChannel);

            final ProcessingContext<TestMessage> sendProcessingFrame = sendMessage(channel, DEFAULT_TEST_MESSAGE);
            testEnvironment.setProperty(EXPECTED_RESULT, sendProcessingFrame);
            return null;
        });
    }

    public static ChannelActionBuilder severalPreFilterOnDifferentPositionAreAdded() {
        final int[] positions = new int[]{0, 1, 0, 0, 3, 2};
        return anAction(actionForAddingSeveralFilter(positions, PRE));
    }

    public static ChannelActionBuilder severalProcessFilterOnDifferentPositionAreAdded() {
        final int[] positions = new int[]{0, 0, 1, 0, 2, 4, 4};
        return anAction(actionForAddingSeveralFilter(positions, PROCESS));
    }

    public static ChannelActionBuilder severalPostFilterOnDifferentPositionAreAdded() {
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

    public static ChannelActionBuilder theFilterAreQueried() {
        return anAction((channel, testEnvironment) -> {
            final FilterPosition pipe = testEnvironment.getPropertyAsType(PIPE, FilterPosition.class);
            final List<Filter<ProcessingContext<TestMessage>>> filter = getFilterOf(channel, pipe);
            testEnvironment.setProperty(RESULT, filter);
            return null;
        });
    }

    @SuppressWarnings("unchecked")
    public static ChannelActionBuilder oneFilterIsRemoved() {
        return anAction((channel, testEnvironment) -> {
            final FilterPosition pipe = testEnvironment.getPropertyAsType(PIPE, FilterPosition.class);
            final List<Filter<ProcessingContext<TestMessage>>> allFilter = (List<Filter<ProcessingContext<TestMessage>>>) testEnvironment.getProperty(EXPECTED_RESULT);
            final Filter<ProcessingContext<TestMessage>> filterToRemove = allFilter.remove(1);
            removeFilter(channel, pipe, filterToRemove);
            return null;
        });
    }

    public static ChannelActionBuilder whenTheMetaDataIsModified() {
        return anAction((channel, testEnvironment) -> {
            final String changedMetaDatum = "changed";
            addAFilterChangingMetaData(channel, changedMetaDatum);
            testEnvironment.setProperty(EXPECTED_RESULT, changedMetaDatum);
            sendMessage(channel, messageOfInterest());
            return null;
        });
    }

    public static ChannelActionBuilder theNumberOfAcceptedMessagesIsQueried() {
        return anAction((channel, testEnvironment) -> {
            testEnvironment.setProperty(RESULT, queryChannelStatistics(channel, ChannelStatistics::getAcceptedMessages));
            return null;
        });
    }

    public static ChannelActionBuilder theNumberOfQueuedMessagesIsQueried() {
        return anAction((channel, testEnvironment) -> {
            testEnvironment.setProperty(RESULT, queryChannelStatistics(channel, ChannelStatistics::getQueuedMessages));
            return null;
        });
    }

    public static ChannelActionBuilder theNumberOfReplacedMessagesIsQueried() {
        return anAction((channel, testEnvironment) -> {
            testEnvironment.setProperty(RESULT, queryChannelStatistics(channel, ChannelStatistics::getReplacedMessages));
            return null;
        });
    }

    public static ChannelActionBuilder theNumberOfBlockedMessagesIsQueried() {
        return anAction((channel, testEnvironment) -> {
            testEnvironment.setProperty(RESULT, queryChannelStatistics(channel, ChannelStatistics::getBlockedMessages));
            return null;
        });
    }

    public static ChannelActionBuilder theNumberOfForgottenMessagesIsQueried() {
        return anAction((channel, testEnvironment) -> {
            testEnvironment.setProperty(RESULT, queryChannelStatistics(channel, ChannelStatistics::getForgottenMessages));
            return null;
        });
    }

    public static ChannelActionBuilder theNumberOfSuccessfulDeliveredMessagesIsQueried() {
        return anAction((channel, testEnvironment) -> {
            testEnvironment.setProperty(RESULT, queryChannelStatistics(channel, ChannelStatistics::getSuccessfulMessages));
            return null;
        });
    }

    public static ChannelActionBuilder theNumberOfFailedDeliveredMessagesIsQueried() {
        return anAction((channel, testEnvironment) -> {
            testEnvironment.setProperty(RESULT, queryChannelStatistics(channel, ChannelStatistics::getFailedMessages));
            return null;
        });
    }

    public static ChannelActionBuilder severalSubscriberAreAdded() {
        return anAction((channel, testEnvironment) -> {
            final Subscription<TestMessage> subscription = getDefaultActionAsSubscription(channel);
            for (int i = 0; i < 5; i++) {
                final SimpleTestSubscriber<TestMessage> subscriber = testSubscriber();
                subscription.addSubscriber(subscriber);
                testEnvironment.addToListProperty(EXPECTED_RECEIVERS, subscriber);

            }
            return null;
        });
    }

    public static ChannelActionBuilder oneSubscriberIsRemoved() {
        return anAction((channel, testEnvironment) -> {
            final Subscription<TestMessage> subscription = getDefaultActionAsSubscription(channel);
            @SuppressWarnings("unchecked")
            final List<TestSubscriber<TestMessage>> currentReceiver = (List<TestSubscriber<TestMessage>>) testEnvironment.getProperty(EXPECTED_RECEIVERS);
            final TestSubscriber<TestMessage> subscriberToRemove = currentReceiver.remove(0);
            subscription.removeSubscriber(subscriberToRemove);
            return null;
        });
    }

    public static ChannelActionBuilder theChannelIsClosedSeveralTimes() {
        return anAction((channel, testEnvironment) -> {
            for (int i = 0; i < 5; i++) {
                channel.close(true);
            }
            return null;
        });
    }

    public static ChannelActionBuilder theChannelIsClosedAndTheShutdownIsAwaited() {
        return anAction((channel, testEnvironment) -> {
            channel.close(true);
            try {
                final boolean awaitSucceeded = channel.awaitTermination(2, MILLISECONDS);
                testEnvironment.setProperty(RESULT, awaitSucceeded);
            } catch (final InterruptedException e) {
                testEnvironment.setProperty(EXCEPTION, e);
            }
            return null;
        });
    }

    public static ChannelActionBuilder theShutdownIsAwaited() {
        return anAction((channel, testEnvironment) -> {
            try {
                final boolean awaitSucceeded = channel.awaitTermination(2, MILLISECONDS);
                testEnvironment.setProperty(RESULT, awaitSucceeded);
            } catch (final InterruptedException e) {
                testEnvironment.setProperty(EXCEPTION, e);
            }
            return null;
        });
    }

    public static ChannelActionBuilder theSubscriberLockIsReleased() {
        return anAction((channel, testEnvironment) -> {
            final Semaphore subscriberLock = testEnvironment.getPropertyAsType(SEMAPHORE_TO_CLEAN_UP, Semaphore.class);
            subscriberLock.release(100);
            try {
                MILLISECONDS.sleep(10);
            } catch (final InterruptedException e) {
                testEnvironment.setProperty(EXCEPTION, e);
            }
            return null;
        });
    }

    private static Subscription<TestMessage> getDefaultActionAsSubscription(final Channel<TestMessage> channel) {
        return (Subscription<TestMessage>) channel.getDefaultAction();
    }

    public List<TestAction<Channel<TestMessage>>> build() {
        return testActions;
    }

    public ChannelActionBuilder andThen(final ChannelActionBuilder followUpBuilder) {
        this.testActions.addAll(followUpBuilder.testActions);
        return this;
    }
}
