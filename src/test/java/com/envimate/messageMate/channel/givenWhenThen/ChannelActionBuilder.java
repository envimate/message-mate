package com.envimate.messageMate.channel.givenWhenThen;


import com.envimate.messageMate.channel.Channel;
import com.envimate.messageMate.qcec.shared.TestAction;
import com.envimate.messageMate.shared.channelMessageBus.givenWhenThen.ActionBuilder;
import com.envimate.messageMate.shared.channelMessageBus.givenWhenThen.ChannelMessageBusSutActions;
import com.envimate.messageMate.shared.testMessages.TestMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.envimate.messageMate.channel.givenWhenThen.ChannelTestActions.channelTestActions;
import static com.envimate.messageMate.shared.channelMessageBus.givenWhenThen.ChannelMessageBusTestActions.*;

/*
TODO: query subscriber
 */
public final class ChannelActionBuilder implements ActionBuilder<Channel<TestMessage>> {
    private List<TestAction<Channel<TestMessage>>> actions = new ArrayList<>();

    private ChannelActionBuilder(final TestAction<Channel<TestMessage>> action) {
        this.actions.add(action);
    }

    public static ActionBuilder<Channel<TestMessage>> aSingleMessageIsSend() {
        return new ChannelActionBuilder((channel, testEnvironment) -> {
            final ChannelMessageBusSutActions sutActions = channelTestActions(channel);
            sendASingleMessage(sutActions, testEnvironment);
            return null;
        });
    }

    public static ActionBuilder<Channel<TestMessage>> severalMessagesAreSend(final int numberOfMessages) {
        return new ChannelActionBuilder((channel, testEnvironment) -> {
            final ChannelMessageBusSutActions sutActions = channelTestActions(channel);
            sendSeveralMessages(sutActions, testEnvironment, numberOfMessages);
            return null;
        });
    }

    public static ActionBuilder<Channel<TestMessage>> severalMessagesAreSendAsynchronously(final int numberOfSender, final int numberOfMessagesPerSender) {
        return new ChannelActionBuilder((channel, testEnvironment) -> {
            final ChannelMessageBusSutActions sutActions = channelTestActions(channel);
            sendSeveralMessagesInTheirOwnThread(sutActions, testEnvironment, numberOfSender, numberOfMessagesPerSender, true);
            return null;
        });
    }

    public static ActionBuilder<Channel<TestMessage>> severalMessagesAreSendAsynchronouslyButWillBeBlocked(final int numberOfSender, final int numberOfMessagesPerSender) {
        return new ChannelActionBuilder((channel, testEnvironment) -> {
            final ChannelMessageBusSutActions sutActions = channelTestActions(channel);
            sendSeveralMessagesInTheirOwnThread(sutActions, testEnvironment, numberOfSender, numberOfMessagesPerSender, false);
            return null;
        });
    }

    public static ActionBuilder<Channel<TestMessage>> oneSubscriberUnsubscribesSeveralTimes(final int numberOfUnsubscriptions) {
        return new ChannelActionBuilder((channel, testEnvironment) -> {
            final ChannelMessageBusSutActions sutActions = channelTestActions(channel);
            unsubscribeASubscriberXTimes(sutActions, testEnvironment, numberOfUnsubscriptions);
            return null;
        });
    }

    public static ActionBuilder<Channel<TestMessage>> oneSubscriberUnsubscribes() {
        return new ChannelActionBuilder((channel, testEnvironment) -> {
            final ChannelMessageBusSutActions sutActions = channelTestActions(channel);
            unsubscribeASubscriberXTimes(sutActions, testEnvironment, 1);
            return null;
        });
    }

    public static ActionBuilder<Channel<TestMessage>> bothValidAndInvalidMessagesAreSendAsynchronously(final int numberOfSender, final int numberOfMessagesPerSender) {
        return new ChannelActionBuilder((channel, testEnvironment) -> {
            final ChannelMessageBusSutActions sutActions = channelTestActions(channel);
            sendBothValidAndInvalidMessagesAsynchronously(sutActions, testEnvironment, numberOfSender, numberOfMessagesPerSender);
            return null;
        });
    }

    public static ActionBuilder<Channel<TestMessage>> severalInvalidMessagesAreSendAsynchronously(final int numberOfSender, final int numberOfMessagesPerSender) {
        return new ChannelActionBuilder((channel, testEnvironment) -> {
            final ChannelMessageBusSutActions sutActions = channelTestActions(channel);
            sendSeveralInvalidMessagesAsynchronously(sutActions, testEnvironment, numberOfSender, numberOfMessagesPerSender);
            return null;
        });
    }

