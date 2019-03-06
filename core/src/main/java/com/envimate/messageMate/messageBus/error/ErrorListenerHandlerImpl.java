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

package com.envimate.messageMate.messageBus.error;

import com.envimate.messageMate.subscribing.SubscriptionId;
import lombok.Getter;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class ErrorListenerHandlerImpl implements ErrorListenerHandler {
    private final Map<Class<?>, List<ListenerInformation>> listenerLookupMap = new ConcurrentHashMap<>();
    private final Map<SubscriptionId, ListenerInformation> unregisterLookupMap = new ConcurrentHashMap<>();

    public static ErrorListenerHandlerImpl errorListenerHandler() {
        return new ErrorListenerHandlerImpl();
    }

    @Override
    public synchronized <T> SubscriptionId register(final Class<T> errorClass, final BiConsumer<T, Exception> exceptionListener) {
        final SubscriptionId subscriptionId = SubscriptionId.newUniqueId();
        final ListenerInformation listenerInformation = new ListenerInformation(errorClass, exceptionListener);
        storeListenerInformation(errorClass, listenerInformation);
        unregisterLookupMap.put(subscriptionId, listenerInformation);
        return subscriptionId;
    }

    @Override
    public synchronized <T> SubscriptionId register(final List<Class<? extends T>> errorClasses,
                                                    final BiConsumer<? extends T, Exception> exceptionListener) {
        final SubscriptionId subscriptionId = SubscriptionId.newUniqueId();
        final ListenerInformation listenerInformation = new ListenerInformation(errorClasses, exceptionListener);
        for (final Class<?> errorClass : errorClasses) {
            storeListenerInformation(errorClass, listenerInformation);
        }
        unregisterLookupMap.put(subscriptionId, listenerInformation);
        return subscriptionId;
    }

    private <T> void storeListenerInformation(final Class<T> errorClass, final ListenerInformation listenerInformation) {
        if (listenerLookupMap.containsKey(errorClass)) {
            final List<ListenerInformation> listenerInformationList = listenerLookupMap.get(errorClass);
            listenerInformationList.add(listenerInformation);
        } else {
            final List<ListenerInformation> listenerList = new LinkedList<>();
            listenerList.add(listenerInformation);
            listenerLookupMap.put(errorClass, listenerList);
        }
    }

    @Override
    public synchronized void unregister(final SubscriptionId subscriptionId) {
        if (unregisterLookupMap.containsKey(subscriptionId)) {
            final ListenerInformation listenerInformation = unregisterLookupMap.get(subscriptionId);
            final List<Class<?>> classes = listenerInformation.getRegisteredClasses();
            for (final Class<?> aClass : classes) {
                final List<ListenerInformation> listenerInformationList = listenerLookupMap.get(aClass);
                if (listenerInformationList != null) {
                    listenerInformationList.remove(listenerInformation);
                }
            }
        }
    }

    @Override
    public synchronized <T> List<BiConsumer<T, Exception>> listenerFor(final Class<T> clazz) {
        if (listenerLookupMap.containsKey(clazz)) {
            final List<ListenerInformation> listenerInformationList = listenerLookupMap.get(clazz);
            final List<?> listener = listenerInformationList.stream()
                    .map(ListenerInformation::getListener)
                    .collect(Collectors.toList());
            @SuppressWarnings("unchecked")
            final List<BiConsumer<T, Exception>> castedListener = (List<BiConsumer<T, Exception>>) listener;
            return castedListener;
        } else {
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    private static final class ListenerInformation {
        @Getter
        private final List<Class<?>> registeredClasses;
        @Getter
        private final BiConsumer<?, Exception> listener;

        private <T> ListenerInformation(final List<?> registeredClasses, final BiConsumer<T, Exception> listener) {
            this.registeredClasses = (List<Class<?>>) registeredClasses;
            this.listener = listener;
        }

        private ListenerInformation(final Class<?> registeredClass, final BiConsumer<?, Exception> listener) {
            this.registeredClasses = Collections.singletonList(registeredClass);
            this.listener = listener;
        }
    }
}
