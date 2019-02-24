package com.envimate.messageMate.messageBus.error;

import com.envimate.messageMate.subscribing.SubscriptionId;

import java.util.List;
import java.util.function.BiConsumer;

public interface ErrorListenerHandler {

    <T> SubscriptionId register(Class<T> errorClass, BiConsumer<T, Exception> exceptionListener);

    <T> SubscriptionId register(List<Class<? extends T>> errorClasses, BiConsumer<? extends T, Exception> exceptionListener);

    <T> List<BiConsumer<T, Exception>> listenerFor(Class<T> clazz);

    void unregister(SubscriptionId subscriptionId);
}
