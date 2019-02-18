package com.envimate.messageMate.pipe.givenWhenThen;


import com.envimate.messageMate.pipe.Pipe;
import com.envimate.messageMate.qcec.shared.TestAction;
import com.envimate.messageMate.shared.channelMessageBus.givenWhenThen.ActionBuilder;
import com.envimate.messageMate.shared.channelMessageBus.givenWhenThen.ChannelMessageBusSutActions;
import com.envimate.messageMate.shared.testMessages.TestMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.envimate.messageMate.pipe.givenWhenThen.ChannelTestActions.channelTestActions;
import static com.envimate.messageMate.shared.channelMessageBus.givenWhenThen.ChannelMessageBusTestActions.*;

/*
TODO: query subscriber
 */
public final class ChannelActionBuilder implements ActionBuilder<Pipe<TestMessage>> {
    private List<TestAction<Pipe<TestMessage>>> actions = new ArrayList<>();

    private ChannelActionBuilder(final TestAction<Pipe<TestMessage>> action) {
        this.actions.add(action);
    }

    public static ActionBuilder<Pipe<TestMessage>> aSingleMessageIsSend() {
        return new ChannelActionBuilder((channel, testEnvironment) -> {
            final ChannelMessageBusSutActions sutActions = channelTestActions(channel);
            sendASingleMessage(sutActions, testEnvironment);
            return null;
        });
    }

    public static ActionBuilder<Pipe<TestMessage>> severalMessagesAreSend(final int numberOfMessages) {
        return new ChannelActionBuilder((channel, testEnvironment) -> {
            final ChannelMessageBusSutActions sutActions = channelTestActions(channel);
            sendSeveralMessages(sutActions, testEnvironment, numberOfMessages);
            return null;
        });
    }

    public static ActionBuilder<Pipe<TestMessage>> severalMessagesAreSendAsynchronously(final int numberOfSender, final int numberOfMessagesPerSender) {
        return new ChannelActionBuilder((channel, testEnvironment) -> {
            final ChannelMessageBusSutActions sutActions = channelTestActions(channel);
            sendSeveralMessagesInTheirOwnThread(sutActions, testEnvironment, numberOfSender, numberOfMessagesPerSender, true);
            return null;
        });
    }

    public static ActionBuilder<Pipe<TestMessage>> severalMessagesAreSendAsynchronouslyButWillBeBlocked(final int numberOfSender, final int numberOfMessagesPerSender) {
        return new ChannelActionBuilder((channel, testEnvironment) -> {
            final ChannelMessageBusSutActions sutActions = channelTestActions(channel);
            sendSeveralMessagesInTheirOwnThread(sutActions, testEnvironment, numberOfSender, numberOfMessagesPerSender, false);
            return null;
        });
    }

    public static ActionBuilder<Pipe<TestMessage>> oneSubscriberUnsubscribesSeveralTimes(final int numberOfUnsubscriptions) {
        return new ChannelActionBuilder((channel, testEnvironment) -> {
            final ChannelMessageBusSutActions sutActions = channelTestActions(channel);
            unsubscribeASubscriberXTimes(sutActions, testEnvironment, numberOfUnsubscriptions);
            return null;
        });
    }

    public static ActionBuilder<Pipe<TestMessage>> oneSubscriberUnsubscribes() {
        return new ChannelActionBuilder((channel, testEnvironment) -> {
            final ChannelMessageBusSutActions sutActions = channelTestActions(channel);
            unsubscribeASubscriberXTimes(sutActions, testEnvironment, 1);
            return null;
        });
    }

    public static ActionBuilder<Pipe<TestMessage>> bothValidAndInvalidMessagesAreSendAsynchronously(final int numberOfSender, final int numberOfMessagesPerSender) {
        return new ChannelActionBuilder((channel, testEnvironment) -> {
            final ChannelMessageBusSutActions sutActions = channelTestActions(channel);
            sendBothValidAndInvalidMessagesAsynchronously(sutActions, testEnvironment, numberOfSender, numberOfMessagesPerSender);
            return null;
        });
    }

    public static ActionBuilder<Pipe<TestMessage>> severalInvalidMessagesAreSendAsynchronously(final int numberOfSender, final int numberOfMessagesPerSender) {
        return new ChannelActionBuilder((channel, testEnvironment) -> {
            final ChannelMessageBusSutActions sutActions = channelTestActions(channel);
            sendSeveralInvalidMessagesAsynchronously(sutActions, testEnvironment, numberOfSender, numberOfMessagesPerSender);
            return null;
        });
    }

