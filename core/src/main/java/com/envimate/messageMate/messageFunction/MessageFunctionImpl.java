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

package com.envimate.messageMate.messageFunction;

import com.envimate.messageMate.channel.ProcessingContext;
import com.envimate.messageMate.identification.CorrelationId;
import com.envimate.messageMate.identification.MessageId;
import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.messageFunction.internal.ExpectedResponseFuture;
import com.envimate.messageMate.subscribing.SubscriptionId;
import lombok.Getter;
import lombok.NonNull;

import static com.envimate.messageMate.channel.ProcessingContext.processingContext;
import static com.envimate.messageMate.identification.CorrelationId.correlationIdFor;
import static com.envimate.messageMate.identification.MessageId.newUniqueMessageId;
import static com.envimate.messageMate.messageFunction.internal.ExpectedResponseFuture.expectedResponseFuture;

final class MessageFunctionImpl implements MessageFunction {
    private final MessageBus messageBus;
    private volatile boolean closed;

    private MessageFunctionImpl(@NonNull final MessageBus messageBus) {
        this.messageBus = messageBus;
    }

    static MessageFunctionImpl messageFunction(@NonNull final MessageBus messageBus) {
        return new MessageFunctionImpl(messageBus);
    }

    //TODO: fullfill future once
    //TODO: unsubscribe when finished
    @Override
    public ResponseFuture request(final Object request) {
        if (closed) {
            return null; //TODO: throw error
        }
        final RequestHandle requestHandle = new RequestHandle(messageBus);
        requestHandle.send(request);
        return requestHandle.getResponseFuture();
    }


    //No automatic cancel right now
    @Override
    public void close() {
        closed = true;
    }

    private final class RequestHandle {
        @Getter
        private final ExpectedResponseFuture responseFuture;
        private final MessageBus messageBus;
        private final SubscriptionContainer subscriptionContainer;
        private volatile boolean alreadyFinishedOrCancelled;

        public RequestHandle(final MessageBus messageBus) {
            this.messageBus = messageBus;
            this.responseFuture = expectedResponseFuture();
            this.subscriptionContainer = new SubscriptionContainer();
        }

        public synchronized void send(final Object request) {
            final MessageId messageId = newUniqueMessageId();
            final CorrelationId correlationId = correlationIdFor(messageId);
            final SubscriptionId answerSubscriptionId = messageBus.subscribe(correlationId, processingContext -> {
                fulFillFuture(processingContext);
                subscriptionContainer.unsubscribe(messageBus);
            });
            final SubscriptionId errorSubscriptionId1 = messageBus.onException(correlationId, (o, e) -> {
                fulFillFuture(e);
                subscriptionContainer.unsubscribe(messageBus);
            });
            final SubscriptionId errorSubsciptionId2 = messageBus.onException(request.getClass(), (o, e) -> {
                if (o == request) {
                    fulFillFuture(e);
                    subscriptionContainer.unsubscribe(messageBus);
                }
            });
            subscriptionContainer.setSubscriptionIds(answerSubscriptionId, errorSubscriptionId1, errorSubsciptionId2);

            final ProcessingContext<Object> processingContext = processingContext(request, messageId, null);
            messageBus.send(processingContext);
        }

        private synchronized void fulFillFuture(final ProcessingContext<Object> processingContext) {
            if (alreadyFinishedOrCancelled) {
                return;
            }
            alreadyFinishedOrCancelled = true;
            final Object payload = processingContext.getPayload();
            responseFuture.fullFill(payload);
        }

        private synchronized void fulFillFuture(final Exception exception) {
            if (alreadyFinishedOrCancelled) {
                return;
            }
            alreadyFinishedOrCancelled = true;
            responseFuture.fullFillWithException(exception);
        }
    }

    private final class SubscriptionContainer {
        private volatile SubscriptionId answerSubscriptionId;
        private volatile SubscriptionId errorSubscriptionId1;
        private volatile SubscriptionId errorSubscriptionId2;

        public void setSubscriptionIds(final SubscriptionId answerSubscriptionId, final SubscriptionId errorSubscriptionId1,
                                       final SubscriptionId errorSubscriptionId2) {
            this.answerSubscriptionId = answerSubscriptionId;
            this.errorSubscriptionId1 = errorSubscriptionId1;
            this.errorSubscriptionId2 = errorSubscriptionId2;
        }

        public void unsubscribe(final MessageBus messageBus) {
            if (answerSubscriptionId != null) {
                messageBus.unsubcribe(answerSubscriptionId);
            }
            if (errorSubscriptionId1 != null) {
                messageBus.unregisterExceptionListener(errorSubscriptionId1);
            }
            if (errorSubscriptionId2 != null) {
                messageBus.unregisterExceptionListener(errorSubscriptionId2);
            }
        }
    }
}
