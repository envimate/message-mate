package com.envimate.messageMate.messageBus.channelCreating;

import com.envimate.messageMate.channel.Channel;
import com.envimate.messageMate.messageBus.error.DelegatingChannelExceptionHandlerForDelieveryChannel;
import com.envimate.messageMate.messageBus.error.MessageBusExceptionHandler;
import com.envimate.messageMate.subscribing.Subscriber;
import lombok.RequiredArgsConstructor;

import static com.envimate.messageMate.channel.ChannelBuilder.aChannel;
import static com.envimate.messageMate.channel.action.Subscription.subscription;
import static com.envimate.messageMate.messageBus.error.DelegatingChannelExceptionHandlerForDelieveryChannel.delegatingChannelExceptionHandlerForDeliveryChannel;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class SynchronousMessageBusChannelFactory implements MessageBusChannelFactory {
    private final MessageBusExceptionHandler exceptionHandler;

    public static SynchronousMessageBusChannelFactory synchronousMessageBusChannelFactory(final MessageBusExceptionHandler exceptionHandler) {
        return new SynchronousMessageBusChannelFactory(exceptionHandler);
    }

    @Override
    public <T> Channel<?> createChannel(final Class<T> tClass, final Subscriber<T> subscriber) {
        final DelegatingChannelExceptionHandlerForDelieveryChannel<T> delegatingChannelExceptionHandler = delegatingChannelExceptionHandlerForDeliveryChannel(exceptionHandler);
        final Channel<T> channel = aChannel(tClass)
                .withDefaultAction(subscription())
                .withChannelExceptionHandler(delegatingChannelExceptionHandler)
                .build();
        delegatingChannelExceptionHandler.setChannel(channel);
        return channel;
    }
}
