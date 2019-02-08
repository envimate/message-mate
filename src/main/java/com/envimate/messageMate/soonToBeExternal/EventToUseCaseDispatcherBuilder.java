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

package com.envimate.messageMate.soonToBeExternal;

import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.messageBus.MessageBusStatusInformation;
import com.envimate.messageMate.messageFunction.MessageFunction;
import com.envimate.messageMate.soonToBeExternal.building.EventToUseCaseDispatcherStep1Builder;
import com.envimate.messageMate.soonToBeExternal.building.EventToUseCaseDispatcherStep2Builder;
import com.envimate.messageMate.soonToBeExternal.building.EventToUseCaseDispatcherStep3Builder;
import com.envimate.messageMate.soonToBeExternal.building.EventToUseCaseDispatcherStep4Builder;
import com.envimate.messageMate.soonToBeExternal.eventCreating.ConstructorEventFactoryImpl;
import com.envimate.messageMate.soonToBeExternal.methodInvoking.UseCaseMethodInvoker;
import com.envimate.messageMate.soonToBeExternal.usecaseInvoking.UseCaseInvocationInformation;
import com.envimate.messageMate.soonToBeExternal.usecaseInvoking.UseCaseInvoker;
import com.envimate.messageMate.subscribing.AcceptingBehavior;
import com.envimate.messageMate.subscribing.Subscriber;
import com.envimate.messageMate.subscribing.SubscriptionId;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.envimate.messageMate.internal.reflections.ReflectionUtils.getConstructorWithFewestArguments;
import static com.envimate.messageMate.messageFunction.MessageFunctionBuilder.aMessageFunction;
import static com.envimate.messageMate.soonToBeExternal.UseCaseCallResponse.useCaseCallResponseForException;
import static com.envimate.messageMate.soonToBeExternal.UseCaseCallResponse.useCaseCallResponseForSuccess;
import static com.envimate.messageMate.soonToBeExternal.eventCreating.ConstructorEventFactoryImpl.constructorEventFactory;
import static com.envimate.messageMate.soonToBeExternal.usecaseInvoking.ClassBasedUseCaseInvokerImpl.classBasedUseCaseInvoker;
import static com.envimate.messageMate.soonToBeExternal.usecaseInvoking.ObjectBasedUseCaseInvokerImpl.objectBasedUseCaseInvoker;
import static com.envimate.messageMate.subscribing.AcceptingBehavior.MESSAGE_ACCEPTED;
import static lombok.AccessLevel.PRIVATE;

