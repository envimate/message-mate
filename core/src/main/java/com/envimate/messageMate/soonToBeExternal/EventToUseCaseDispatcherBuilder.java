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
import com.envimate.messageMate.soonToBeExternal.building.*;
import com.envimate.messageMate.subscribing.AcceptingBehavior;
import com.envimate.messageMate.subscribing.Subscriber;
import com.envimate.messageMate.subscribing.SubscriptionId;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.envimate.messageMate.messageFunction.MessageFunctionBuilder.aMessageFunction;
import static com.envimate.messageMate.soonToBeExternal.EventToUseCaseMapping.eventToUseCaseMapping;
import static com.envimate.messageMate.soonToBeExternal.UseCaseCallResponse.useCaseCallResponseForException;
import static com.envimate.messageMate.soonToBeExternal.UseCaseCallResponse.useCaseCallResponseForSuccess;
import static com.envimate.messageMate.subscribing.AcceptingBehavior.MESSAGE_ACCEPTED;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public class EventToUseCaseDispatcherBuilder implements EventToUseCaseDispatcherStep1Builder,
        EventToUseCaseDispatcherStep3Builder,
        EventToUseCaseDispatcherStep4Builder {
    private final Map<Class<?>, EventToUseCaseMapping> eventToUseCaseMappings = new HashMap<>();
    private MessageBus messageBus;

    public static EventToUseCaseDispatcherStep1Builder anEventToUseCaseDispatcher() {
        return new EventToUseCaseDispatcherBuilder();
    }

    @Override
    public <USECASE> EventToUseCaseDispatcherStep2Builder<USECASE> invokingUseCase(final Class<USECASE> useCaseClass) {
        return new EventToUseCaseDispatcherStep2Builder<USECASE>() {
            @Override
            public <EVENT> EventToUseCaseDispatcherStepCallingBuilder<USECASE, EVENT> forEvent(Class<EVENT> eventClass) {
                return caller -> {
                    eventToUseCaseMappings.put(eventClass, eventToUseCaseMapping(useCaseClass, eventClass, caller));
                    return EventToUseCaseDispatcherBuilder.this;
                };
            }
        };
    }

    @Override
    public EventToUseCaseDispatcherStep4Builder usingMessageBus(final MessageBus messageBus) {
        this.messageBus = messageBus;
        return this;
    }

    @Override
    public EventToUseCaseDispatcher obtainingUseCaseInstancesUsing(final Function<Class, Object> instantiator) {
        final MessageFunction<UseCaseCallRequest, UseCaseCallResponse> messageFunction = aMessageFunction()
                .forRequestType(UseCaseCallRequest.class)
                .forResponseType(UseCaseCallResponse.class)
                .with(UseCaseCallRequest.class).answeredBy(UseCaseCallResponse.class)
                .obtainingCorrelationIdsOfRequestsWith(UseCaseCallRequest::getCorrelationId)
                .obtainingCorrelationIdsOfResponsesWith(UseCaseCallResponse::getCorrelationId)
                .usingMessageBus(messageBus)
                .build();
        ensureSharedSubscriberSingletonSubscribedOnMessagesBus(messageBus);
        return new EventToUseCaseDispatcherImpl(instantiator, eventToUseCaseMappings, messageFunction);
    }

    private void ensureSharedSubscriberSingletonSubscribedOnMessagesBus(final MessageBus messageBus) {
        if (notAlreadyOnUseCaseRequestSubscriberIncluded(messageBus)) {
            final UseCaseRequestExecutingSubscriber useCaseRequestSubscriber = new UseCaseRequestExecutingSubscriber(messageBus);
            messageBus.subscribe(UseCaseCallRequest.class, useCaseRequestSubscriber);
        }
    }

    private boolean notAlreadyOnUseCaseRequestSubscriberIncluded(final MessageBus messageBus) {
        final MessageBusStatusInformation statusInformation = messageBus.getStatusInformation();
        final Map<Class<?>, List<Subscriber<?>>> subscribersPerType = statusInformation.getSubscribersPerType();
        if (!subscribersPerType.containsKey(UseCaseCallRequest.class)) {
            return true;
        } else {
            final List<Subscriber<?>> subscribers = subscribersPerType.get(UseCaseCallRequest.class);
            for (final Subscriber<?> subscriber : subscribers) {
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
                final Caller caller = useCaseCallRequest.getCaller();
                final Object event = useCaseCallRequest.getEvent();
                final Object returnValue = caller.call(useCase, event).orElse(null);
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
