package com.envimate.messageMate.messageBus.error;

import com.envimate.messageMate.channel.ProcessingContext;
import lombok.Getter;

public class MessageBusInternalException extends RuntimeException {
    @Getter
    private final ProcessingContext<?> underlyingMessage;

    public MessageBusInternalException(final ProcessingContext<?> underlyingMessage, final Exception e) {
        super(String.format("An unexpected internal exception in MessageBus for message %s occurred ", underlyingMessage), e);
        this.underlyingMessage = underlyingMessage;
    }
}