    public static ActionBuilder<Channel<TestMessage>> theNumberOfAcceptedMessagesIsQueried() {
        return new ChannelActionBuilder((channel, testEnvironment) -> {
            final ChannelMessageBusSutActions sutActions = channelTestActions(channel);
            queryTheNumberOfAcceptedMessages(sutActions, testEnvironment);
            return null;
        });
    }

    public static ActionBuilder<Channel<TestMessage>> theNumberOfAcceptedMessagesIsQueriedAsynchronously() {
        return new ChannelActionBuilder((channel, testEnvironment) -> {
            final ChannelMessageBusSutActions sutActions = channelTestActions(channel);
            queryTheNumberOfAcceptedMessagesAsynchronously(sutActions, testEnvironment);
            return null;
        });
    }

    public static ActionBuilder<Channel<TestMessage>> theNumberOfWaitingMessagesIsQueried() {
        return new ChannelActionBuilder((channel, testEnvironment) -> {
            final ChannelMessageBusSutActions sutActions = channelTestActions(channel);
            queryTheNumberOfWaitingMessages(sutActions, testEnvironment);
            return null;
        });
    }

    public static ActionBuilder<Channel<TestMessage>> theNumberOfSuccessfulMessagesIsQueried() {
        return new ChannelActionBuilder((channel, testEnvironment) -> {
            final ChannelMessageBusSutActions sutActions = channelTestActions(channel);
            queryTheNumberOfSuccessfulDeliveredMessages(sutActions, testEnvironment);
            return null;
        });
    }

    public static ActionBuilder<Channel<TestMessage>> theNumberOfFailedMessagesIsQueried() {
        return new ChannelActionBuilder((channel, testEnvironment) -> {
            final ChannelMessageBusSutActions sutActions = channelTestActions(channel);
            queryTheNumberOfFailedDeliveredMessages(sutActions, testEnvironment);
            return null;
        });
    }

    public static ActionBuilder<Channel<TestMessage>> theNumberOfDroppedMessagesIsQueried() {
        return new ChannelActionBuilder((channel, testEnvironment) -> {
            final ChannelMessageBusSutActions sutActions = channelTestActions(channel);
            queryTheNumberOfDroppedMessages(sutActions, testEnvironment);
            return null;
        });
    }

    public static ActionBuilder<Channel<TestMessage>> theNumberOfReplacedMessagesIsQueried() {
        return new ChannelActionBuilder((channel, testEnvironment) -> {
            final ChannelMessageBusSutActions sutActions = channelTestActions(channel);
            queryTheNumberOfReplacedMessages(sutActions, testEnvironment);
            return null;
        });
    }

    public static ActionBuilder<Channel<TestMessage>> theNumberOfForgottenMessagesIsQueried() {
        return new ChannelActionBuilder((channel, testEnvironment) -> {
            final ChannelMessageBusSutActions sutActions = channelTestActions(channel);
            queryTheNumberOfForgottenMessages(sutActions, testEnvironment);
            return null;
        });
    }

    public static ActionBuilder<Channel<TestMessage>> theNumberOfCurrentlyDeliveredMessagesIsQueried() {
        return new ChannelActionBuilder((channel, testEnvironment) -> {
            final ChannelMessageBusSutActions sutActions = channelTestActions(channel);
            queryTheNumberOfCurrentlyDeliveredMessages(sutActions, testEnvironment);
            return null;
        });
    }

    public static ActionBuilder<Channel<TestMessage>> theNumberOfCurrentlyTransportedMessagesIsQueried() {
        return new ChannelActionBuilder((channel, testEnvironment) -> {
            final ChannelMessageBusSutActions sutActions = channelTestActions(channel);
            queryTheNumberOfCurrentlyTransportedMessages(sutActions, testEnvironment);
            return null;
        });
    }

    public static ActionBuilder<Channel<TestMessage>> theTimestampOfTheStatisticsIsQueried() {
        return new ChannelActionBuilder((channel, testEnvironment) -> {
            final ChannelMessageBusSutActions sutActions = channelTestActions(channel);
            queryTheTimestampOfTheMessageStatistics(sutActions, testEnvironment);
            return null;
        });
    }

