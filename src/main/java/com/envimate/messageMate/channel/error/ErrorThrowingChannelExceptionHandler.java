package com.envimate.messageMate.channel.error;

import com.envimate.messageMate.channel.ProcessingContext;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class ErrorThrowingChannelExceptionHandler<T> implements ChannelExceptionHandler<T> {

    public static <T> ErrorThrowingChannelExceptionHandler<T> errorThrowingChannelExceptionHandler() {
        return new ErrorThrowingChannelExceptionHandler<>();
    }

    @Override
    public boolean shouldSubscriberErrorBeHandledAndDeliveryAborted(final ProcessingContext<T> message, final Exception e) {
        return true;
    }

    @Override
    public void handleSubscriberException(final ProcessingContext<T> message, final Exception e) {
        if (e instanceof RuntimeException) {
            throw (RuntimeException) e;
        } else {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void handleFilterException(final ProcessingContext<T> message, final Exception e) {
        if (e instanceof RuntimeException) {
            throw (RuntimeException) e;
        } else {
            throw new RuntimeException(e);
        }
    }
}
