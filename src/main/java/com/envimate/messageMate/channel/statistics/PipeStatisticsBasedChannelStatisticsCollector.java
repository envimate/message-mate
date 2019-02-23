package com.envimate.messageMate.channel.statistics;

import com.envimate.messageMate.channel.ChannelStatistics;
import com.envimate.messageMate.pipe.Pipe;
import com.envimate.messageMate.pipe.statistics.PipeStatistics;
import lombok.RequiredArgsConstructor;

import java.math.BigInteger;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class PipeStatisticsBasedChannelStatisticsCollector implements PartialCollectingChannelStatisticsCollector {
    private final Pipe<?> acceptingPipe;
    private final Pipe<?> deliveringPipe;
    private final AtomicLong messagesReplaced = new AtomicLong();
    private final AtomicLong messagesBlocked = new AtomicLong();
    private final AtomicLong messagesForgotten = new AtomicLong();
    private final AtomicLong exceptionsInFilter = new AtomicLong();

    public static PipeStatisticsBasedChannelStatisticsCollector pipeStatisticsBasedChannelStatisticsCollector(
            final Pipe<?> acceptingPipe,
            final Pipe<?> deliveringPipe) {
        return new PipeStatisticsBasedChannelStatisticsCollector(acceptingPipe, deliveringPipe);
    }

    @Override
    public void informMessageReplaced() {
        messagesReplaced.incrementAndGet();
    }

    @Override
    public void informMessageBlocked() {
        messagesBlocked.incrementAndGet();
    }

    @Override
    public void informMessageForgotten() {
        messagesForgotten.incrementAndGet();
    }

    @Override
    public void informExceptionInFilterThrown() {
        exceptionsInFilter.incrementAndGet();
    }

    @Override
    public ChannelStatistics getStatistics() {
        final Date timestamp = new Date();
        final PipeStatistics acceptingStatistics = statisticsOf(acceptingPipe);
        final PipeStatistics deliveringStatistics = statisticsOf(deliveringPipe);
        final BigInteger acceptedMessages = acceptingStatistics.getAcceptedMessages();
        final BigInteger queuedMessages = acceptingStatistics.getQueuedMessages();
        final BigInteger replacedMessages = asBigInt(messagesReplaced);
        final BigInteger blockedMessages = asBigInt(messagesBlocked);
        final BigInteger forgottenMessages = asBigInt(messagesForgotten);
        final BigInteger successfulMessages = deliveringStatistics.getSuccessfulMessages();
        final BigInteger pipeFailedMessages = deliveringStatistics.getFailedMessages();
        final BigInteger deliveryFailedBecauseOfExceptionsInFilter = asBigInt(exceptionsInFilter);
        final BigInteger failedMessages = pipeFailedMessages.add(deliveryFailedBecauseOfExceptionsInFilter);
        return ChannelStatistics.channelStatistics(timestamp, acceptedMessages, queuedMessages, replacedMessages, blockedMessages,
                forgottenMessages, successfulMessages, failedMessages);
    }

    private PipeStatistics statisticsOf(final Pipe<?> pipe) {
        return pipe.getStatusInformation().getCurrentMessageStatistics();
    }


    private BigInteger asBigInt(final AtomicLong atomicLong) {
        final long value = atomicLong.get();
        return BigInteger.valueOf(value);
    }
}
