package com.envimate.messageMate.messageBus.channelCreating;

import com.envimate.messageMate.channel.Channel;
import com.envimate.messageMate.subscribing.Subscriber;
import lombok.RequiredArgsConstructor;

import static com.envimate.messageMate.channel.ChannelBuilder.aChannel;
import static com.envimate.messageMate.channel.action.Subscription.subscription;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class SynchronousMessageBusChannelFactory implements MessageBusChannelFactory {

    public static SynchronousMessageBusChannelFactory synchronousMessageBusChannelFactory(){
        return new SynchronousMessageBusChannelFactory();
    }

    @Override
    public <T> Channel<?> createChannel(final Class<T> tClass, final Subscriber<T> subscriber) {
        return aChannel(tClass)
                .withDefaultAction(subscription())
                .build();
    }
}
