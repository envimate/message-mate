package com.envimate.messageMate.messageBus.error;

import com.envimate.messageMate.channel.Channel;
import com.envimate.messageMate.channel.ProcessingContext;

import java.util.List;
import java.util.function.BiConsumer;

public interface MessageBusExceptionHandler {

    boolean shouldDeliveryChannelErrorBeHandledAndDeliveryAborted(ProcessingContext<?> message, Exception e, Channel<?> channel);

    void handleDeliveryChannelException(ProcessingContext<?> message, Exception e, Channel<?> channel);

    void handleFilterException(ProcessingContext<?> message, Exception e, Channel<?> channel);

    default <T> void callTemporaryExceptionListener(final ProcessingContext<T> message, final Exception e,
                                                    final List<BiConsumer<T, Exception>> listener) {
        final T payload = message.getPayload();
        listener.forEach(l -> l.accept(payload, e));
    }
}
