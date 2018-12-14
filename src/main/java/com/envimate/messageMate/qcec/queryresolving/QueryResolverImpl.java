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

package com.envimate.messageMate.qcec.queryresolving;

import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.messages.DeliveryFailedMessage;
import com.envimate.messageMate.messages.ExceptionInSubscriberException;
import com.envimate.messageMate.messages.NoSuitableSubscriberException;
import com.envimate.messageMate.subscribing.PreemptiveSubscriber;
import com.envimate.messageMate.subscribing.SubscriptionId;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class QueryResolverImpl implements QueryResolver {
    private final MessageBus messageBus;
    private final Map<Object, Throwable> exceptionsDuringQueryProgress;

    QueryResolverImpl(final MessageBus messageBus) {
        this.messageBus = messageBus;
        this.exceptionsDuringQueryProgress = new HashMap<>();
        messageBus.subscribe(DeliveryFailedMessage.class, new DeliveryFailedMessageConsumer());
    }

    @Override
    public <T extends Query> SubscriptionId answer(final Class<T> queryType, final Consumer<T> responder) {
        final PreemptiveSubscriber<T> subscriber = PreemptiveSubscriber.preemptiveSubscriber(t -> {
            responder.accept(t);
            final boolean continueDelivery = !t.finished();
            return continueDelivery;
        });
        messageBus.subscribe(queryType, subscriber);
        final SubscriptionId subscriptionId = subscriber.getSubscriptionId();
        return subscriptionId;
    }

    @Override
    public <R> Optional<R> query(final Query<R> query) {
        messageBus.send(query);
        if (exceptionsDuringQueryProgress.containsKey(query)) {
            final Optional<R> emptyOptionalForErrorCase = Optional.empty();
            exceptionsDuringQueryProgress.remove(query);
            return emptyOptionalForErrorCase;
        } else {
            return Optional.ofNullable(query.result());
        }
    }

    @Override
    public <R> R queryRequired(final Query<R> query) {
        messageBus.send(query);
        if (exceptionsDuringQueryProgress.containsKey(query)) {
            final Throwable cause = exceptionsDuringQueryProgress.get(query);
            exceptionsDuringQueryProgress.remove(query);
            throw new InvalidQueryDueToExceptionOccurredInReceiverException(cause);
        } else {
            return Optional
                    .ofNullable(query.result())
                    .orElseThrow(() -> new UnsupportedOperationException("Expected a query result for query " + query));
        }

    }

    @Override
    public void unsubscribe(final SubscriptionId subscriptionId) {
        messageBus.unsubcribe(subscriptionId);
    }

    private class DeliveryFailedMessageConsumer implements Consumer<DeliveryFailedMessage> {
        @Override
        public void accept(final DeliveryFailedMessage deliveryFailedMessage) {
            final Object originalMessage = deliveryFailedMessage.getOriginalMessage();
            if (notYetAnExceptionThrown(originalMessage)) {
                final Exception thrownException = deliveryFailedMessage.getCause();
                if (canInfluenceCorrectnessOfQuery(thrownException)) {
                    final Throwable exceptionToRethrow = getExceptionToRethrow(thrownException);
                    exceptionsDuringQueryProgress.put(originalMessage, exceptionToRethrow);
                }
            }
        }

        private boolean notYetAnExceptionThrown(final Object originalMessage) {
            return !exceptionsDuringQueryProgress.containsKey(originalMessage);
        }

        private boolean canInfluenceCorrectnessOfQuery(final Exception exception) {
            final boolean isExceptionForNorSubscriberFound = exception instanceof NoSuitableSubscriberException;
            return !isExceptionForNorSubscriberFound;
        }

        private Throwable getExceptionToRethrow(final Exception thrownException) {
            if (thrownException instanceof ExceptionInSubscriberException) {
                final Throwable originalException = thrownException.getCause();
                return originalException;
            } else {
                return thrownException;
            }
        }
    }

}
