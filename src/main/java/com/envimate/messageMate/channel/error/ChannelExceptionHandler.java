package com.envimate.messageMate.channel.error;

import com.envimate.messageMate.channel.ProcessingContext;

public interface ChannelExceptionHandler<T> {

    boolean shouldSubscriberErrorBeHandledAndDeliveryAborted(ProcessingContext<T> message, Exception e);

    void handleSubscriberException(ProcessingContext<T> message, Exception e);

    void handleFilterException(ProcessingContext<T> message, Exception e);
}
