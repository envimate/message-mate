package com.envimate.messageMate.channel;

import lombok.*;

import java.math.BigInteger;
import java.util.Date;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class ChannelStatistics {
    @Getter
    private final Date timestamp;
    @Getter
    private final BigInteger acceptedMessages;
    @Getter
    private final BigInteger queuedMessages;
    @Getter
    private final BigInteger replacedMessages;
    @Getter
    private final BigInteger blockedMessages;
    @Getter
    private final BigInteger forgottenMessages;
    @Getter
    private final BigInteger successfulMessages;
    @Getter
    private final BigInteger failedMessages;

    public static ChannelStatistics channelStatistics(@NonNull final Date timestamp,
                                                      @NonNull final BigInteger acceptedMessages,
                                                      @NonNull final BigInteger queuedMessages,
                                                      @NonNull final BigInteger replacedMessages,
                                                      @NonNull final BigInteger blockedMessages,
                                                      @NonNull final BigInteger forgottenMessages,
                                                      @NonNull final BigInteger successfulMessages,
                                                      @NonNull final BigInteger failedMessages) {
        return new ChannelStatistics(timestamp, acceptedMessages, queuedMessages, replacedMessages, blockedMessages, forgottenMessages, successfulMessages, failedMessages);
    }

}
