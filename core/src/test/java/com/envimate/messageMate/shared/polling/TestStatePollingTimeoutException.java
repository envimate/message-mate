package com.envimate.messageMate.shared.polling;

public class TestStatePollingTimeoutException extends RuntimeException {

    public TestStatePollingTimeoutException() {
    }

    public TestStatePollingTimeoutException(final String message) {
        super(message);
    }
}
