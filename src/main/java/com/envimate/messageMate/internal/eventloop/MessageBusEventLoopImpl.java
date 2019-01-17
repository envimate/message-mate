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

package com.envimate.messageMate.internal.eventloop;

import com.envimate.messageMate.internal.accepting.MessageAcceptingStrategy;
import com.envimate.messageMate.internal.delivering.DeliveryStrategy;
import com.envimate.messageMate.internal.statistics.StatisticsCollector;
import com.envimate.messageMate.internal.transport.MessageTransportProcess;
import com.envimate.messageMate.internal.transport.MessageTransportProcessFactory;
import com.envimate.messageMate.error.DeliveryFailedMessage;
import com.envimate.messageMate.subscribing.Subscriber;
import lombok.NonNull;

import java.util.List;

import static com.envimate.messageMate.error.DeliveryFailedMessage.deliveryFailedMessage;

public class MessageBusEventLoopImpl implements EventLoop<Object> {
    private MessageAcceptingStrategy<Object> acceptingStrategy;
    private MessageTransportProcessFactory<Object> messageTransportProcessFactory;
    private DeliveryStrategy<Object> deliveryStrategy;
    private StatisticsCollector statisticsCollector;

    public void setRequiredObjects(@NonNull final MessageAcceptingStrategy<Object> acceptingStrategy,
                                   @NonNull final MessageTransportProcessFactory<Object> messageTransportProcessFactory,
                                   @NonNull final DeliveryStrategy<Object> deliveryStrategy,
                                   @NonNull final StatisticsCollector statisticsCollector) {
        this.acceptingStrategy = acceptingStrategy;
        this.messageTransportProcessFactory = messageTransportProcessFactory;
        this.deliveryStrategy = deliveryStrategy;
        this.statisticsCollector = statisticsCollector;
    }

    @Override
    public void messageAccepted(final Object message) {
        statisticsCollector.informMessageAccepted();
    }

    @Override
    public void messageDropped(final Object message) {

    }

    @Override
    public void messageQueued(final Object message) {
        statisticsCollector.informMessageQueued();
    }

    @Override
    public void messageDequeued(final Object message) {
        statisticsCollector.informMessageDequeued();
    }

    @Override
    public boolean requestTransport(final Object message) {
        final MessageTransportProcess<Object> transportProcess = messageTransportProcessFactory.getNext(message);
        transportProcess.start(message);
        return true;
    }

    @Override
    public void messageTransportStarted(final Object message) {
        statisticsCollector.informTransportStarted();
    }

    @Override
    public void messageFilteringStarted(final Object message) {

    }

    @Override
    public void messagePassedAllFilter(final Object message) {
    }

    @Override
    public void messageBlockedByFilter(final Object message) {
        statisticsCollector.informMessageDropped();
    }

    @Override
    public void messageReplacedByFilter(final Object message) {
        statisticsCollector.informMessageReplaced();
        acceptingStrategy.accept(message);
    }

    @Override
    public void messageForgottenByFilter(final Object message) {
        statisticsCollector.informMessageForgotten();
    }

    @Override
    public void messageTransportFinished(final Object message) {
        statisticsCollector.informMessageTransportFinished();
    }

    @Override
    public boolean requestDelivery(final Object message, final List<Subscriber<Object>> receivers) {
        deliveryStrategy.deliver(message, receivers);
        return true;
    }

    @Override
    public void messageDeliveryStarted(final Object message) {
        statisticsCollector.informDeliveryStarted();
    }

    @Override
    public void messageDeliverySuccess(final Object message) {
        statisticsCollector.informMessageDeliverySuccess();
        this.acceptingStrategy.markSuccessfulDelivered(message);
    }

    @Override
    public void messageDeliveryFailure(final Object message, final Exception cause) {
        this.acceptingStrategy.markDeliveryAborted(message);
        if (isNotDeliveryFailedMessage(message)) {
            statisticsCollector.informMessageDeliveryFailed();
            final DeliveryFailedMessage<Object> deliveryFailedMessage = deliveryFailedMessage(message, cause);
            this.acceptingStrategy.acceptDirectFollowUpMessage(deliveryFailedMessage);
        }
    }

    @Override
    public void messageDeliveryPreempted(final Object message) {
        statisticsCollector.informMessageDeliverySuccess();
        this.acceptingStrategy.markSuccessfulDelivered(message);
    }

    private boolean isNotDeliveryFailedMessage(final Object message) {
        return !(message instanceof DeliveryFailedMessage);
    }
}
