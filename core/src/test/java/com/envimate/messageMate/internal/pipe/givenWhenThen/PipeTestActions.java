package com.envimate.messageMate.internal.pipe.givenWhenThen;

import com.envimate.messageMate.identification.MessageId;
import com.envimate.messageMate.internal.pipe.Pipe;
import com.envimate.messageMate.internal.pipe.PipeStatusInformation;
import com.envimate.messageMate.internal.pipe.statistics.PipeStatistics;
import com.envimate.messageMate.processingContext.EventType;
import com.envimate.messageMate.shared.pipeChannelMessageBus.testActions.SendingAndReceivingActions;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import com.envimate.messageMate.subscribing.Subscriber;
import com.envimate.messageMate.subscribing.SubscriptionId;
import lombok.RequiredArgsConstructor;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.envimate.messageMate.identification.MessageId.newUniqueMessageId;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class PipeTestActions implements SendingAndReceivingActions {
    private final Pipe<TestMessage> pipe;

    public static PipeTestActions pipeTestActions(final Pipe<TestMessage> pipe) {
        return new PipeTestActions(pipe);
    }

    @Override
    public void close(final boolean finishRemainingTasks) {
        pipe.close(finishRemainingTasks);
    }

    @Override
    public boolean await(final int timeout, final TimeUnit timeUnit) throws InterruptedException {
        return pipe.awaitTermination(timeout, timeUnit);
    }

    @Override
    public boolean isClosed() {
        return pipe.isClosed();
    }


    @Override
    public MessageId send(final EventType eventType, final TestMessage message) {
        pipe.send(message);
        return newUniqueMessageId();
    }

    @Override
    public void subscribe(final EventType eventType, final Subscriber<TestMessage> subscriber) {
        pipe.subscribe(subscriber);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Subscriber<?>> getAllSubscribers() {
        final PipeStatusInformation<TestMessage> statusInformation = pipe.getStatusInformation();
        final List<?> subscribers = statusInformation.getAllSubscribers();
        return (List<Subscriber<?>>) subscribers;
    }

    @Override
    public void unsubscribe(final SubscriptionId subscriptionId) {
        pipe.unsubscribe(subscriptionId);
    }

    public long getTheNumberOfAcceptedMessages() {
        return getMessageStatistics(PipeStatistics::getAcceptedMessages);
    }

    public long getTheNumberOfQueuedMessages() {
        return getMessageStatistics(PipeStatistics::getQueuedMessages);
    }

    public long getTheNumberOfSuccessfulDeliveredMessages() {
        return getMessageStatistics(PipeStatistics::getSuccessfulMessages);
    }

    public long getTheNumberOfFailedDeliveredMessages() {
        return getMessageStatistics(PipeStatistics::getFailedMessages);
    }

    public Date getTheTimestampOfTheMessageStatistics() {
        final PipeStatistics pipeStatistics = getPipeStatistics();
        final Date timestamp = pipeStatistics.getTimestamp();
        return timestamp;
    }

    private long getMessageStatistics(final Function<PipeStatistics, BigInteger> query) {
        final PipeStatistics pipeStatistics = getPipeStatistics();
        final BigInteger statistic = query.apply(pipeStatistics);
        return statistic.longValueExact();
    }

    private PipeStatistics getPipeStatistics() {
        final PipeStatusInformation<TestMessage> statusInformation = pipe.getStatusInformation();
        return statusInformation.getCurrentMessageStatistics();
    }
}
