package com.envimate.messageMate.serializedMessageBus;

public class TestMissingSerializationException extends RuntimeException {
    public TestMissingSerializationException(final String message) {
        super(message);
    }
}
