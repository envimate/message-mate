package com.envimate.messageMate.channel.givenWhenThen;


import com.envimate.messageMate.channel.Channel;
import com.envimate.messageMate.internal.statistics.MessageStatistics;
import com.envimate.messageMate.shared.givenWhenThen.ActionBuilder;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import com.envimate.messageMate.subscribing.Subscriber;
import com.envimate.messageMate.subscribing.SubscriptionId;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.TimeUnit;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class ChannelActionBuilder extends ActionBuilder<Channel<TestMessage>> {


    public static ActionBuilder<Channel<TestMessage>> aSingleMessageIsSend() {
        return new ChannelActionBuilder()
                .thatSendsASingleMessage();
    }

    public static ActionBuilder<Channel<TestMessage>> severalMessagesAreSend(final int numberOfMessages) {
        return new ChannelActionBuilder()
                .thatSendsSeveralMessages(numberOfMessages);
    }

    public static ActionBuilder<Channel<TestMessage>> severalMessagesAreSendAsynchronously(final int numberOfSender, final int numberOfMessagesPerSender) {
        return new ChannelActionBuilder()
                .thatSendsSeveralMessagesAsynchronously(numberOfSender, numberOfMessagesPerSender);
    }

    public static ActionBuilder<Channel<TestMessage>> severalMessagesAreSendAsynchronouslyButWillBeBlocked(final int numberOfSender, final int numberOfMessagesPerSender) {
        return new ChannelActionBuilder()
                .thatSendsSeveralMessagesAsynchronouslyButWillBeBlocked(numberOfSender, numberOfMessagesPerSender);
    }


    public static ActionBuilder<Channel<TestMessage>> oneSubscriberUnsubscribesSeveralTimes(final int numberOfUnsubscriptions) {
        return new ChannelActionBuilder()
                .thatUnsubscribesASubscriberSeveralTimes(numberOfUnsubscriptions);
    }

    public static ActionBuilder<Channel<TestMessage>> oneSubscriberUnsubscribes() {
        return new ChannelActionBuilder()
                .thatUnsubscribesASubscriber();
    }

    public static ActionBuilder<Channel<TestMessage>> bothValidAndInvalidMessagesAreSendAsynchronously(final int numberOfSender, final int numberOfMessagesPerSender) {
        return new ChannelActionBuilder()
                .thatSendsBothValidAndInvalidMessagesAsynchronously(numberOfSender, numberOfMessagesPerSender);
    }

    public static ActionBuilder<Channel<TestMessage>> severalInvalidMessagesAreSendAsynchronously(final int numberOfSender, final int numberOfMessagesPerSender) {
        return new ChannelActionBuilder()
                .thatSendsSeveralInvalidMessagesAsynchronously(numberOfSender, numberOfMessagesPerSender);
    }

    public static ActionBuilder<Channel<TestMessage>> theNumberOfAcceptedMessagesIsQueried() {
        return new ChannelActionBuilder()
                .thatQueriesTheNumberOfAcceptedMessages();
    }

    public static ActionBuilder<Channel<TestMessage>> theNumberOfAcceptedMessagesIsQueriedAsynchronously() {
        return new ChannelActionBuilder()
                .thatQueriesTheNumberOfAcceptedMessagesAsynchronously();
    }

    public static ActionBuilder<Channel<TestMessage>> theNumberOfWaitingMessagesIsQueried() {
        return new ChannelActionBuilder()
                .thatQueriesTheNumberOfWaitingMessages();
    }

    public static ActionBuilder<Channel<TestMessage>> theNumberOfSuccessfulMessagesIsQueried() {
        return new ChannelActionBuilder()
                .thatQueriesTheNumberOfSuccessfulDeliveredMessages();
    }

    public static ActionBuilder<Channel<TestMessage>> theNumberOfFailedMessagesIsQueried() {
        return new ChannelActionBuilder()
                .thatQueriesTheNumberOfFailedDeliveredMessages();
    }

    public static ActionBuilder<Channel<TestMessage>> theNumberOfDroppedMessagesIsQueried() {
        return new ChannelActionBuilder()
                .thatQueriesTheNumberOfDroppedMessages();
    }

    public static ActionBuilder<Channel<TestMessage>> theNumberOfReplacedMessagesIsQueried() {
        return new ChannelActionBuilder()
                .thatQueriesTheNumberOfReplacedMessages();
    }

    public static ActionBuilder<Channel<TestMessage>> theNumberOfForgottenMessagesIsQueried() {
        return new ChannelActionBuilder()
                .thatQueriesTheNumberOfForgottenMessages();
    }

    public static ActionBuilder<Channel<TestMessage>> theNumberOfCurrentlyDeliveredMessagesIsQueried() {
        return new ChannelActionBuilder()
                .thatQueriesTheNumberOfCurrentlyDeliveredMessages();
    }

    public static ActionBuilder<Channel<TestMessage>> theNumberOfCurrentlyTransportedMessagesIsQueried() {
        return new ChannelActionBuilder()
                .thatQueriesTheNumberOfCurrentlyTransportedMessages();
    }

    public static ActionBuilder<Channel<TestMessage>> theTimestampOfTheStatisticsIsQueried() {
        return new ChannelActionBuilder()
                .thatQueriesTheTimestampOfTheMessageStatistics();
    }

    public static ActionBuilder<Channel<TestMessage>> aShortWaitIsDone(final long timeout, final TimeUnit timeUnit) {
        return new ChannelActionBuilder()
                .thatPerformsAShortWait(timeout, timeUnit);
    }

    public static ActionBuilder<Channel<TestMessage>> severalMessagesAreSendAsynchronouslyBeforeTheChannelIsShutdown(final int numberOfSenders, final int numberOfMessages) {
        return new ChannelActionBuilder()
                .thatSendsSeveralMessagesAsynchronouslyBeforeTheObjectIsShutdown(numberOfSenders, numberOfMessages);
    }

    public static ActionBuilder<Channel<TestMessage>> theChannelIsShutdownAsynchronouslyXTimes(final int numberOfThreads) {
        return new ChannelActionBuilder()
                .thatShutdownsTheObjectAsynchronouslyXTimes(numberOfThreads);
    }

    public static ActionBuilder<Channel<TestMessage>> theChannelIsShutdown() {
        return new ChannelActionBuilder()
                .thatShutdownsTheObject();
    }

    public static ActionBuilder<Channel<TestMessage>> theChannelIsShutdownAfterHalfOfTheMessagesWereDelivered(final int numberOfMessages) {
        final int numberOfMessagesBeforeShutdown = numberOfMessages / 2;
        final int remainingMessages = numberOfMessages - numberOfMessagesBeforeShutdown;
        return new ChannelActionBuilder()
                .thatSendsXMessagesAShutdownsIsCalledThenSendsYMessage(numberOfMessagesBeforeShutdown, remainingMessages, true);
    }

    public static ActionBuilder<Channel<TestMessage>> theChannelIsShutdownAfterHalfOfTheMessagesWereDelivered_withoutFinishingRemainingTasks(final int numberOfMessages) {
        final int numberOfMessagesBeforeShutdown = numberOfMessages / 2;
        final int remainingMessages = numberOfMessages - numberOfMessagesBeforeShutdown;
        return new ChannelActionBuilder()
                .thatSendsXMessagesAShutdownsIsCalledThenSendsYMessage(numberOfMessagesBeforeShutdown, remainingMessages, false);
    }

    public static ActionBuilder<Channel<TestMessage>> theChannelShutdownIsExpectedForTimeoutInSeconds(final int timeoutInSeconds) {
        return new ChannelActionBuilder()
                .thatAwaitsTheShutdownTimeoutInSeconds(timeoutInSeconds);
    }


    @Override
    public void send(final Channel<TestMessage> channel, final TestMessage message) {
        channel.send(message);
    }

    @Override
    protected void unsubscribe(final Channel<TestMessage> channel, final SubscriptionId subscriptionId) {
        channel.unsubscribe(subscriptionId);
    }

    @Override
    protected MessageStatistics getMessageStatistics(final Channel<TestMessage> channel) {
        return channel.getStatusInformation().getCurrentMessageStatistics();
    }

    @Override
    protected <R> void subscribe(final Channel<TestMessage> channel, final Class<R> messageClass, final Subscriber<R> subscriber) {
        channel.subscribe((Subscriber<TestMessage>) subscriber);
    }

    @Override
    protected void close(final Channel<TestMessage> channel, final boolean finishRemainingTasks) {
        channel.close(finishRemainingTasks);
    }

    @Override
    protected boolean awaitTermination(final Channel<TestMessage> channel, final int timeout, final TimeUnit timeUnit) throws InterruptedException {
        return channel.awaitTermination(timeout, timeUnit);
    }
}
