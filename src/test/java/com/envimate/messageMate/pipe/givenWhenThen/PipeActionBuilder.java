package com.envimate.messageMate.pipe.givenWhenThen;


import com.envimate.messageMate.pipe.Pipe;
import com.envimate.messageMate.qcec.shared.TestAction;
import com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.ActionBuilder;
import com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeMessageBusSutActions;
import com.envimate.messageMate.shared.testMessages.TestMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.envimate.messageMate.pipe.givenWhenThen.PipeTestActions.pipeTestActions;
import static com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeMessageBusTestActions.*;

/*
TODO: query subscriber
 */
public final class PipeActionBuilder implements ActionBuilder<Pipe<TestMessage>> {
    private List<TestAction<Pipe<TestMessage>>> actions = new ArrayList<>();

    private PipeActionBuilder(final TestAction<Pipe<TestMessage>> action) {
        this.actions.add(action);
    }

    public static ActionBuilder<Pipe<TestMessage>> aSingleMessageIsSend() {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = pipeTestActions(pipe);
            sendASingleMessage(sutActions, testEnvironment);
            return null;
        });
    }

    public static ActionBuilder<Pipe<TestMessage>> severalMessagesAreSend(final int numberOfMessages) {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = pipeTestActions(pipe);
            sendSeveralMessages(sutActions, testEnvironment, numberOfMessages);
            return null;
        });
    }

    public static ActionBuilder<Pipe<TestMessage>> severalMessagesAreSendAsynchronously(final int numberOfSender, final int numberOfMessagesPerSender) {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = pipeTestActions(pipe);
            sendSeveralMessagesInTheirOwnThread(sutActions, testEnvironment, numberOfSender, numberOfMessagesPerSender, true);
            return null;
        });
    }

    public static ActionBuilder<Pipe<TestMessage>> severalMessagesAreSendAsynchronouslyButWillBeBlocked(final int numberOfSender, final int numberOfMessagesPerSender) {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = pipeTestActions(pipe);
            sendSeveralMessagesInTheirOwnThread(sutActions, testEnvironment, numberOfSender, numberOfMessagesPerSender, false);
            return null;
        });
    }

    public static ActionBuilder<Pipe<TestMessage>> oneSubscriberUnsubscribesSeveralTimes(final int numberOfUnsubscriptions) {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = pipeTestActions(pipe);
            unsubscribeASubscriberXTimes(sutActions, testEnvironment, numberOfUnsubscriptions);
            return null;
        });
    }

    public static ActionBuilder<Pipe<TestMessage>> oneSubscriberUnsubscribes() {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = pipeTestActions(pipe);
            unsubscribeASubscriberXTimes(sutActions, testEnvironment, 1);
            return null;
        });
    }

    public static ActionBuilder<Pipe<TestMessage>> bothValidAndInvalidMessagesAreSendAsynchronously(final int numberOfSender, final int numberOfMessagesPerSender) {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = pipeTestActions(pipe);
            sendBothValidAndInvalidMessagesAsynchronously(sutActions, testEnvironment, numberOfSender, numberOfMessagesPerSender);
            return null;
        });
    }

    public static ActionBuilder<Pipe<TestMessage>> severalInvalidMessagesAreSendAsynchronously(final int numberOfSender, final int numberOfMessagesPerSender) {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = pipeTestActions(pipe);
            sendSeveralInvalidMessagesAsynchronously(sutActions, testEnvironment, numberOfSender, numberOfMessagesPerSender);
            return null;
        });
    }

    public static ActionBuilder<Pipe<TestMessage>> theNumberOfAcceptedMessagesIsQueried() {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = pipeTestActions(pipe);
            queryTheNumberOfAcceptedMessages(sutActions, testEnvironment);
            return null;
        });
    }

    public static ActionBuilder<Pipe<TestMessage>> theNumberOfAcceptedMessagesIsQueriedAsynchronously() {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = pipeTestActions(pipe);
            queryTheNumberOfAcceptedMessagesAsynchronously(sutActions, testEnvironment);
            return null;
        });
    }

    public static ActionBuilder<Pipe<TestMessage>> theNumberOfWaitingMessagesIsQueried() {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = pipeTestActions(pipe);
            queryTheNumberOfWaitingMessages(sutActions, testEnvironment);
            return null;
        });
    }

    public static ActionBuilder<Pipe<TestMessage>> theNumberOfSuccessfulMessagesIsQueried() {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = pipeTestActions(pipe);
            queryTheNumberOfSuccessfulDeliveredMessages(sutActions, testEnvironment);
            return null;
        });
    }

    public static ActionBuilder<Pipe<TestMessage>> theNumberOfFailedMessagesIsQueried() {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = pipeTestActions(pipe);
            queryTheNumberOfFailedDeliveredMessages(sutActions, testEnvironment);
            return null;
        });
    }

    public static ActionBuilder<Pipe<TestMessage>> theNumberOfCurrentlyDeliveredMessagesIsQueried() {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = pipeTestActions(pipe);
            queryTheNumberOfCurrentlyDeliveredMessages(sutActions, testEnvironment);
            return null;
        });
    }

    public static ActionBuilder<Pipe<TestMessage>> theNumberOfCurrentlyTransportedMessagesIsQueried() {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = pipeTestActions(pipe);
            queryTheNumberOfCurrentlyTransportedMessages(sutActions, testEnvironment);
            return null;
        });
    }

    public static ActionBuilder<Pipe<TestMessage>> theTimestampOfTheStatisticsIsQueried() {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = pipeTestActions(pipe);
            queryTheTimestampOfTheMessageStatistics(sutActions, testEnvironment);
            return null;
        });
    }

    public static ActionBuilder<Pipe<TestMessage>> aShortWaitIsDone(final long timeout, final TimeUnit timeUnit) {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            performAShortWait(timeout, timeUnit);
            return null;
        });
    }

    public static ActionBuilder<Pipe<TestMessage>> severalMessagesAreSendAsynchronouslyBeforeThePipeIsShutdown(final int numberOfSenders, final int numberOfMessages) {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = pipeTestActions(pipe);
            sendSeveralMessagesAsynchronouslyBeforeTheObjectIsShutdown(sutActions, testEnvironment, numberOfSenders, numberOfMessages);
            return null;
        });
    }

    public static ActionBuilder<Pipe<TestMessage>> thePipeIsShutdownAsynchronouslyXTimes(final int numberOfThreads) {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = pipeTestActions(pipe);
            shutdownTheObjectAsynchronouslyXTimes(sutActions, numberOfThreads);
            return null;
        });
    }

    public static ActionBuilder<Pipe<TestMessage>> thePipeIsShutdown() {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = pipeTestActions(pipe);
            shutdownTheSut(sutActions);
            return null;
        });
    }

    public static ActionBuilder<Pipe<TestMessage>> thePipeIsShutdownAfterHalfOfTheMessagesWereDelivered(final int numberOfMessages) {
        final int numberOfMessagesBeforeShutdown = numberOfMessages / 2;
        final int remainingMessages = numberOfMessages - numberOfMessagesBeforeShutdown;
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = pipeTestActions(pipe);
            sendXMessagesAShutdownsIsCalledThenSendsYMessage(sutActions, testEnvironment, numberOfMessagesBeforeShutdown, remainingMessages, true);
            return null;
        });
    }

    public static ActionBuilder<Pipe<TestMessage>> thePipeIsShutdownAfterHalfOfTheMessagesWereDelivered_withoutFinishingRemainingTasks(final int numberOfMessages) {
        final int numberOfMessagesBeforeShutdown = numberOfMessages / 2;
        final int remainingMessages = numberOfMessages - numberOfMessagesBeforeShutdown;
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = pipeTestActions(pipe);
            sendXMessagesAShutdownsIsCalledThenSendsYMessage(sutActions, testEnvironment, numberOfMessagesBeforeShutdown, remainingMessages, false);
            return null;
        });
    }

    public static ActionBuilder<Pipe<TestMessage>> thePipeShutdownIsExpectedForTimeoutInSeconds(final int timeoutInSeconds) {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = pipeTestActions(pipe);
            awaitTheShutdownTimeoutInSeconds(sutActions, testEnvironment, timeoutInSeconds);
            return null;
        });
    }

    public static ActionBuilder<Pipe<TestMessage>> awaitWithoutACloseIsCalled() {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = pipeTestActions(pipe);
            callAwaitWithoutACloseIsCalled(sutActions, testEnvironment);
            return null;
        });
    }

    public static ActionBuilder<Pipe<TestMessage>> theListOfFiltersIsQueried() {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = pipeTestActions(pipe);
            queryTheListOfFilters(sutActions, testEnvironment);
            return null;
        });
    }

    public static ActionBuilder<Pipe<TestMessage>> aFilterIsRemoved() {
        return new PipeActionBuilder((pipe, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = pipeTestActions(pipe);
            removeAFilter(sutActions, testEnvironment);
            return null;
        });
    }

    @Override
    public ActionBuilder<Pipe<TestMessage>> andThen(final ActionBuilder<Pipe<TestMessage>> followUpBuilder) {
        if (followUpBuilder instanceof PipeActionBuilder) {
            actions.addAll(((PipeActionBuilder) followUpBuilder).actions);
        }
        return this;
    }

    @Override
    public List<TestAction<Pipe<TestMessage>>> build() {
        return actions;
    }

}
