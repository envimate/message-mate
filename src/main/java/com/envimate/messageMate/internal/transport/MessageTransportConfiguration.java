package com.envimate.messageMate.internal.transport;

import lombok.Getter;
import lombok.Setter;

import static com.envimate.messageMate.internal.transport.MessageTransportType.SYNCHRONOUS;

public final class MessageTransportConfiguration {
    @Getter
    @Setter
    private MessageTransportType messageTransportType;
    @Getter
    @Setter
    private int numberOfThreads;

    private MessageTransportConfiguration(final MessageTransportType messageTransportType, final int numberOfThreads) {
        this.messageTransportType = messageTransportType;
        this.numberOfThreads = numberOfThreads;
    }

    public static MessageTransportConfiguration messageTransportConfiguration(final MessageTransportType messageTransportType,
                                                                              final int numberOfThreads) {
        return new MessageTransportConfiguration(messageTransportType, numberOfThreads);
    }

    public static MessageTransportConfiguration synchronTransportConfiguration() {
        return new MessageTransportConfiguration(SYNCHRONOUS, 0);
    }
}