    public static ActionBuilder<Pipe<TestMessage>> theNumberOfAcceptedMessagesIsQueried() {
        return new ChannelActionBuilder((channel, testEnvironment) -> {
            final ChannelMessageBusSutActions sutActions = channelTestActions(channel);
            queryTheNumberOfAcceptedMessages(sutActions, testEnvironment);
            return null;
        });
    }

    public static ActionBuilder<Pipe<TestMessage>> theNumberOfAcceptedMessagesIsQueriedAsynchronously() {
        return new ChannelActionBuilder((channel, testEnvironment) -> {
            final ChannelMessageBusSutActions sutActions = channelTestActions(channel);
            queryTheNumberOfAcceptedMessagesAsynchronously(sutActions, testEnvironment);
            return null;
        });
    }

    public static ActionBuilder<Pipe<TestMessage>> theNumberOfWaitingMessagesIsQueried() {
        return new ChannelActionBuilder((channel, testEnvironment) -> {
            final ChannelMessageBusSutActions sutActions = channelTestActions(channel);
            queryTheNumberOfWaitingMessages(sutActions, testEnvironment);
            return null;
        });
    }

    public static ActionBuilder<Pipe<TestMessage>> theNumberOfSuccessfulMessagesIsQueried() {
        return new ChannelActionBuilder((channel, testEnvironment) -> {
            final ChannelMessageBusSutActions sutActions = channelTestActions(channel);
            queryTheNumberOfSuccessfulDeliveredMessages(sutActions, testEnvironment);
            return null;
        });
    }

    public static ActionBuilder<Pipe<TestMessage>> theNumberOfFailedMessagesIsQueried() {
        return new ChannelActionBuilder((channel, testEnvironment) -> {
            final ChannelMessageBusSutActions sutActions = channelTestActions(channel);
            queryTheNumberOfFailedDeliveredMessages(sutActions, testEnvironment);
            return null;
        });
    }

    public static ActionBuilder<Pipe<TestMessage>> theNumberOfDroppedMessagesIsQueried() {
        return new ChannelActionBuilder((channel, testEnvironment) -> {
            final ChannelMessageBusSutActions sutActions = channelTestActions(channel);
            queryTheNumberOfDroppedMessages(sutActions, testEnvironment);
            return null;
        });
    }

    public static ActionBuilder<Pipe<TestMessage>> theNumberOfReplacedMessagesIsQueried() {
        return new ChannelActionBuilder((channel, testEnvironment) -> {
            final ChannelMessageBusSutActions sutActions = channelTestActions(channel);
            queryTheNumberOfReplacedMessages(sutActions, testEnvironment);
            return null;
        });
    }

    public static ActionBuilder<Pipe<TestMessage>> theNumberOfForgottenMessagesIsQueried() {
        return new ChannelActionBuilder((channel, testEnvironment) -> {
            final ChannelMessageBusSutActions sutActions = channelTestActions(channel);
            queryTheNumberOfForgottenMessages(sutActions, testEnvironment);
            return null;
        });
    }

    public static ActionBuilder<Pipe<TestMessage>> theNumberOfCurrentlyDeliveredMessagesIsQueried() {
        return new ChannelActionBuilder((channel, testEnvironment) -> {
            final ChannelMessageBusSutActions sutActions = channelTestActions(channel);
            queryTheNumberOfCurrentlyDeliveredMessages(sutActions, testEnvironment);
            return null;
        });
    }

    public static ActionBuilder<Pipe<TestMessage>> theNumberOfCurrentlyTransportedMessagesIsQueried() {
        return new ChannelActionBuilder((channel, testEnvironment) -> {
            final ChannelMessageBusSutActions sutActions = channelTestActions(channel);
            queryTheNumberOfCurrentlyTransportedMessages(sutActions, testEnvironment);
            return null;
        });
    }

    public static ActionBuilder<Pipe<TestMessage>> theTimestampOfTheStatisticsIsQueried() {
        return new ChannelActionBuilder((channel, testEnvironment) -> {
            final ChannelMessageBusSutActions sutActions = channelTestActions(channel);
            queryTheTimestampOfTheMessageStatistics(sutActions, testEnvironment);
            return null;
        });
    }

    public static ActionBuilder<Pipe<TestMessage>> aShortWaitIsDone(final long timeout, final TimeUnit timeUnit) {
        return new ChannelActionBuilder((channel, testEnvironment) -> {
            performAShortWait(timeout, timeUnit);
            return null;
        });
    }

