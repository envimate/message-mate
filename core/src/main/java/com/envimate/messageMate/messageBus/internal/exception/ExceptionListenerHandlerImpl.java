/*
 * Copyright (c) 2018 envimate GmbH - https://envimate.com/.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.envimate.messageMate.messageBus.internal.exception;

import com.envimate.messageMate.channel.ProcessingContext;
import com.envimate.messageMate.identification.CorrelationId;
import com.envimate.messageMate.messageBus.exception.MessageBusExceptionListener;
import com.envimate.messageMate.subscribing.SubscriptionId;
import lombok.Getter;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

//TODO: add same listener twice: unregisterLookupMap bug + what about reusing ListenerInformation
public class ExceptionListenerHandlerImpl implements ExceptionListenerHandler {
    private final Map<Class<?>, List<ListenerInformation>> classBasedListenerLookupMap = new ConcurrentHashMap<>();
    private final Map<CorrelationId, List<ListenerInformation>> correlationIdBasedListenerLookupMap = new ConcurrentHashMap<>();
    private final Map<SubscriptionId, ListenerInformation> unregisterLookupMap = new ConcurrentHashMap<>();

    public static ExceptionListenerHandlerImpl errorListenerHandler() {
        return new ExceptionListenerHandlerImpl();
    }

    @Override
    public synchronized <T> SubscriptionId register(final Class<T> messageClass,
                                                    final MessageBusExceptionListener<T> exceptionListener) {
        final SubscriptionId subscriptionId = SubscriptionId.newUniqueId();
        final ListenerInformation listenerInformation = new ListenerInformation(messageClass, exceptionListener);
        storeListenerInformation(messageClass, listenerInformation);
        unregisterLookupMap.put(subscriptionId, listenerInformation);
        return subscriptionId;
    }

    @Override
    public synchronized <T> SubscriptionId register(final List<Class<? extends T>> messageClasses,
                                                    final MessageBusExceptionListener<? extends T> exceptionListener) {
        final SubscriptionId subscriptionId = SubscriptionId.newUniqueId();
        final ListenerInformation listenerInformation = new ListenerInformation(messageClasses, exceptionListener);
        for (final Class<?> errorClass : messageClasses) {
            storeListenerInformation(errorClass, listenerInformation);
        }
        unregisterLookupMap.put(subscriptionId, listenerInformation);
        return subscriptionId;
    }

    private <T> void storeListenerInformation(final Class<T> errorClass, final ListenerInformation listenerInformation) {
        if (classBasedListenerLookupMap.containsKey(errorClass)) {
            final List<ListenerInformation> listenerInformationList = classBasedListenerLookupMap.get(errorClass);
            listenerInformationList.add(listenerInformation);
        } else {
            final List<ListenerInformation> listenerList = new LinkedList<>();
            listenerList.add(listenerInformation);
            classBasedListenerLookupMap.put(errorClass, listenerList);
        }
    }

    @Override
    public synchronized SubscriptionId register(final CorrelationId correlationId,
                                                final MessageBusExceptionListener<Object> exceptionListener) {
        final ListenerInformation listenerInformation = new ListenerInformation(correlationId, exceptionListener);
        if (correlationIdBasedListenerLookupMap.containsKey(correlationId)) {
            correlationIdBasedListenerLookupMap.get(correlationId).add(listenerInformation);
        } else {
            final LinkedList<ListenerInformation> listenerInformationList = new LinkedList<>();
            listenerInformationList.add(listenerInformation);
            correlationIdBasedListenerLookupMap.put(correlationId, listenerInformationList);
        }
        final SubscriptionId subscriptionId = SubscriptionId.newUniqueId();
        unregisterLookupMap.put(subscriptionId, listenerInformation);
        return subscriptionId;
    }

    @Override
    public synchronized void unregister(final SubscriptionId subscriptionId) {
        if (unregisterLookupMap.containsKey(subscriptionId)) {
            final ListenerInformation listenerInformation = unregisterLookupMap.get(subscriptionId);
            removeClassBasedListener(listenerInformation);
            removeCorrelationIdBasedListener(listenerInformation);
        }
    }

    private void removeClassBasedListener(final ListenerInformation listenerInformation) {
        final List<Class<?>> classes = listenerInformation.getRegisteredClasses();
        for (final Class<?> aClass : classes) {
            final List<ListenerInformation> listenerInformationList = classBasedListenerLookupMap.get(aClass);
            if (listenerInformationList != null) {
                listenerInformationList.remove(listenerInformation);
            }
        }
    }

    private void removeCorrelationIdBasedListener(final ListenerInformation listenerInformation) {
        final List<CorrelationId> correlationIds = listenerInformation.getRegisteredCorrelationIds();
        for (final CorrelationId correlationId : correlationIds) {
            final List<ListenerInformation> listenerInformationList = correlationIdBasedListenerLookupMap.get(correlationId);
            if (listenerInformationList != null) {
                listenerInformationList.remove(listenerInformation);
            }
        }
    }

    @Override
    public List<MessageBusExceptionListener> listenerFor(final ProcessingContext<?> processingContext) {
        final List<MessageBusExceptionListener> listeners = new LinkedList<>();
        final CorrelationId correlationId = processingContext.getCorrelationId();
        if (correlationId != null) {
            final List<MessageBusExceptionListener> correlationBasedListeners = listenerForCorrelationId(correlationId);
            listeners.addAll(correlationBasedListeners);
        }

        final Object payload = processingContext.getPayload();
        if (payload != null) {
            final Class<?> payloadClass = payload.getClass();
            final List<MessageBusExceptionListener> classBasedListener = listenerForClass(payloadClass);
            listeners.addAll(classBasedListener);
        }
        return listeners;
    }

    private List<MessageBusExceptionListener> listenerForClass(final Class<?> clazz) {
        if (classBasedListenerLookupMap.containsKey(clazz)) {
            final List<ListenerInformation> listenerInformationList = classBasedListenerLookupMap.get(clazz);
            final List<MessageBusExceptionListener> listener = extractListener(listenerInformationList);
            return listener;
        } else {
            return Collections.emptyList();
        }
    }

    private List<MessageBusExceptionListener> listenerForCorrelationId(final CorrelationId correlationId) {
        if (correlationIdBasedListenerLookupMap.containsKey(correlationId)) {
            final List<ListenerInformation> listenerInformationList = correlationIdBasedListenerLookupMap.get(correlationId);
            final List<MessageBusExceptionListener> listener = extractListener(listenerInformationList);
            return listener;
        } else {
            return Collections.emptyList();
        }
    }

    private List<MessageBusExceptionListener> extractListener(final List<ListenerInformation> listenerInformationList) {
        return listenerInformationList.stream()
                .map(ListenerInformation::getListener)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private static final class ListenerInformation {
        @Getter
        private final List<Class<?>> registeredClasses;
        @Getter
        private final MessageBusExceptionListener<?> listener;

        @Getter
        private final List<CorrelationId> registeredCorrelationIds;

        private <T> ListenerInformation(final List<?> registeredClasses, final MessageBusExceptionListener<T> listener) {
            this.registeredClasses = (List<Class<?>>) registeredClasses;
            this.listener = listener;
            this.registeredCorrelationIds = new LinkedList<>();
        }

        private ListenerInformation(final Class<?> registeredClass, final MessageBusExceptionListener<?> listener) {
            this.registeredClasses = Collections.singletonList(registeredClass);
            this.listener = listener;
            this.registeredCorrelationIds = new LinkedList<>();
        }

        private ListenerInformation(final CorrelationId registeredCorrelationId, final MessageBusExceptionListener<?> listener) {
            this.registeredClasses = new LinkedList<>();
            this.listener = listener;
            this.registeredCorrelationIds = Collections.singletonList(registeredCorrelationId);
        }
    }
}
