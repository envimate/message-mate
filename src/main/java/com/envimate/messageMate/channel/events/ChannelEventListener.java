package com.envimate.messageMate.channel.events;

public interface ChannelEventListener<T> {

    void messageReplaced(T message);

    void messageBlocked(T message);

    void messageForgotten(T message);

    void exceptionInFilter(T message, Exception e);
}
