package com.envimate.messageMate.channel.statistics;

public interface PartialCollectingChannelStatisticsCollector extends ChannelStatisticsCollector {

    void informMessageReplaced();

    void informMessageBlocked();

    void informMessageForgotten();

    void informExceptionInFilterThrown();
}
