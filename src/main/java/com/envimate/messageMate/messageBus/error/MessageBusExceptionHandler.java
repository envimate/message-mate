package com.envimate.messageMate.messageBus.error;

import com.envimate.messageMate.channel.Channel;
import com.envimate.messageMate.channel.ProcessingContext;

public interface MessageBusExceptionHandler {

    boolean shouldDeliveryChannelErrorBeHandledAndDeliveryAborted(ProcessingContext<?> message, Exception e, Channel<?> channel);

    void handleDeliveryChannelException(ProcessingContext<?> message, Exception e, Channel<?> channel);

    void handleFilterException(ProcessingContext<?> message, Exception e, Channel<?> channel);
}
