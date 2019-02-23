package com.envimate.messageMate.messageBus.channelCreating;

import com.envimate.messageMate.channel.Channel;
import com.envimate.messageMate.subscribing.Subscriber;

public interface MessageBusChannelFactory {
    <T> Channel<?> createChannel(Class<T> tClass, Subscriber<T> subscriber);
}
