package com.envimate.messageMate.messageBus.error;

import com.envimate.messageMate.channel.Channel;
import com.envimate.messageMate.channel.ProcessingContext;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class ErrorListenerDelegatingMessageBusExceptionHandler implements MessageBusExceptionHandler {
    private final MessageBusExceptionHandler delegate;
    private final ErrorListenerHandler errorListenerHandler;

    public static ErrorListenerDelegatingMessageBusExceptionHandler errorListenerDelegatingMessageBusExceptionHandler(
            final MessageBusExceptionHandler delegate, final ErrorListenerHandler errorListenerHandler) {
        return new ErrorListenerDelegatingMessageBusExceptionHandler(delegate, errorListenerHandler);
    }

    @Override
    public boolean shouldDeliveryChannelErrorBeHandledAndDeliveryAborted(final ProcessingContext<?> message, final Exception e,
                                                                         final Channel<?> channel) {
        return delegate.shouldDeliveryChannelErrorBeHandledAndDeliveryAborted(message, e, channel);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void handleDeliveryChannelException(final ProcessingContext<?> message, final Exception e, final Channel<?> channel) {
        try {
            delegate.handleDeliveryChannelException(message, e, channel);
        } finally {
            @SuppressWarnings("raw")
            final List listener = getListener(message);
            delegate.callTemporaryExceptionListener(message, e, listener);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void handleFilterException(final ProcessingContext<?> message, final Exception e, final Channel<?> channel) {
        try {
            delegate.handleDeliveryChannelException(message, e, channel);
        } finally {
            final List listener = getListener(message);
            delegate.callTemporaryExceptionListener(message, e, listener);
        }
    }


    @SuppressWarnings("rawtypes")
    private List getListener(final ProcessingContext<?> message) {
        final Class<?> aClass = message.getPayload().getClass();
        final List listener = errorListenerHandler.listenerFor(aClass);
        return listener;
    }
}
