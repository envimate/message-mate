package com.envimate.messageMate.messageBus.internal.correlationIds;

import com.envimate.messageMate.channel.ProcessingContext;
import com.envimate.messageMate.messageFunction.correlation.CorrelationId;
import com.envimate.messageMate.subscribing.Subscriber;
import com.envimate.messageMate.subscribing.SubscriptionId;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.util.Collections.emptyList;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class CorrelationBasedSubscriptionsImpl implements CorrelationBasedSubscriptions {
    private final Map<CorrelationId, List<Subscriber<ProcessingContext<Object>>>> correlationBasedSubscriber = new ConcurrentHashMap<>();
    private final Map<SubscriptionId, List<CorrelationId>> reverseLookupMap = new ConcurrentHashMap<>();

    public static CorrelationBasedSubscriptionsImpl correlationBasedSubscriptions() {
        return new CorrelationBasedSubscriptionsImpl();
    }

    @Override
    public synchronized SubscriptionId addCorrelationBasedSubscriber(final CorrelationId correlationId,
                                                                     final Subscriber<ProcessingContext<Object>> subscriber) {
        final SubscriptionId subscriptionId = subscriber.getSubscriptionId();
        if (correlationBasedSubscriber.containsKey(correlationId)) {
            final List<Subscriber<ProcessingContext<Object>>> subscribers = correlationBasedSubscriber.get(correlationId);
            subscribers.add(subscriber);
        } else {
            final CopyOnWriteArrayList<Subscriber<ProcessingContext<Object>>> subscribers = new CopyOnWriteArrayList<>();
            subscribers.add(subscriber);
            correlationBasedSubscriber.put(correlationId, subscribers);
        }
        if (reverseLookupMap.containsKey(subscriptionId)) {
            final List<CorrelationId> correlationIds = reverseLookupMap.get(subscriptionId);
            correlationIds.add(correlationId);
        } else {
            final LinkedList<CorrelationId> correlationIds = new LinkedList<>();
            correlationIds.add(correlationId);
            reverseLookupMap.putIfAbsent(subscriptionId, correlationIds);
        }
        return subscriptionId;
    }

    @Override
    public synchronized void unsubscribe(final SubscriptionId subscriptionId) {
        if (reverseLookupMap.containsKey(subscriptionId)) {
            final List<CorrelationId> correlationIds = reverseLookupMap.get(subscriptionId);
            for (final CorrelationId correlationId : correlationIds) {
                final List<Subscriber<ProcessingContext<Object>>> subscribers = correlationBasedSubscriber.get(correlationId);
                subscribers.removeIf(s -> s.getSubscriptionId().equals(subscriptionId));
            }
            if (correlationIds.size() == 1) {
                reverseLookupMap.remove(subscriptionId);
            }
        }
    }

    @Override
    public List<Subscriber<ProcessingContext<Object>>> getSubscribersFor(final CorrelationId correlationId) {
        return correlationBasedSubscriber.getOrDefault(correlationId, emptyList());
    }
}
