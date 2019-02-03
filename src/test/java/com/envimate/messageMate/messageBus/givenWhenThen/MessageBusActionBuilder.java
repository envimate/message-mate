package com.envimate.messageMate.messageBus.givenWhenThen;


import com.envimate.messageMate.filtering.Filter;
import com.envimate.messageMate.internal.statistics.MessageStatistics;
import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.shared.givenWhenThen.ActionBuilder;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import com.envimate.messageMate.subscribing.Subscriber;
import com.envimate.messageMate.subscribing.SubscriptionId;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.envimate.messageMate.shared.context.TestExecutionProperty.RESULT;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class MessageBusActionBuilder extends ActionBuilder<MessageBus> {


    public static ActionBuilder<MessageBus> aSingleMessageIsSend() {
        return new MessageBusActionBuilder()
                .thatSendsASingleMessage();
    }

    public static ActionBuilder<MessageBus> severalMessagesAreSend(final int numberOfMessages) {
        return new MessageBusActionBuilder()
                .thatSendsSeveralMessages(numberOfMessages);
    }

    public static ActionBuilder<MessageBus> severalMessagesAreSendAsynchronously(final int numberOfSender, final int numberOfMessagesPerSender) {
        return new MessageBusActionBuilder()
                .thatSendsSeveralMessagesAsynchronously(numberOfSender, numberOfMessagesPerSender);
    }

    public static ActionBuilder<MessageBus> severalMessagesAreSendAsynchronouslyButWillBeBlocked(final int numberOfSender, final int numberOfMessagesPerSender) {
        return new MessageBusActionBuilder()
                .thatSendsSeveralMessagesAsynchronouslyButWillBeBlocked(numberOfSender, numberOfMessagesPerSender);
    }

    public static ActionBuilder<MessageBus> oneSubscriberUnsubscribesSeveralTimes(final int numberOfUnsubscriptions) {
        return new MessageBusActionBuilder()
                .thatUnsubscribesASubscriberSeveralTimes(numberOfUnsubscriptions);
    }

    public static ActionBuilder<MessageBus> oneSubscriberUnsubscribes() {
        return new MessageBusActionBuilder()
                .thatUnsubscribesASubscriber();
    }

    public static ActionBuilder<MessageBus> halfValidAndInvalidMessagesAreSendAsynchronously(final int numberOfSender, final int numberOfMessagesPerSender) {
        return new MessageBusActionBuilder()
                .thatSendsBothValidAndInvalidMessagesAsynchronously(numberOfSender, numberOfMessagesPerSender);
    }

    public static ActionBuilder<MessageBus> severalInvalidMessagesAreSendAsynchronously(final int numberOfSender, final int numberOfMessagesPerSender) {
        return new MessageBusActionBuilder()
                .thatSendsSeveralInvalidMessagesAsynchronously(numberOfSender, numberOfMessagesPerSender);
    }

    public static ActionBuilder<MessageBus> theNumberOfAcceptedMessagesIsQueried() {
        return new MessageBusActionBuilder()
                .thatQueriesTheNumberOfAcceptedMessages();
    }

    public static ActionBuilder<MessageBus> theNumberOfAcceptedMessagesIsQueriedAsynchronously() {
        return new MessageBusActionBuilder()
                .thatQueriesTheNumberOfAcceptedMessagesAsynchronously();
    }

    public static ActionBuilder<MessageBus> theNumberOfWaitingMessagesIsQueried() {
        return new MessageBusActionBuilder()
                .thatQueriesTheNumberOfWaitingMessages();
    }

    public static ActionBuilder<MessageBus> theNumberOfSuccessfulMessagesIsQueried() {
        return new MessageBusActionBuilder()
                .thatQueriesTheNumberOfSuccessfulDeliveredMessages();
    }

    public static ActionBuilder<MessageBus> theNumberOfFailedMessagesIsQueried() {
        return new MessageBusActionBuilder()
                .thatQueriesTheNumberOfFailedDeliveredMessages();
    }

    public static ActionBuilder<MessageBus> theNumberOfDroppedMessagesIsQueried() {
        return new MessageBusActionBuilder()
                .thatQueriesTheNumberOfDroppedMessages();
    }

    public static ActionBuilder<MessageBus> theNumberOfReplacedMessagesIsQueried() {
        return new MessageBusActionBuilder()
                .thatQueriesTheNumberOfReplacedMessages();
    }

    public static ActionBuilder<MessageBus> theNumberOfForgottenMessagesIsQueried() {
        return new MessageBusActionBuilder()
                .thatQueriesTheNumberOfForgottenMessages();
    }

    public static ActionBuilder<MessageBus> theNumberOfCurrentlyDeliveredMessagesIsQueried() {
        return new MessageBusActionBuilder()
                .thatQueriesTheNumberOfCurrentlyDeliveredMessages();
    }

    public static ActionBuilder<MessageBus> theNumberOfCurrentlyTransportedMessagesIsQueried() {
        return new MessageBusActionBuilder()
                .thatQueriesTheNumberOfCurrentlyTransportedMessages();
    }

    public static ActionBuilder<MessageBus> theTimestampOfTheStatisticsIsQueried() {
        return new MessageBusActionBuilder()
                .thatQueriesTheTimestampOfTheMessageStatistics();
    }

    public static ActionBuilder<MessageBus> aShortWaitIsDone(final long timeout, final TimeUnit timeUnit) {
        return new MessageBusActionBuilder()
                .thatPerformsAShortWait(timeout, timeUnit);
    }

    public static ActionBuilder<MessageBus> theSubscriberAreQueriedPerType() {
        return new MessageBusActionBuilder()
                .withAnAction((messageBus, executionContext) -> {
                    final Map<Object, List<Subscriber<Object>>> subscriberMap = messageBus.getStatusInformation().getSubscribersPerType();
                    executionContext.setProperty(RESULT, subscriberMap);
                });
    }

    public static ActionBuilder<MessageBus> allSubscribersAreQueriedAsList() {
        return new MessageBusActionBuilder()
                .withAnAction((messageBus, executionContext) -> {
                    final List<Subscriber<Object>> subscribers = messageBus.getStatusInformation().getAllSubscribers();
                    executionContext.setProperty(RESULT, subscribers);
                });
    }

    public static ActionBuilder<MessageBus> severalMessagesAreSendAsynchronouslyBeforeTheMessageBusIsShutdown(final int numberOfSenders, final int numberOfMessages) {
        return new MessageBusActionBuilder()
                .thatSendsSeveralMessagesAsynchronouslyBeforeTheObjectIsShutdown(numberOfSenders, numberOfMessages);
    }

    public static ActionBuilder<MessageBus> theBusIsShutdownAfterHalfOfTheMessagesWereDelivered(final int numberOfMessages) {
        final int numberOfMessagesBeforeShutdown = numberOfMessages / 2;
        final int remainingMessages = numberOfMessages - numberOfMessagesBeforeShutdown;
        return new MessageBusActionBuilder()
                .thatSendsXMessagesAShutdownsIsCalledThenSendsYMessage(numberOfMessagesBeforeShutdown, remainingMessages, true);
    }

    public static ActionBuilder<MessageBus> theBusIsShutdownAfterHalfOfTheMessagesWereDelivered_withoutFinishingRemainingTasks(final int numberOfMessages) {
        final int numberOfMessagesBeforeShutdown = numberOfMessages / 2;
        final int remainingMessages = numberOfMessages - numberOfMessagesBeforeShutdown;
        return new MessageBusActionBuilder()
                .thatSendsXMessagesAShutdownsIsCalledThenSendsYMessage(numberOfMessagesBeforeShutdown, remainingMessages, false);
    }

    public static ActionBuilder<MessageBus> theMessageBusIsShutdownAsynchronouslyXTimes(final int numberOfThreads) {
        return new MessageBusActionBuilder()
                .thatShutdownsTheObjectAsynchronouslyXTimes(numberOfThreads);
    }

    public static ActionBuilder<MessageBus> theMessageBusIsShutdown() {
        return new MessageBusActionBuilder()
                .thatShutdownsTheObject();
    }

    public static ActionBuilder<MessageBus> theMessageBusShutdownIsExpectedForTimeoutInSeconds(final int timeoutInSeconds) {
        return new MessageBusActionBuilder()
                .thatAwaitsTheShutdownTimeoutInSeconds(timeoutInSeconds);
    }

    public static ActionBuilder<MessageBus> theListOfFiltersIsQueried() {
        return new MessageBusActionBuilder()
                .thatQueriesTheListOfFilters();
    }

    public static ActionBuilder<MessageBus> aFilterIsRemoved() {
        return new MessageBusActionBuilder()
                .thatRemovesAFilter();
    }

    @Override
    public void send(final MessageBus messageBus, final TestMessage message) {
        messageBus.send(message);
    }

    @Override
    protected void unsubscribe(final MessageBus messageBus, final SubscriptionId subscriptionId) {
        messageBus.unsubcribe(subscriptionId);
    }

    @Override
    protected MessageStatistics getMessageStatistics(final MessageBus messageBus) {
        return messageBus.getStatusInformation().getCurrentMessageStatistics();
    }

    @Override
    protected <R> void subscribe(final MessageBus messageBus, final Class<R> messageClass, final Subscriber<R> subscriber) {
        messageBus.subscribe(messageClass, subscriber);
    }

    @Override
    protected void close(final MessageBus messageBus, final boolean finishRemainingTasks) {
        messageBus.close(finishRemainingTasks);
    }

    @Override
    protected boolean awaitTermination(final MessageBus messageBus, final int timeout, final TimeUnit timeUnit) throws InterruptedException {
        return messageBus.awaitTermination(timeout, timeUnit);
    }

    @Override
    protected List<?> getFilter(final MessageBus messageBus) {
        return messageBus.getFilter();
    }

    @Override
    protected Object removeAFilter(final MessageBus messageBus) {
        final List<Filter<Object>> filters = messageBus.getFilter();
        final int indexToRemove = (int) (Math.random() * filters.size());
        final Filter<Object> filter = filters.get(indexToRemove);
        messageBus.remove(filter);
        return filter;
    }
}
