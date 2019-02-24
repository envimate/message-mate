package com.envimate.messageMate.channel.action;

import com.envimate.messageMate.subscribing.Subscriber;
import com.envimate.messageMate.subscribing.SubscriptionId;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class Subscription<T> implements Action<T> {
    @Getter
    private final Set<Subscriber<T>> subscribers;

    public static <T> Subscription<T> subscription() {
        final Set<Subscriber<T>> linkedList = ConcurrentHashMap.newKeySet();
        return new Subscription<>(linkedList);
    }

    public void addSubscriber(final Subscriber<T> subscriber) {
        subscribers.add(subscriber);
    }

    public boolean hasSubscriber() {
        return !subscribers.isEmpty();
    }

    public void removeSubscriber(final Subscriber<T> subscriber) {
        subscribers.remove(subscriber);
    }

    public void removeSubscriber(final SubscriptionId subscriptionId) {
        subscribers.removeIf(subscriber -> subscriber.getSubscriptionId().equals(subscriptionId));
    }
}
