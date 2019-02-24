package com.envimate.messageMate.messageBus.error;

import com.envimate.messageMate.channel.Channel;
import com.envimate.messageMate.channel.ProcessingContext;
import com.envimate.messageMate.channel.error.ChannelExceptionHandler;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class DelegatingChannelExceptionHandlerForDelieveryChannel<T> implements ChannelExceptionHandler<T> {
    private final MessageBusExceptionHandler messageBusExceptionHandler;
    @Setter
    private Channel<?> channel;

    public static <T> DelegatingChannelExceptionHandlerForDelieveryChannel<T> delegatingChannelExceptionHandlerForDeliveryChannel(final MessageBusExceptionHandler messageBusExceptionHandler) {
        return new DelegatingChannelExceptionHandlerForDelieveryChannel<>(messageBusExceptionHandler);
    }

    @Override
    public boolean shouldSubscriberErrorBeHandledAndDeliveryAborted(final ProcessingContext<T> message, final Exception e) {
        return messageBusExceptionHandler.shouldDeliveryChannelErrorBeHandledAndDeliveryAborted(message, e, channel);
    }

    @Override
    public void handleSubscriberException(final ProcessingContext<T> message, final Exception e) {
        messageBusExceptionHandler.handleDeliveryChannelException(message, e, channel);
    }

    @Override
    public void handleFilterException(final ProcessingContext<T> message, final Exception e) {
        messageBusExceptionHandler.handleFilterException(message, e, channel);
    }
}
