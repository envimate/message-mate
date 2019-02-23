package com.envimate.messageMate.channel.events;

import com.envimate.messageMate.channel.statistics.PartialCollectingChannelStatisticsCollector;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class SimpleChannelEventListener<T> implements ChannelEventListener<T> {
    private final PartialCollectingChannelStatisticsCollector statisticsCollector;

    public static <T> SimpleChannelEventListener<T> simpleChannelEventListener(
            final PartialCollectingChannelStatisticsCollector statisticsCollector) {
        return new SimpleChannelEventListener<>(statisticsCollector);
    }

    @Override
    public void messageReplaced(final T message) {
        statisticsCollector.informMessageReplaced();
    }

    @Override
    public void messageBlocked(final T message) {
        statisticsCollector.informMessageBlocked();
    }

    @Override
    public void messageForgotten(final T message) {
        statisticsCollector.informMessageForgotten();
    }

    @Override
    public void exceptionInFilter(final T message, final Exception e) {
        statisticsCollector.informExceptionInFilterThrown();
    }
}
