package com.envimate.messageMate.shared.channelMessageBus.givenWhenThen;

import com.envimate.messageMate.filtering.Filter;
import com.envimate.messageMate.internal.statistics.MessageStatistics;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import com.envimate.messageMate.subscribing.Subscriber;
import com.envimate.messageMate.subscribing.SubscriptionId;

import java.util.List;
import java.util.concurrent.TimeUnit;

public interface ChannelMessageBusSutActions {

    boolean isShutdown(final TestEnvironment testEnvironment);

    <R> void subscribe(Class<R> messageClass, Subscriber<R> subscriber);

    void close(boolean finishRemainingTasks);

    boolean awaitTermination(int timeout, TimeUnit timeUnit) throws InterruptedException;

    List<?> getFilter();

    void unsubscribe(SubscriptionId subscriptionId);

    void send(TestMessage message);

    MessageStatistics getMessageStatistics();

    void addFilter(Filter<?> filter);

    void addFilter(Filter<?> filter, int position);

    List<?> getFilter(final TestEnvironment testEnvironment);

    Object removeAFilter();

    List<Subscriber<?>> getAllSubscribers();
}
