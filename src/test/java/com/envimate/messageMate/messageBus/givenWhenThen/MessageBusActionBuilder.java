package com.envimate.messageMate.messageBus.givenWhenThen;


import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.messageBus.MessageBusStatusInformation;
import com.envimate.messageMate.qcec.shared.TestAction;
import com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.ActionBuilder;
import com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeMessageBusSutActions;
import com.envimate.messageMate.subscribing.Subscriber;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.envimate.messageMate.messageBus.givenWhenThen.MessageBusTestActions.messageBusTestActions;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.RESULT;
import static com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeMessageBusTestActions.*;


//TODO: a lot of unnessary nulls
public final class MessageBusActionBuilder implements ActionBuilder<MessageBus> {
    private List<TestAction<MessageBus>> actions = new ArrayList<>();

    private MessageBusActionBuilder(final TestAction<MessageBus> action) {
        this.actions.add(action);
    }

    public static ActionBuilder<MessageBus> aSingleMessageIsSend() {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = messageBusTestActions(messageBus);
            sendASingleMessage(sutActions, testEnvironment);
            return null;
        });
    }

    public static ActionBuilder<MessageBus> severalMessagesAreSend(final int numberOfMessages) {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = messageBusTestActions(messageBus);
            sendSeveralMessages(sutActions, testEnvironment, numberOfMessages);
            return null;
        });
    }

    public static ActionBuilder<MessageBus> severalMessagesAreSendAsynchronously(final int numberOfSender, final int numberOfMessagesPerSender) {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = messageBusTestActions(messageBus);
            sendSeveralMessagesInTheirOwnThread(sutActions, testEnvironment, numberOfSender, numberOfMessagesPerSender, true);
            return null;
        });
    }

    public static ActionBuilder<MessageBus> severalMessagesAreSendAsynchronouslyButWillBeBlocked(final int numberOfSender, final int numberOfMessagesPerSender) {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = messageBusTestActions(messageBus);
            sendSeveralMessagesInTheirOwnThread(sutActions, testEnvironment, numberOfSender, numberOfMessagesPerSender, false);
            return null;
        });
    }

    public static ActionBuilder<MessageBus> oneSubscriberUnsubscribesSeveralTimes(final int numberOfUnsubscriptions) {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = messageBusTestActions(messageBus);
            unsubscribeASubscriberXTimes(sutActions, testEnvironment, numberOfUnsubscriptions);
            return null;
        });
    }

    public static ActionBuilder<MessageBus> oneSubscriberUnsubscribes() {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = messageBusTestActions(messageBus);
            unsubscribeASubscriberXTimes(sutActions, testEnvironment, 1);
            return null;
        });
    }

    public static ActionBuilder<MessageBus> halfValidAndInvalidMessagesAreSendAsynchronously(final int numberOfSender, final int numberOfMessagesPerSender) {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = messageBusTestActions(messageBus);
            sendBothValidAndInvalidMessagesAsynchronously(sutActions, testEnvironment, numberOfSender, numberOfMessagesPerSender);
            return null;
        });
    }

    public static ActionBuilder<MessageBus> severalInvalidMessagesAreSendAsynchronously(final int numberOfSender, final int numberOfMessagesPerSender) {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = messageBusTestActions(messageBus);
            sendSeveralInvalidMessagesAsynchronously(sutActions, testEnvironment, numberOfSender, numberOfMessagesPerSender);
            return null;
        });
    }

    public static ActionBuilder<MessageBus> theNumberOfAcceptedMessagesIsQueried() {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = messageBusTestActions(messageBus);
            queryTheNumberOfAcceptedMessages(sutActions, testEnvironment);
            return null;
        });
    }

    public static ActionBuilder<MessageBus> theNumberOfAcceptedMessagesIsQueriedAsynchronously() {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = messageBusTestActions(messageBus);
            queryTheNumberOfAcceptedMessagesAsynchronously(sutActions, testEnvironment);
            return null;
        });
    }

    public static ActionBuilder<MessageBus> theNumberOfWaitingMessagesIsQueried() {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = messageBusTestActions(messageBus);
            queryTheNumberOfQueuedMessages(sutActions, testEnvironment);
            return null;
        });
    }

    public static ActionBuilder<MessageBus> theNumberOfSuccessfulMessagesIsQueried() {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = messageBusTestActions(messageBus);
            queryTheNumberOfSuccessfulDeliveredMessages(sutActions, testEnvironment);
            return null;
        });
    }

    public static ActionBuilder<MessageBus> theNumberOfFailedMessagesIsQueried() {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = messageBusTestActions(messageBus);
            queryTheNumberOfFailedDeliveredMessages(sutActions, testEnvironment);
            return null;
        });
    }

    public static ActionBuilder<MessageBus> theNumberOfDroppedMessagesIsQueried() {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = messageBusTestActions(messageBus);
            queryTheNumberOfDroppedMessages(sutActions, testEnvironment);
            return null;
        });
    }

    public static ActionBuilder<MessageBus> theNumberOfReplacedMessagesIsQueried() {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = messageBusTestActions(messageBus);
            queryTheNumberOfReplacedMessages(sutActions, testEnvironment);
            return null;
        });
    }

    public static ActionBuilder<MessageBus> theNumberOfForgottenMessagesIsQueried() {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = messageBusTestActions(messageBus);
            queryTheNumberOfForgottenMessages(sutActions, testEnvironment);
            return null;
        });
    }

    public static ActionBuilder<MessageBus> theNumberOfCurrentlyDeliveredMessagesIsQueried() {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = messageBusTestActions(messageBus);
            queryTheNumberOfCurrentlyDeliveredMessages(sutActions, testEnvironment);
            return null;
        });
    }

    public static ActionBuilder<MessageBus> theNumberOfCurrentlyTransportedMessagesIsQueried() {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = messageBusTestActions(messageBus);
            queryTheNumberOfCurrentlyTransportedMessages(sutActions, testEnvironment);
            return null;
        });
    }

    public static ActionBuilder<MessageBus> theTimestampOfTheStatisticsIsQueried() {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = messageBusTestActions(messageBus);
            queryTheTimestampOfTheMessageStatistics(sutActions, testEnvironment);
            return null;
        });
    }

    public static ActionBuilder<MessageBus> aShortWaitIsDone(final long timeout, final TimeUnit timeUnit) {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            performAShortWait(timeout, timeUnit);
            return null;
        });
    }

    public static ActionBuilder<MessageBus> theSubscriberAreQueriedPerType() {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            //TODO: ev noch falsch hier
            final PipeMessageBusSutActions sutActions = messageBusTestActions(messageBus);
            final MessageBusStatusInformation statusInformation = messageBus.getStatusInformation();
            final Map<Object, List<Subscriber<Object>>> subscribersPerType = statusInformation.getSubscribersPerType();
            testEnvironment.setProperty(RESULT, subscribersPerType);
            return null;
        });
    }

    public static ActionBuilder<MessageBus> allSubscribersAreQueriedAsList() {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            //TODO: in sutActions -> make usable for pipe too
            final PipeMessageBusSutActions sutActions = messageBusTestActions(messageBus);
            final List<Subscriber<Object>> allSubscribers = messageBus.getStatusInformation().getAllSubscribers();
            testEnvironment.setProperty(RESULT, allSubscribers);
            return null;
        });
    }

    public static ActionBuilder<MessageBus> severalMessagesAreSendAsynchronouslyBeforeTheMessageBusIsShutdown(final int numberOfSenders, final int numberOfMessages) {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = messageBusTestActions(messageBus);
            sendSeveralMessagesAsynchronouslyBeforeTheObjectIsShutdown(sutActions, testEnvironment, numberOfSenders, numberOfMessages);
            return null;
        });
    }

    public static ActionBuilder<MessageBus> theBusIsShutdownAfterHalfOfTheMessagesWereDelivered(final int numberOfMessages) {
        final int numberOfMessagesBeforeShutdown = numberOfMessages / 2;
        final int remainingMessages = numberOfMessages - numberOfMessagesBeforeShutdown;
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = messageBusTestActions(messageBus);
            sendXMessagesAShutdownsIsCalledThenSendsYMessage(sutActions, testEnvironment, numberOfMessagesBeforeShutdown, remainingMessages, true);
            return null;
        });
    }

    public static ActionBuilder<MessageBus> theBusIsShutdownAfterHalfOfTheMessagesWereDelivered_withoutFinishingRemainingTasks(final int numberOfMessages) {
        final int numberOfMessagesBeforeShutdown = numberOfMessages / 2;
        final int remainingMessages = numberOfMessages - numberOfMessagesBeforeShutdown;
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = messageBusTestActions(messageBus);
            sendXMessagesAShutdownsIsCalledThenSendsYMessage(sutActions, testEnvironment, numberOfMessagesBeforeShutdown, remainingMessages, false);
            return null;
        });
    }

    public static ActionBuilder<MessageBus> theMessageBusIsShutdownAsynchronouslyXTimes(final int numberOfThreads) {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = messageBusTestActions(messageBus);
            shutdownTheObjectAsynchronouslyXTimes(sutActions, numberOfThreads);
            return null;
        });
    }

    public static ActionBuilder<MessageBus> theMessageBusIsShutdown() {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = messageBusTestActions(messageBus);
            shutdownTheSut(sutActions);
            return null;
        });
    }

    public static ActionBuilder<MessageBus> theMessageBusShutdownIsExpectedForTimeoutInSeconds(final int timeoutInSeconds) {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = messageBusTestActions(messageBus);
            awaitTheShutdownTimeoutInSeconds(sutActions, testEnvironment, timeoutInSeconds);
            return null;
        });
    }

    public static ActionBuilder<MessageBus> theListOfFiltersIsQueried() {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = messageBusTestActions(messageBus);
            queryTheListOfFilters(sutActions, testEnvironment);
            return null;
        });
    }

    public static ActionBuilder<MessageBus> aFilterIsRemoved() {
        return new MessageBusActionBuilder((messageBus, testEnvironment) -> {
            final PipeMessageBusSutActions sutActions = messageBusTestActions(messageBus);
            removeAFilter(sutActions, testEnvironment);
            return null;
        });
    }

    @Override
    public ActionBuilder<MessageBus> andThen(final ActionBuilder<MessageBus> followUpBuilder) {
        if (followUpBuilder instanceof MessageBusActionBuilder) {
            actions.addAll(((MessageBusActionBuilder) followUpBuilder).actions);
        }
        return this;
    }

    @Override
    public List<TestAction<MessageBus>> build() {
        return actions;
    }
}