public class EventToUseCaseDispatcherBuilder implements EventToUseCaseDispatcherStep1Builder,
        EventToUseCaseDispatcherStep2Builder,
        EventToUseCaseDispatcherStep3Builder,
        EventToUseCaseDispatcherStep4Builder {
    private final Map<Class<?>, EventFactory> eventFactories = new HashMap<>();
    private final Map<Class<?>, UseCaseInvocationInformation> useCaseInvokingInformationMap = new HashMap<>();
    private MessageBus messageBus;
    private Object currentUseCaseObject;
    private Class<?> currentEventClass;
    private EventFactory currentEventFactory;

    public static EventToUseCaseDispatcherStep1Builder anEventToUseCaseDispatcher() {
        return new EventToUseCaseDispatcherBuilder();
    }

    @Override
    public EventToUseCaseDispatcherStep2Builder invokingUseCase(final Object object) {
        storeCurrentUseCaseInformation();
        currentUseCaseObject = object;
        return this;
    }

    @Override
    public EventToUseCaseDispatcherStep3Builder forEvent(final Class<?> eventClass) {
        final Constructor<?> constructor = getConstructorWithFewestArguments(eventClass);
        final ConstructorEventFactoryImpl eventFactory = constructorEventFactory(eventClass, constructor);
        return forEvent(eventClass, eventFactory);
    }

    private EventToUseCaseDispatcherStep3Builder forEvent(final Class<?> eventClass, final EventFactory eventFactory) {
        this.currentEventClass = eventClass;
        this.currentEventFactory = eventFactory;
        return this;
    }

    @Override
    public EventToUseCaseDispatcherStep4Builder usingMessageBus(final MessageBus messageBus) {
        this.messageBus = messageBus;
        return this;
    }

    @Override
    public EventToUseCaseDispatcher build() {
        storeCurrentUseCaseInformation();
        final MessageFunction<UseCaseCallRequest, UseCaseCallResponse> messageFunction = aMessageFunction()
                .forRequestType(UseCaseCallRequest.class)
                .forResponseType(UseCaseCallResponse.class)
                .with(UseCaseCallRequest.class).answeredBy(UseCaseCallResponse.class)
                .obtainingCorrelationIdsOfRequestsWith(UseCaseCallRequest::getCorrelationId)
                .obtainingCorrelationIdsOfResponsesWith(UseCaseCallResponse::getCorrelationId)
                .usingMessageBus(messageBus)
                .build();

        ensureSharedSubscriberSingletonSubscribedOnMessagesBus(messageBus);
        return new EventToUseCaseDispatcherImpl(eventFactories, useCaseInvokingInformationMap, messageFunction);
    }

    private void storeCurrentUseCaseInformation() {
        if (currentUseCaseObject != null) {
            final UseCaseInvoker useCaseInvoker = createUseCaseInvoker();
            final EventFactory eventFactory = useCaseInvoker.getEventFactory();
            final Class<?> eventType = eventFactory.eventType();
            if (!eventFactories.containsKey(eventType)) {
                eventFactories.put(eventType, eventFactory);
                final UseCaseInvocationInformation invocationInformation = useCaseInvoker.getInvocationInformation();
                useCaseInvokingInformationMap.put(eventType, invocationInformation);
            } else {
                throw new UnsupportedOperationException("A UseCase for event " + eventType + " is already registered.");
            }
        }
        currentUseCaseObject = null;
        currentEventClass = null;
        currentEventFactory = null;
    }

    private UseCaseInvoker createUseCaseInvoker() {
        if (currentUseCaseObject instanceof Class<?>) {
            final Class<?> useCaseClass = (Class<?>) currentUseCaseObject;
            if (currentEventClass != null) {
                return classBasedUseCaseInvoker(useCaseClass, currentEventFactory);
            } else {
                return classBasedUseCaseInvoker(useCaseClass);
            }
        } else {
            if (currentEventClass != null) {
                return objectBasedUseCaseInvoker(currentUseCaseObject, currentEventFactory);
            } else {
                return objectBasedUseCaseInvoker(currentUseCaseObject);
            }
        }
    }

    private void ensureSharedSubscriberSingletonSubscribedOnMessagesBus(final MessageBus messageBus) {
        if (notAlreadyOnUseCaseRequestSubscriberIncluded(messageBus)) {
            final UseCaseRequestExecutingSubscriber useCaseRequestSubscriber = new UseCaseRequestExecutingSubscriber(messageBus);
            messageBus.subscribe(UseCaseCallRequest.class, useCaseRequestSubscriber);
        }
    }

    private boolean notAlreadyOnUseCaseRequestSubscriberIncluded(final MessageBus messageBus) {
        final MessageBusStatusInformation statusInformation = messageBus.getStatusInformation();
        final Map<Object, List<Subscriber<Object>>> subscribersPerType = statusInformation.getSubscribersPerType();
        if (!subscribersPerType.containsKey(UseCaseCallRequest.class)) {
            return true;
        } else {
            final List<Subscriber<Object>> subscribers = subscribersPerType.get(UseCaseCallRequest.class);
            for (final Subscriber<Object> subscriber : subscribers) {
                if (subscriber.getClass().equals(UseCaseRequestExecutingSubscriber.class)) {
                    return false;
                }
            }
            return true;
        }
    }

    @RequiredArgsConstructor(access = PRIVATE)
    private final class UseCaseRequestExecutingSubscriber implements Subscriber<UseCaseCallRequest> {
        private final MessageBus messageBus;
        private final SubscriptionId subscriptionId = SubscriptionId.newUniqueId();

        @Override
        public AcceptingBehavior accept(final UseCaseCallRequest useCaseCallRequest) {
            try {
                final Object useCase = useCaseCallRequest.getUseCase();
                final UseCaseMethodInvoker methodInvoker = useCaseCallRequest.getMethodInvoker();
                final Object event = useCaseCallRequest.getEvent();
                final List<Object> parameter = useCaseCallRequest.getParameter();
                final Object returnValue = methodInvoker.invoke(useCase, event, parameter);
                final UseCaseCallResponse useCaseCallResponse = useCaseCallResponseForSuccess(returnValue, useCaseCallRequest);
                messageBus.send(useCaseCallResponse);
            } catch (final Exception e) {
                final UseCaseCallResponse useCaseCallResponse = useCaseCallResponseForException(useCaseCallRequest, e);
                messageBus.send(useCaseCallResponse);
            }
            return MESSAGE_ACCEPTED;
        }

        @Override
        public SubscriptionId getSubscriptionId() {
            return subscriptionId;
        }
    }

}