    public static ActionBuilder<Pipe<TestMessage>> severalMessagesAreSendAsynchronouslyBeforeTheChannelIsShutdown(final int numberOfSenders, final int numberOfMessages) {
        return new ChannelActionBuilder((channel, testEnvironment) -> {
            final ChannelMessageBusSutActions sutActions = channelTestActions(channel);
            sendSeveralMessagesAsynchronouslyBeforeTheObjectIsShutdown(sutActions, testEnvironment, numberOfSenders, numberOfMessages);
            return null;
        });
    }

    public static ActionBuilder<Pipe<TestMessage>> theChannelIsShutdownAsynchronouslyXTimes(final int numberOfThreads) {
        return new ChannelActionBuilder((channel, testEnvironment) -> {
            final ChannelMessageBusSutActions sutActions = channelTestActions(channel);
            shutdownTheObjectAsynchronouslyXTimes(sutActions, numberOfThreads);
            return null;
        });
    }

    public static ActionBuilder<Pipe<TestMessage>> theChannelIsShutdown() {
        return new ChannelActionBuilder((channel, testEnvironment) -> {
            final ChannelMessageBusSutActions sutActions = channelTestActions(channel);
            shutdownTheSut(sutActions);
            return null;
        });
    }

    public static ActionBuilder<Pipe<TestMessage>> theChannelIsShutdownAfterHalfOfTheMessagesWereDelivered(final int numberOfMessages) {
        final int numberOfMessagesBeforeShutdown = numberOfMessages / 2;
        final int remainingMessages = numberOfMessages - numberOfMessagesBeforeShutdown;
        return new ChannelActionBuilder((channel, testEnvironment) -> {
            final ChannelMessageBusSutActions sutActions = channelTestActions(channel);
            sendXMessagesAShutdownsIsCalledThenSendsYMessage(sutActions, testEnvironment, numberOfMessagesBeforeShutdown, remainingMessages, true);
            return null;
        });
    }

    public static ActionBuilder<Pipe<TestMessage>> theChannelIsShutdownAfterHalfOfTheMessagesWereDelivered_withoutFinishingRemainingTasks(final int numberOfMessages) {
        final int numberOfMessagesBeforeShutdown = numberOfMessages / 2;
        final int remainingMessages = numberOfMessages - numberOfMessagesBeforeShutdown;
        return new ChannelActionBuilder((channel, testEnvironment) -> {
            final ChannelMessageBusSutActions sutActions = channelTestActions(channel);
            sendXMessagesAShutdownsIsCalledThenSendsYMessage(sutActions, testEnvironment, numberOfMessagesBeforeShutdown, remainingMessages, false);
            return null;
        });
    }

    public static ActionBuilder<Pipe<TestMessage>> theChannelShutdownIsExpectedForTimeoutInSeconds(final int timeoutInSeconds) {
        return new ChannelActionBuilder((channel, testEnvironment) -> {
            final ChannelMessageBusSutActions sutActions = channelTestActions(channel);
            awaitTheShutdownTimeoutInSeconds(sutActions, testEnvironment, timeoutInSeconds);
            return null;
        });
    }

    public static ActionBuilder<Pipe<TestMessage>> awaitWithoutACloseIsCalled() {
        return new ChannelActionBuilder((channel, testEnvironment) -> {
            final ChannelMessageBusSutActions sutActions = channelTestActions(channel);
            callAwaitWithoutACloseIsCalled(sutActions, testEnvironment);
            return null;
        });
    }

    public static ActionBuilder<Pipe<TestMessage>> theListOfFiltersIsQueried() {
        return new ChannelActionBuilder((channel, testEnvironment) -> {
            final ChannelMessageBusSutActions sutActions = channelTestActions(channel);
            queryTheListOfFilters(sutActions, testEnvironment);
            return null;
        });
    }

    public static ActionBuilder<Pipe<TestMessage>> aFilterIsRemoved() {
        return new ChannelActionBuilder((channel, testEnvironment) -> {
            final ChannelMessageBusSutActions sutActions = channelTestActions(channel);
            removeAFilter(sutActions, testEnvironment);
            return null;
        });
    }

    @Override
    public ActionBuilder<Pipe<TestMessage>> andThen(final ActionBuilder<Pipe<TestMessage>> followUpBuilder) {
        if (followUpBuilder instanceof ChannelActionBuilder) {
            actions.addAll(((ChannelActionBuilder) followUpBuilder).actions);
        }
        return this;
    }

    @Override
    public List<TestAction<Pipe<TestMessage>>> build() {
        return actions;
    }

}
