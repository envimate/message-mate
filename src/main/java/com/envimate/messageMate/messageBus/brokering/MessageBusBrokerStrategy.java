package com.envimate.messageMate.messageBus.brokering;

import com.envimate.messageMate.channel.Channel;
import com.envimate.messageMate.subscribing.Subscriber;
import com.envimate.messageMate.subscribing.SubscriptionId;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface MessageBusBrokerStrategy {

    Set<Channel<?>> getDeliveringChannelsFor(Class<?> messageClass);

    <T> void addSubscriber(Class<T> tClass, Subscriber<T> subscriber);

    void removeSubscriber(SubscriptionId subscriptionId);

    List<Subscriber<?>> getAllSubscribers();

    Map<Class<?>, List<Subscriber<?>>> getSubscribersPerType();

    Channel<?> getClassSpecificChannel(Class<?> messageClass);
}