    public static ActionBuilder<Channel<TestMessage>> aShortWaitIsDone(final long timeout, final TimeUnit timeUnit) {
        return new ChannelActionBuilder((channel, testEnvironment) -> {
            performAShortWait(timeout, timeUnit);
            return null;
        });
    }

    public static ActionBuilder<Channel<TestMessage>> severalMessagesAreSendAsynchronouslyBeforeTheChannelIsShutdown(final int numberOfSenders, final int numberOfMessages) {
        return new ChannelActionBuilder((channel, testEnvironment) -> {
            final ChannelMessageBusSutActions sutActions = channelTestActions(channel);
            sendSeveralMessagesAsynchronouslyBeforeTheObjectIsShutdown(sutActions, testEnvironment, numberOfSenders, numberOfMessages);
            return null;
        });
    }

    public static ActionBuilder<Channel<TestMessage>> theChannelIsShutdownAsynchronouslyXTimes(final int numberOfThreads) {
        return new ChannelActionBuilder((channel, testEnvironment) -> {
            final ChannelMessageBusSutActions sutActions = channelTestActions(channel);
            shutdownTheObjectAsynchronouslyXTimes(sutActions, numberOfThreads);
            return null;
        });
    }

    public static ActionBuilder<Channel<TestMessage>> theChannelIsShutdown() {
        return new ChannelActionBuilder((channel, testEnvironment) -> {
            final ChannelMessageBusSutActions sutActions = channelTestActions(channel);
            shutdownTheSut(sutActions);
            return null;
        });
    }

    public static ActionBuilder<Channel<TestMessage>> theChannelIsShutdownAfterHalfOfTheMessagesWereDelivered(final int numberOfMessages) {
        final int numberOfMessagesBeforeShutdown = numberOfMessages / 2;
        final int remainingMessages = numberOfMessages - numberOfMessagesBeforeShutdown;
        return new ChannelActionBuilder((channel, testEnvironment) -> {
            final ChannelMessageBusSutActions sutActions = channelTestActions(channel);
            sendXMessagesAShutdownsIsCalledThenSendsYMessage(sutActions, testEnvironment, numberOfMessagesBeforeShutdown, remainingMessages, true);
            return null;
        });
    }

    public static ActionBuilder<Channel<TestMessage>> theChannelIsShutdownAfterHalfOfTheMessagesWereDelivered_withoutFinishingRemainingTasks(final int numberOfMessages) {
        final int numberOfMessagesBeforeShutdown = numberOfMessages / 2;
        final int remainingMessages = numberOfMessages - numberOfMessagesBeforeShutdown;
        return new ChannelActionBuilder((channel, testEnvironment) -> {
            final ChannelMessageBusSutActions sutActions = channelTestActions(channel);
            sendXMessagesAShutdownsIsCalledThenSendsYMessage(sutActions, testEnvironment, numberOfMessagesBeforeShutdown, remainingMessages, false);
            return null;
        });
    }

    public static ActionBuilder<Channel<TestMessage>> theChannelShutdownIsExpectedForTimeoutInSeconds(final int timeoutInSeconds) {
        return new ChannelActionBuilder((channel, testEnvironment) -> {
            final ChannelMessageBusSutActions sutActions = channelTestActions(channel);
            awaitTheShutdownTimeoutInSeconds(sutActions, testEnvironment, timeoutInSeconds);
            return null;
        });
    }

    public static ActionBuilder<Channel<TestMessage>> theListOfFiltersIsQueried() {
        return new ChannelActionBuilder((channel, testEnvironment) -> {
            final ChannelMessageBusSutActions sutActions = channelTestActions(channel);
            queryTheListOfFilters(sutActions, testEnvironment);
            return null;
        });
    }

    public static ActionBuilder<Channel<TestMessage>> aFilterIsRemoved() {
        return new ChannelActionBuilder((channel, testEnvironment) -> {
            final ChannelMessageBusSutActions sutActions = channelTestActions(channel);
            removeAFilter(sutActions, testEnvironment);
            return null;
        });
    }

    @Override
    public ActionBuilder<Channel<TestMessage>> andThen(final ActionBuilder<Channel<TestMessage>> followUpBuilder) {
        if (followUpBuilder instanceof ChannelActionBuilder) {
            actions.addAll(((ChannelActionBuilder) followUpBuilder).actions);
        }
        return this;
    }

    @Override
    public List<TestAction<Channel<TestMessage>>> build() {
        return actions;
    }

}
