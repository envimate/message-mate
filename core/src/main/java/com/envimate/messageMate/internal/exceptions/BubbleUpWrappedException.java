package com.envimate.messageMate.internal.exceptions;

public class BubbleUpWrappedException extends RuntimeException {
    public BubbleUpWrappedException(final Throwable cause) {
        super(cause);
    }
}
