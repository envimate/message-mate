package com.envimate.messageMate.messageBus.exception;

import com.envimate.messageMate.processingContext.ProcessingContext;

public class MissingEventTypeException extends RuntimeException {
    public MissingEventTypeException(final ProcessingContext<Object> processingContext) {
        super("Cannot send processing context without a set event type: " + processingContext);
    }
}
