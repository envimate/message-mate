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

package com.envimate.messageMate.internal.delivering;

import com.envimate.messageMate.configuration.ExceptionCatchingCondition;
import com.envimate.messageMate.internal.eventloop.DeliveryEventLoop;
import com.envimate.messageMate.error.ExceptionInSubscriberException;
import com.envimate.messageMate.error.NoSuitableSubscriberException;
import com.envimate.messageMate.subscribing.AcceptingBehavior;
import com.envimate.messageMate.subscribing.Subscriber;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class SynchronousDeliveryStrategy<T> implements DeliveryStrategy<T> {
    private final DeliveryEventLoop<T> eventLoop;
    private final ExceptionCatchingCondition exceptionCatchingCondition;

    @Override
    public void deliver(final T message, final List<Subscriber<T>> subscriberList) {
        eventLoop.messageDeliveryStarted(message);
        final List<Subscriber<T>> localList = new LinkedList<>(subscriberList);
        if (localList.isEmpty()) {
            final NoSuitableSubscriberException cause = new NoSuitableSubscriberException();
            eventLoop.messageDeliveryFailure(message, cause);
        } else {
            for (final Subscriber<T> subscriber : localList) {
                try {
                    final AcceptingBehavior acceptingBehavior = subscriber.accept(message);
                    final boolean proceedWithDelivery = acceptingBehavior.continueDelivery();
                    if (!proceedWithDelivery) {
                        eventLoop.messageDeliveryPreempted(message);
                        return;
                    }
                } catch (final Exception e) {
                    if (exceptionCatchingCondition.shouldBeCaught(e)) {
                        final ExceptionInSubscriberException cause = new ExceptionInSubscriberException(e);
                        eventLoop.messageDeliveryFailure(message, cause);
                        return;
                    } else {
                        eventLoop.messageDeliverySuccess(message);
                        throw e;
                    }
                }
            }
            eventLoop.messageDeliverySuccess(message);
        }
    }

    @Override
    public void close(final boolean finishRemainingTasks) {
        //intentionally left blank
    }

    @Override
    public boolean awaitTermination(final Date deadline) {
        return true;
    }

    @Override
    public boolean isShutdown() {
        return true;
    }
}
