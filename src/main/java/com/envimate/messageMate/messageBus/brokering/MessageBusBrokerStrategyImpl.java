package com.envimate.messageMate.messageBus.brokering;

import com.envimate.messageMate.channel.Channel;
import com.envimate.messageMate.channel.action.Action;
import com.envimate.messageMate.channel.action.Subscription;
import com.envimate.messageMate.messageBus.channelCreating.MessageBusChannelFactory;
import com.envimate.messageMate.subscribing.Subscriber;
import com.envimate.messageMate.subscribing.SubscriptionId;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.envimate.messageMate.internal.reflections.ReflectionUtils.getAllSuperClassesAndInterfaces;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class MessageBusBrokerStrategyImpl implements MessageBusBrokerStrategy {
    private final Map<Class<?>, Set<Channel<?>>> cachedChannelMap = new ConcurrentHashMap<>();
    private final Map<Class<?>, ClassInformation> classInformationMap = new ConcurrentHashMap<>();
    private final Map<SubscriptionId, Set<Subscription<?>>> subscriptionIdToSubscriptionMap = new ConcurrentHashMap<>();
    private final MessageBusChannelFactory channelFactory;

    public static MessageBusBrokerStrategyImpl messageBusBrokerStrategy(
            final MessageBusChannelFactory channelFactory) {
        return new MessageBusBrokerStrategyImpl(channelFactory);
    }

    @Override
    public Set<Channel<?>> getDeliveringChannelsFor(final Class<?> messageClass) {
        if (cachedChannelMap.containsKey(messageClass)) {
            System.out.println("getDeliveringChannelsFor: cached entry for " + messageClass);
            return cachedChannelMap.get(messageClass);
        } else {
            final Set<Channel<?>> channels = storeNewlySeenMessageClass(messageClass);
            return channels;
        }
    }

    private synchronized Set<Channel<?>> storeNewlySeenMessageClass(final Class<?> messageClass) {
        System.out.println("getDeliveringChannelsFor: creating entry for " + messageClass);
        final ClassInformation classInformation = createClassInformation(messageClass);
        final Set<Channel<?>> channels = collectAllChannelsFromSuperClassHierarchy(classInformation);
        cachedChannelMap.put(messageClass, channels);
        return channels;
    }

    @Override
    public synchronized <T> void addSubscriber(final Class<T> tClass, final Subscriber<T> subscriber) {
        if (classInformationMap.containsKey(tClass)) {
            final ClassInformation classInformation = classInformationMap.get(tClass);
            if (classInformation.hasChannel()) {
                System.out.println("addSubscriber: only adding subscriber to existing channel for " + tClass);
                final Channel<?> channel = classInformation.getChannel();
                addSubscriberTo(channel, subscriber);
            } else {
                System.out.println("addSubscriber: creating channel for existing entry for" + tClass);
                final Channel<?> newlyCreatedChannel = createNewChannel(tClass, subscriber, classInformation);
                publishNewChannelToAllChildren(classInformation, newlyCreatedChannel);
            }
        } else {
            System.out.println("addSubscriber: creating new entry for " + tClass);
            final ClassInformation classInformation = createClassInformation(tClass);
            createNewChannel(tClass, subscriber, classInformation);
            final Set<Channel<?>> channels = collectAllChannelsFromSuperClassHierarchy(classInformation);
            cachedChannelMap.put(tClass, channels);
        }
    }

    private <T> Channel<?> createNewChannel(final Class<T> tClass, final Subscriber<T> subscriber, final ClassInformation classInformation) {
        final Channel<?> newlyCreatedChannel = channelFactory.createChannel(tClass, subscriber);
        addSubscriberTo(newlyCreatedChannel, subscriber);
        classInformation.setChannel(newlyCreatedChannel);
        return newlyCreatedChannel;
    }

    private void publishNewChannelToAllChildren(final ClassInformation classInformation, final Channel<?> newlyCreatedChannel) {
        final LinkedList<ClassInformation> pendingClassInformation = new LinkedList<>();
        pendingClassInformation.add(classInformation);
        while (!pendingClassInformation.isEmpty()) {
            final ClassInformation currentClassInformation = pendingClassInformation.removeFirst();
            final Class<?> clazz = currentClassInformation.clazz;
            cachedChannelMap.putIfAbsent(clazz, new HashSet<>());
            final Set<Channel<?>> channels = cachedChannelMap.get(clazz);
            channels.add(newlyCreatedChannel);
            pendingClassInformation.addAll(currentClassInformation.extendingClassInformationSet);
        }
    }

    private <T> void addSubscriberTo(final Channel<?> channel, final Subscriber<T> subscriber) {
        final Subscription<T> subscription = getSubscriptionOf(channel);
        subscription.addSubscriber(subscriber);
        final SubscriptionId subscriptionId = subscriber.getSubscriptionId();
        subscriptionIdToSubscriptionMap.putIfAbsent(subscriptionId, new HashSet<>());
        final Set<Subscription<?>> subscriptions = subscriptionIdToSubscriptionMap.get(subscriptionId);
        subscriptions.add(subscription);
    }

    private <T> Subscription<T> getSubscriptionOf(final Channel<?> channel) {
        final Action<?> defaultAction = channel.getDefaultAction();
        if (defaultAction instanceof Subscription) {
            final Subscription<T> subscription = (Subscription<T>) defaultAction;
            return subscription;
        } else {
            throw new IllegalStateException("Channels inside the MessageBus must have Subscription as default action.");
        }
    }

    private Set<Channel<?>> collectAllChannelsFromSuperClassHierarchy(final ClassInformation classInformation) {
        final Set<Channel<?>> channels = new HashSet<>();
        final LinkedList<ClassInformation> pendingClassInformationList = new LinkedList<>();
        pendingClassInformationList.add(classInformation);
        while (!pendingClassInformationList.isEmpty()) {
            final ClassInformation currentClassInformation = pendingClassInformationList.removeFirst();
            if (currentClassInformation.hasChannel()) {
                final Channel<?> channel = currentClassInformation.getChannel();
                channels.add(channel);
            }
            final Set<ClassInformation> superClassInformationSet = currentClassInformation.superClassInformationSet;
            pendingClassInformationList.addAll(superClassInformationSet);
        }
        return channels;
    }

    private ClassInformation createClassInformation(final Class<?> clazz) {
        final Set<Class<?>> directSuperClasses = allSuperClassesAndInterfaces(clazz);
        System.out.println("messageClass = " + clazz);
        System.out.println("directSuperClasses = " + directSuperClasses);
        final Set<ClassInformation> superClassInformation = classInformationOfSuperClasses(directSuperClasses);//recursive
        final ClassInformation classInformation = new ClassInformation(clazz, superClassInformation);
        for (final ClassInformation currentSuperClassInformation : superClassInformation) {
            currentSuperClassInformation.addExtendingClass(classInformation);
        }
        classInformationMap.put(clazz, classInformation);
        return classInformation;
    }

    private Set<Class<?>> allSuperClassesAndInterfaces(final Class<?> clazz) {
        if (clazz.equals(Object.class)) {
            return Collections.emptySet();
        } else {
            return getAllSuperClassesAndInterfaces(clazz);
        }
    }

    private Set<ClassInformation> classInformationOfSuperClasses(final Set<Class<?>> directSuperClasses) {
        final Set<ClassInformation> superClassInformationSet = new HashSet<>();
        for (final Class<?> superClass : directSuperClasses) {
            if (classInformationMap.containsKey(superClass)) {
                final ClassInformation superClassInformation = classInformationMap.get(superClass);
                superClassInformationSet.add(superClassInformation);
            } else {
                final ClassInformation superClassInformation = createClassInformation(superClass);
                superClassInformationSet.add(superClassInformation);
            }
        }
        return superClassInformationSet;
    }

    @Override
    public synchronized void removeSubscriber(final SubscriptionId subscriptionId) {
        if (subscriptionIdToSubscriptionMap.containsKey(subscriptionId)) {
            final Set<Subscription<?>> subscriptions = subscriptionIdToSubscriptionMap.get(subscriptionId);
            for (final Subscription<?> subscription : subscriptions) {
                subscription.removeSubscriber(subscriptionId);
                //TODO: think about cleaning up orphaned channels
            }
        }
    }

    @Override
    public List<Subscriber<?>> getAllSubscribers() {
        final Set<? extends Subscriber<?>> subscribers = subscriptionIdToSubscriptionMap.values()
                .stream()
                .flatMap(Collection::stream)
                .map(Subscription::getSubscribers)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        return new LinkedList<>(subscribers);
    }

    @Override
    public Map<Class<?>, List<Subscriber<?>>> getSubscribersPerType() {
        return classInformationMap.entrySet()
                .stream()
                .filter(e -> e.getValue().hasChannel())
                .collect(Collectors.toMap(Map.Entry::getKey, this::subscribersOf));
    }

    @Override
    public synchronized Channel<?> getClassSpecificChannel(final Class<?> messageClass) {
        if (classInformationMap.containsKey(messageClass)) {
            final ClassInformation classInformation = classInformationMap.get(messageClass);
            return classInformation.getChannel();
        } else {
            return null;
        }
    }

    private List<Subscriber<?>> subscribersOf(final Map.Entry<Class<?>, ClassInformation> entry) {
        final ClassInformation classInformation = entry.getValue();
        final Channel<?> channel = classInformation.getChannel();
        final Subscription<?> subscription = getSubscriptionOf(channel);
        return new LinkedList<>(subscription.getSubscribers());
    }

    private class ClassInformation {
        private final Class<?> clazz;
        private final Set<ClassInformation> extendingClassInformationSet;
        private final Set<ClassInformation> superClassInformationSet;
        @Getter
        @Setter
        private Channel<?> channel;

        public ClassInformation(final Class<?> clazz, final Set<ClassInformation> superClassInformationSet) {
            this.clazz = clazz;
            this.superClassInformationSet = superClassInformationSet;
            extendingClassInformationSet = new HashSet<>();
        }

        public boolean hasChannel() {
            return channel != null;
        }

        public void addExtendingClass(final ClassInformation extendingClassInformation) {
            extendingClassInformationSet.add(extendingClassInformation);
        }
    }
}
