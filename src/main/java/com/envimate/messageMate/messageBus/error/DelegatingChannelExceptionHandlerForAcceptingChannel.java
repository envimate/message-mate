package com.envimate.messageMate.messageBus.error;

import com.envimate.messageMate.channel.Channel;
import com.envimate.messageMate.channel.ProcessingContext;
import com.envimate.messageMate.channel.error.ChannelExceptionHandler;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.function.BiConsumer;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class DelegatingChannelExceptionHandlerForAcceptingChannel<T> implements ChannelExceptionHandler<T> {
    private final MessageBusExceptionHandler messageBusExceptionHandler;
    private final ErrorListenerHandler errorListenerHandler;
    @Setter
    private Channel<?> channel;

    public static <T> DelegatingChannelExceptionHandlerForAcceptingChannel<T> delegatingChannelExceptionHandlerForAcceptingChannel(
            final MessageBusExceptionHandler messageBusExceptionHandler, final ErrorListenerHandler errorListenerHandler) {
        return new DelegatingChannelExceptionHandlerForAcceptingChannel<>(messageBusExceptionHandler, errorListenerHandler);
    }

    @Override
    public boolean shouldSubscriberErrorBeHandledAndDeliveryAborted(final ProcessingContext<T> message, final Exception e) {
        return true;
    }

    @Override
    public void handleSubscriberException(final ProcessingContext<T> message, final Exception e) {
        try {
            System.out.println("here");
            messageBusExceptionHandler.handleDeliveryChannelException(message, e, channel);
        } finally {
            System.out.println("here2");
            final List<BiConsumer<T, Exception>> listener = getListener(message);
            System.out.println(listener.size());
            messageBusExceptionHandler.callTemporaryExceptionListener(message, e, listener);
        }
    }

    @Override
    public void handleFilterException(final ProcessingContext<T> message, final Exception e) {
        try {
            messageBusExceptionHandler.handleFilterException(message, e, channel);
        } finally {
            final List<BiConsumer<T, Exception>> listener = getListener(message);
            messageBusExceptionHandler.callTemporaryExceptionListener(message, e, listener);
        }
    }

    private List<BiConsumer<T, Exception>> getListener(final ProcessingContext<T> message) {
        final Class<?> aClass = message.getPayload().getClass();
        System.out.println("aClass = " + aClass);
        final List<?> uncheckedListener = errorListenerHandler.listenerFor(aClass);
        return (List<BiConsumer<T, Exception>>) uncheckedListener;
    }
}
