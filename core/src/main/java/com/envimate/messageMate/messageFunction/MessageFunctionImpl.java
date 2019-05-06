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

package com.envimate.messageMate.messageFunction;

import com.envimate.messageMate.exceptions.AlreadyClosedException;
import com.envimate.messageMate.identification.CorrelationId;
import com.envimate.messageMate.identification.MessageId;
import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.messageFunction.internal.ExpectedResponseFuture;
import com.envimate.messageMate.messageFunction.internal.SubscriptionContainer;
import com.envimate.messageMate.processingContext.EventType;
import com.envimate.messageMate.processingContext.ProcessingContext;
import com.envimate.messageMate.subscribing.SubscriptionId;
import lombok.Getter;
import lombok.NonNull;

import static com.envimate.messageMate.identification.CorrelationId.correlationIdFor;
import static com.envimate.messageMate.identification.MessageId.newUniqueMessageId;
import static com.envimate.messageMate.messageFunction.internal.ExpectedResponseFuture.expectedResponseFuture;
import static com.envimate.messageMate.messageFunction.internal.SubscriptionContainer.subscriptionContainer;
import static com.envimate.messageMate.processingContext.ProcessingContext.processingContext;

final class MessageFunctionImpl implements MessageFunction {
    private final MessageBus messageBus;
    private volatile boolean closed;

    private MessageFunctionImpl(@NonNull final MessageBus messageBus) {
        this.messageBus = messageBus;
    }

    static MessageFunctionImpl messageFunction(@NonNull final MessageBus messageBus) {
        return new MessageFunctionImpl(messageBus);
    }

    @Override
    public ResponseFuture request(final EventType eventType, final Object request) {
        if (closed) {
            throw new AlreadyClosedException();
        }
        final RequestHandle requestHandle = new RequestHandle(messageBus);
        requestHandle.send(eventType, request);
        return requestHandle.getResponseFuture();
    }

    //No automatic cancel right now
    @Override
    public void close() {
        closed = true;
    }

    private static final class RequestHandle {
        @Getter
        private final ExpectedResponseFuture responseFuture;
        private final MessageBus messageBus;
        private final SubscriptionContainer subscriptionContainer;
        private volatile boolean alreadyFinishedOrCancelled;

        RequestHandle(final MessageBus messageBus) {
            this.messageBus = messageBus;
            this.subscriptionContainer = subscriptionContainer(messageBus);
            this.responseFuture = expectedResponseFuture(this.subscriptionContainer);
        }

        public synchronized void send(final EventType eventType, final Object request) {
            final MessageId messageId = newUniqueMessageId();
            final CorrelationId correlationId = correlationIdFor(messageId);
            final SubscriptionId answerSubscriptionId = messageBus.subscribe(correlationId, this::fulFillFuture);
            final SubscriptionId errorSubscriptionId1 = messageBus.onException(correlationId, (processingContext, e) -> {
                fulFillFuture(e);
            });
            final SubscriptionId errorSubscriptionId2 = messageBus.onException(eventType, (processingContext, e) -> {
                if (processingContext.getPayload() == request) {
                    fulFillFuture(e);
                }
            });
            subscriptionContainer.setSubscriptionIds(answerSubscriptionId, errorSubscriptionId1, errorSubscriptionId2);

            final ProcessingContext<Object> processingContext = processingContext(eventType, messageId, request);
            try {
                messageBus.send(processingContext);
            } catch (final Exception e) {
                fulFillFuture(e);
            }
        }

        private synchronized void fulFillFuture(final ProcessingContext<Object> processingContext) {
            if (alreadyFinishedOrCancelled) {
                return;
            }
            alreadyFinishedOrCancelled = true;
            responseFuture.fullFill(processingContext);
        }

        private synchronized void fulFillFuture(final Exception exception) {
            if (alreadyFinishedOrCancelled) {
                return;
            }
            alreadyFinishedOrCancelled = true;
            responseFuture.fullFillWithException(exception);
        }
    }

}
