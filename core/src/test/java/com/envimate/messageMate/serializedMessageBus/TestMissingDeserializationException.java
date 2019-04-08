package com.envimate.messageMate.serializedMessageBus;

public class TestMissingDeserializationException extends RuntimeException {
    public TestMissingDeserializationException(final String message) {
        super(message);
    }
}
