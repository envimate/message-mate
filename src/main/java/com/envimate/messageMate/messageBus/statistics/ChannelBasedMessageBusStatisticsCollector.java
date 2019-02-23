package com.envimate.messageMate.messageBus.statistics;

import com.envimate.messageMate.channel.Channel;
import com.envimate.messageMate.channel.ChannelStatistics;
import com.envimate.messageMate.channel.ChannelStatusInformation;
import lombok.RequiredArgsConstructor;

import java.math.BigInteger;
import java.util.Date;

import static com.envimate.messageMate.messageBus.statistics.MessageBusStatistics.messageBusStatistics;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class ChannelBasedMessageBusStatisticsCollector implements MessageBusStatisticsCollector {
    private final Channel<?> channel;

    public static ChannelBasedMessageBusStatisticsCollector channelBasedMessageBusStatisticsCollector(final Channel<?> channel) {
        return new ChannelBasedMessageBusStatisticsCollector(channel);
    }

    @Override
    public MessageBusStatistics getStatistics() {
        final ChannelStatusInformation statusInformation = channel.getStatusInformation();
        final ChannelStatistics channelStatistics = statusInformation.getChannelStatistics();
        final MessageBusStatistics messageBusStatistics = getMessageBusStatistics(channelStatistics);
        return messageBusStatistics;
    }

    private MessageBusStatistics getMessageBusStatistics(final ChannelStatistics channelStatistics) {
        final Date timestamp = channelStatistics.getTimestamp();
        final BigInteger acceptedMessages = channelStatistics.getAcceptedMessages();
        final BigInteger successfulMessages = channelStatistics.getSuccessfulMessages();
        final BigInteger failedMessages = channelStatistics.getFailedMessages();
        final BigInteger blockedMessages = channelStatistics.getBlockedMessages();
        final BigInteger replacedMessages = channelStatistics.getReplacedMessages();
        final BigInteger forgottenMessages = channelStatistics.getForgottenMessages();
        final BigInteger queuedMessages = channelStatistics.getQueuedMessages();
        return messageBusStatistics(timestamp, acceptedMessages, successfulMessages, failedMessages, blockedMessages,
                replacedMessages, forgottenMessages, queuedMessages);
    }
}
