package com.envimate.messageMate.messageBus.internal.brokering;

import com.envimate.messageMate.channel.Channel;
import com.envimate.messageMate.channel.action.Subscription;
import com.envimate.messageMate.messageBus.EventType;
import com.envimate.messageMate.messageBus.channelCreating.MessageBusChannelFactory;
import com.envimate.messageMate.messageBus.exception.MessageBusExceptionHandler;
import com.envimate.messageMate.processingContext.ProcessingContext;
import com.envimate.messageMate.subscribing.Subscriber;
import com.envimate.messageMate.subscribing.SubscriptionId;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class MessageBusBrokerStrategyImpl implements MessageBusBrokerStrategy {
    private final Map<EventType, Channel<Object>> channelMap = new ConcurrentHashMap<>();
    private final Map<SubscriptionId, List<EventType>> subscriptionLookupMap = new ConcurrentHashMap<>();
    private final MessageBusChannelFactory channelFactory;
    private final MessageBusExceptionHandler messageBusExceptionHandler;

    public static MessageBusBrokerStrategyImpl messageBusBrokerStrategyImpl2(final MessageBusChannelFactory messageBusChannelFactory,
                                                                             final MessageBusExceptionHandler messageBusExceptionHandler) {
        return new MessageBusBrokerStrategyImpl(messageBusChannelFactory, messageBusExceptionHandler);
    }

    @Override
    public Channel<Object> getDeliveringChannelFor(EventType eventType) {
        return getOrCreateChannel(eventType);
    }

    private Channel<Object> getOrCreateChannel(EventType eventType) {
        if (channelMap.containsKey(eventType)) {
            return channelMap.get(eventType);
        } else {
            final Channel<Object> channel = channelFactory.createChannel(eventType, null, messageBusExceptionHandler);
            channelMap.put(eventType, channel);
            return channel;
        }
    }

    @Override
    public void addSubscriber(EventType eventType, Subscriber<Object> subscriber) {
        final Channel<Object> channel = getOrCreateChannel(eventType);
        final Subscription<Object> subscription = getChannelSubscription(channel);
        subscription.addSubscriber(subscriber);
        storeSubscriptionForLookup(eventType, subscriber);
    }

    @Override
    public void addRawSubscriber(EventType eventType, Subscriber<ProcessingContext<Object>> subscriber) {
        final Channel<Object> channel = getOrCreateChannel(eventType);
        final Subscription<Object> subscription = getChannelSubscription(channel);
        subscription.addRawSubscriber(subscriber);
        storeSubscriptionForLookup(eventType, subscriber);
    }

    private Subscription<Object> getChannelSubscription(Channel<Object> channel) {
        return (Subscription<Object>) channel.getDefaultAction();
    }

    private void storeSubscriptionForLookup(EventType eventType, Subscriber<?> subscriber) {
        final SubscriptionId subscriptionId = subscriber.getSubscriptionId();
        final List<EventType> eventTypes;
        if (subscriptionLookupMap.containsKey(subscriptionId)) {
            eventTypes = subscriptionLookupMap.get(subscriptionId);
        } else {
            eventTypes = new LinkedList<>();
            subscriptionLookupMap.put(subscriptionId, eventTypes);
        }
        eventTypes.add(eventType);
    }

    @Override
    public void removeSubscriber(SubscriptionId subscriptionId) {
        if (subscriptionLookupMap.containsKey(subscriptionId)) {
            final List<EventType> eventTypes = subscriptionLookupMap.get(subscriptionId);
            eventTypes.stream()
                    .map(eventType -> channelMap.get(eventType))
                    .map(channel -> getChannelSubscription(channel))
                    .forEach(subscription -> subscription.removeSubscriber(subscriptionId));
        }
    }

    @Override
    public List<Subscriber<?>> getAllSubscribers() {
        return channelMap.values().stream()
                .map(channel -> getChannelSubscription(channel))
                .flatMap(subscription -> subscription.getAllSubscribers().stream())
                .collect(Collectors.toList());
    }

    @Override
    public Map<EventType, List<Subscriber<?>>> getSubscribersPerType() {
        final Map<EventType, List<Subscriber<?>>> map = channelMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> getChannelSubscription(e.getValue()).getAllSubscribers()));
        return map;
    }

}
