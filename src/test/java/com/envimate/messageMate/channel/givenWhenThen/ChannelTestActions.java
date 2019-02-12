package com.envimate.messageMate.channel.givenWhenThen;

import com.envimate.messageMate.channel.Channel;
import com.envimate.messageMate.channel.ChannelStatusInformation;
import com.envimate.messageMate.filtering.Filter;
import com.envimate.messageMate.internal.statistics.MessageStatistics;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.shared.channelMessageBus.givenWhenThen.ChannelMessageBusSutActions;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import com.envimate.messageMate.subscribing.Subscriber;
import com.envimate.messageMate.subscribing.SubscriptionId;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class ChannelTestActions implements ChannelMessageBusSutActions {
    private final Channel<TestMessage> channel;

    public static ChannelTestActions channelTestActions(final Channel<TestMessage> channel) {
        return new ChannelTestActions(channel);
    }

    @Override
    public boolean isShutdown(final TestEnvironment testEnvironment) {
        return channel.isShutdown();
    }

    @Override
    public List<?> getFilter(final TestEnvironment testEnvironment) {
        return channel.getFilter();
    }

    @Override
    public <R> void subscribe(final Class<R> messageClass, final Subscriber<R> subscriber) {
        @SuppressWarnings("unchecked")
        final Subscriber<TestMessage> messageSubscriber = (Subscriber<TestMessage>) subscriber;
        channel.subscribe(messageSubscriber);
    }

    @Override
    public void close(final boolean finishRemainingTasks) {
        channel.close(finishRemainingTasks);
    }

    @Override
    public boolean awaitTermination(final int timeout, final TimeUnit timeUnit) throws InterruptedException {
        return channel.awaitTermination(timeout, timeUnit);
    }

    @Override
    public List<?> getFilter() {
        final List<?> filters = channel.getFilter();
        return filters;
    }

    @Override
    public void unsubscribe(final SubscriptionId subscriptionId) {
        channel.unsubscribe(subscriptionId);
    }

    @Override
    public void send(final TestMessage message) {
        channel.send(message);
    }

    @Override
    public MessageStatistics getMessageStatistics() {
        final ChannelStatusInformation<TestMessage> statusInformation = channel.getStatusInformation();
        return statusInformation.getCurrentMessageStatistics();
    }

    @Override
    public Object removeAFilter() {
        final List<Filter<TestMessage>> filters = channel.getFilter();
        final int indexToRemove = (int) (Math.random() * filters.size());
        final Filter<TestMessage> filter = filters.get(indexToRemove);
        channel.remove(filter);
        return filter;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addFilter(final Filter<?> filter) {
        final Filter<TestMessage> testMessageFilter = (Filter<TestMessage>) filter;
        channel.add(testMessageFilter);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addFilter(final Filter<?> filter, final int position) {
        final Filter<TestMessage> testMessageFilter = (Filter<TestMessage>) filter;
        channel.add(testMessageFilter, position);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Subscriber<?>> getAllSubscribers() {
        final ChannelStatusInformation<TestMessage> statusInformation = channel.getStatusInformation();
        final List<?> allSubscribers = statusInformation.getAllSubscribers();
        return (List<Subscriber<?>>) allSubscribers;
    }
}
