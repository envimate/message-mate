package com.envimate.messageMate.messageBus.statistics;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.math.BigInteger;
import java.util.Date;

import static lombok.AccessLevel.PRIVATE;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = PRIVATE)
public final class MessageBusStatistics {
    private final Date timestamp;
    private final BigInteger acceptedMessages;
    private final BigInteger successfulMessages;
    private final BigInteger failedMessages;
    private final BigInteger blockedMessages;
    private final BigInteger replacedMessages;
    private final BigInteger forgottenMessages;
    private final BigInteger queuedMessages;

    public static MessageBusStatistics messageBusStatistics(@NonNull final Date timestamp,
                                                            @NonNull final BigInteger acceptedMessages,
                                                            @NonNull final BigInteger successfulMessages,
                                                            @NonNull final BigInteger failedMessages,
                                                            @NonNull final BigInteger blockedMessages,
                                                            @NonNull final BigInteger replacedMessages,
                                                            @NonNull final BigInteger forgottenMessages,
                                                            @NonNull final BigInteger queuedMessages) {
        return new MessageBusStatistics(timestamp, acceptedMessages, successfulMessages, failedMessages, blockedMessages,
                replacedMessages, forgottenMessages, queuedMessages);
    }

    public Date getTimestamp() {
        final long copyForSafeSharing = timestamp.getTime();
        return new Date(copyForSafeSharing);
    }

    public BigInteger getAcceptedMessages() {
        return acceptedMessages;
    }

    public BigInteger getSuccessfulMessages() {
        return successfulMessages;
    }

    public BigInteger getFailedMessages() {
        return failedMessages;
    }

    public BigInteger getBlockedMessages() {
        return blockedMessages;
    }

    public BigInteger getReplacedMessages() {
        return replacedMessages;
    }

    public BigInteger getForgottenMessages() {
        return forgottenMessages;
    }

    public BigInteger getQueuedMessages() {
        return queuedMessages;
    }

}
