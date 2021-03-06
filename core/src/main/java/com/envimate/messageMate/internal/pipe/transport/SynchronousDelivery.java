/*
 * Copyright (c) 2019 envimate GmbH - https://envimate.com/.
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

package com.envimate.messageMate.internal.pipe.transport;

import com.envimate.messageMate.internal.exceptions.BubbleUpWrappedException;
import com.envimate.messageMate.internal.pipe.error.PipeErrorHandler;
import com.envimate.messageMate.internal.pipe.events.PipeEventListener;
import com.envimate.messageMate.internal.pipe.excepions.NoSuitableSubscriberException;
import com.envimate.messageMate.subscribing.AcceptingBehavior;
import com.envimate.messageMate.subscribing.Subscriber;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
public final class SynchronousDelivery<T> {
    private final PipeEventListener<T> eventListener;
    private final PipeErrorHandler<T> pipeErrorHandler;

    public void deliver(final T message, final List<Subscriber<T>> subscribers) {
        if (subscribers.isEmpty()) {
            handleNoSubscriberException(message);
        } else {
            for (final Subscriber<T> subscriber : subscribers) {
                final boolean continueDelivery = deliveryMessageTo(message, subscriber);
                if (!continueDelivery) {
                    return;
                }
            }
            eventListener.messageDeliverySucceeded(message);
        }
    }

    private void handleNoSubscriberException(final T message) {
        final NoSuitableSubscriberException exception = new NoSuitableSubscriberException();
        if (pipeErrorHandler.shouldErrorBeHandledAndDeliveryAborted(message, exception)) {
            eventListener.messageDeliveryFailed(message, exception);
            pipeErrorHandler.handleException(message, exception);
        } else {
            eventListener.messageDeliverySucceeded(message);
        }
    }

    private boolean deliveryMessageTo(final T message, final Subscriber<T> subscriber) {
        try {
            final AcceptingBehavior acceptingBehavior = subscriber.accept(message);
            final boolean proceedWithDelivery = acceptingBehavior.continueDelivery();
            if (!proceedWithDelivery) {
                eventListener.messageDeliverySucceeded(message);
                return false;
            } else {
                return true;
            }
        } catch (final Exception e) {
            if (e instanceof BubbleUpWrappedException) {
                throw e;
            } else {
                try {
                    if (pipeErrorHandler.shouldErrorBeHandledAndDeliveryAborted(message, e)) {
                        eventListener.messageDeliveryFailed(message, e);
                        pipeErrorHandler.handleException(message, e);
                        return false;
                    } else {
                        return true;
                    }
                } catch (final BubbleUpWrappedException bubbledException) {
                    throw bubbledException;
                } catch (final Exception rethrownException) {
                    throw new BubbleUpWrappedException(rethrownException);
                }
            }
        }
    }
}
