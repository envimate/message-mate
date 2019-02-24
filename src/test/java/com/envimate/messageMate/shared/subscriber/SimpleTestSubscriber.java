package com.envimate.messageMate.shared.subscriber;

import com.envimate.messageMate.subscribing.AcceptingBehavior;
import com.envimate.messageMate.subscribing.SubscriptionId;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.envimate.messageMate.subscribing.AcceptingBehavior.acceptingBehavior;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SimpleTestSubscriber<T> implements TestSubscriber<T> {
    private final SubscriptionId subscriptionId = SubscriptionId.newUniqueId();
    private final List<T> receivedMessages = new CopyOnWriteArrayList<>();
    private final boolean preemptDelivery;

    public static <T> SimpleTestSubscriber<T> testSubscriber() {
        return new SimpleTestSubscriber<>(false);
    }

    public static <T> SimpleTestSubscriber<T> deliveryPreemptingSubscriber() {
        return new SimpleTestSubscriber<>(true);
    }

    @Override
    public AcceptingBehavior accept(final T message) {
        receivedMessages.add(message);
        final boolean continueDelivery = !preemptDelivery;
        return acceptingBehavior(continueDelivery);
    }

    @Override
    public SubscriptionId getSubscriptionId() {
        return subscriptionId;
    }

    public List<T> getReceivedMessages() {
        return receivedMessages;
    }


    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final SimpleTestSubscriber<?> that = (SimpleTestSubscriber<?>) o;
        return Objects.equals(subscriptionId, that.subscriptionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subscriptionId);
    }

    @Override
    public String toString() {
        return "SimpleTestSubscriber{SubscriptionId=" + subscriptionId + "}";
    }
}
