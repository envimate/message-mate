package com.envimate.messageMate.pipe.givenWhenThen;

import com.envimate.messageMate.filtering.Filter;
import com.envimate.messageMate.pipe.Pipe;
import com.envimate.messageMate.pipe.PipeStatusInformation;
import com.envimate.messageMate.pipe.statistics.PipeStatistics;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeMessageBusSutActions;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import com.envimate.messageMate.subscribing.Subscriber;
import com.envimate.messageMate.subscribing.SubscriptionId;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class PipeTestActions implements PipeMessageBusSutActions {
    private final Pipe<TestMessage> pipe;

    public static PipeTestActions pipeTestActions(final Pipe<TestMessage> pipe) {
        return new PipeTestActions(pipe);
    }

    @Override
    public boolean isShutdown(final TestEnvironment testEnvironment) {
        return pipe.isShutdown();
    }

    @Override
    public List<?> getFilter(final TestEnvironment testEnvironment) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <R> void subscribe(final Class<R> messageClass, final Subscriber<R> subscriber) {
        @SuppressWarnings("unchecked")
        final Subscriber<TestMessage> messageSubscriber = (Subscriber<TestMessage>) subscriber;
        pipe.subscribe(messageSubscriber);
    }

    @Override
    public void close(final boolean finishRemainingTasks) {
        pipe.close(finishRemainingTasks);
    }

    @Override
    public boolean awaitTermination(final int timeout, final TimeUnit timeUnit) throws InterruptedException {
        return pipe.awaitTermination(timeout, timeUnit);
    }

    @Override
    public List<?> getFilter() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void unsubscribe(final SubscriptionId subscriptionId) {
        pipe.unsubscribe(subscriptionId);
    }

    @Override
    public void send(final TestMessage message) {
        pipe.send(message);
    }

    @Override
    public PipeStatistics getMessageStatistics() {
        final PipeStatusInformation<TestMessage> statusInformation = pipe.getStatusInformation();
        return statusInformation.getCurrentMessageStatistics();
    }

    @Override
    public Object removeAFilter() {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addFilter(final Filter<?> filter) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addFilter(final Filter<?> filter, final int position) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Subscriber<?>> getAllSubscribers() {
        final PipeStatusInformation<TestMessage> statusInformation = pipe.getStatusInformation();
        final List<?> allSubscribers = statusInformation.getAllSubscribers();
        return (List<Subscriber<?>>) allSubscribers;
    }
}
