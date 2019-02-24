package com.envimate.messageMate.messageBus.error;

import com.envimate.messageMate.channel.Channel;
import com.envimate.messageMate.channel.ProcessingContext;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class ErrorThrowingMessageBusExceptionHandler implements MessageBusExceptionHandler {

    public static ErrorThrowingMessageBusExceptionHandler errorThrowingMessageBusExceptionHandler() {
        return new ErrorThrowingMessageBusExceptionHandler();
    }

    @Override
    public boolean shouldDeliveryChannelErrorBeHandledAndDeliveryAborted(final ProcessingContext<?> message, final Exception e, final Channel<?> channel) {
        return true;
    }

    @Override
    public void handleDeliveryChannelException(final ProcessingContext<?> message, final Exception e, final Channel<?> channel) {
        if (e instanceof RuntimeException) {
            throw (RuntimeException) e;
        } else {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void handleFilterException(final ProcessingContext<?> message, final Exception e, final Channel<?> channel) {
        if (e instanceof RuntimeException) {
            throw (RuntimeException) e;
        } else {
            throw new RuntimeException(e);
        }
    }
}
