package com.envimate.messageMate.messageBus.givenWhenThen;

import com.envimate.messageMate.filtering.Filter;
import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.messageBus.MessageBusStatusInformation;
import com.envimate.messageMate.pipe.statistics.PipeStatistics;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeMessageBusSutActions;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import com.envimate.messageMate.subscribing.Subscriber;
import com.envimate.messageMate.subscribing.SubscriptionId;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class MessageBusTestActions implements PipeMessageBusSutActions {
    private final MessageBus messageBus;

    public static MessageBusTestActions messageBusTestActions(final MessageBus messageBus) {
        return new MessageBusTestActions(messageBus);
    }

    @Override
    public boolean isShutdown(final TestEnvironment testEnvironment) {
        return messageBus.isShutdown();
    }

    @Override
    public List<?> getFilter(final TestEnvironment testEnvironment) {
        return messageBus.getFilter();
    }

    @Override
    public <R> void subscribe(final Class<R> messageClass, final Subscriber<R> subscriber) {
        messageBus.subscribe(messageClass, subscriber);
    }

    @Override
    public void close(final boolean finishRemainingTasks) {
        messageBus.close(finishRemainingTasks);
    }

    @Override
    public boolean awaitTermination(final int timeout, final TimeUnit timeUnit) throws InterruptedException {
        return messageBus.awaitTermination(timeout, timeUnit);
    }

    @Override
    public List<?> getFilter() {
        return messageBus.getFilter();
    }

    @Override
    public void unsubscribe(final SubscriptionId subscriptionId) {
        messageBus.unsubcribe(subscriptionId);
    }

    @Override
    public void send(final TestMessage message) {
        messageBus.send(message);
    }

    @Override
    public PipeStatistics getMessageStatistics() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object removeAFilter() {
        final List<Filter<Object>> filters = messageBus.getFilter();
        final int indexToRemove = (int) (Math.random() * filters.size());
        final Filter<Object> filter = filters.get(indexToRemove);
        messageBus.remove(filter);
        return filter;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addFilter(final Filter<?> filter) {
        final Filter<Object> objectFilter = (Filter<Object>) filter;
        messageBus.add(objectFilter);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addFilter(final Filter<?> filter, final int position) {
        final Filter<Object> objectFilter = (Filter<Object>) filter;
        messageBus.add(objectFilter, position);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Subscriber<?>> getAllSubscribers() {
        final MessageBusStatusInformation statusInformation = messageBus.getStatusInformation();
        final List<?> allSubscribers = statusInformation.getAllSubscribers();
        return (List<Subscriber<?>>) allSubscribers;
    }
}
