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
import com.envimate.messageMate.subscribing.Subscriber;
import lombok.NonNull;

import java.util.List;

public class PipeEventLoopImpl<T> implements EventLoop<T> {
    private MessageAcceptingStrategy<T> acceptingStrategy;
    private MessageTransportProcessFactory<T> messageTransportProcessFactory;
    private DeliveryStrategy<T> deliveryStrategy;
    private StatisticsCollector statisticsCollector;

    public void setRequiredObjects(@NonNull final MessageAcceptingStrategy<T> acceptingStrategy,
                                   @NonNull final MessageTransportProcessFactory<T> messageTransportProcessFactory,
                                   @NonNull final DeliveryStrategy<T> deliveryStrategy,
                                   @NonNull final StatisticsCollector statisticsCollector) {
        this.acceptingStrategy = acceptingStrategy;
        this.messageTransportProcessFactory = messageTransportProcessFactory;
        this.deliveryStrategy = deliveryStrategy;
        this.statisticsCollector = statisticsCollector;
    }

    @Override
    public void messageAccepted(final T message) {
        statisticsCollector.informMessageAccepted();
    }

    @Override
    public void messageDropped(final T message) {

    }

    @Override
    public void messageQueued(final T message) {
        statisticsCollector.informMessageQueued();
    }

    @Override
    public void messageDequeued(final T message) {
        statisticsCollector.informMessageDequeued();
    }

    @Override
    public boolean requestTransport(final T message) {
        final MessageTransportProcess<T> transportProcess = messageTransportProcessFactory.getNext(message);
        if (transportProcess != null) {
            transportProcess.start(message);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void messageTransportStarted(final T message) {
        statisticsCollector.informTransportStarted();
    }

    @Override
    public void messageFilteringStarted(final T message) {

    }

    @Override
    public void messagePassedAllFilter(final T message) {
    }

    @Override
    public void messageBlockedByFilter(final T message) {
        statisticsCollector.informMessageDropped();
    }

    @Override
    public void messageReplacedByFilter(final T message) {
        statisticsCollector.informMessageReplaced();
        acceptingStrategy.accept(message);
    }

    @Override
    public void messageForgottenByFilter(final T message) {
        statisticsCollector.informMessageForgotten();
    }

    @Override
    public void messageTransportFinished(final T message) {
        statisticsCollector.informMessageTransportFinished();
    }

    @Override
    public boolean requestDelivery(final T message, final List<Subscriber<T>> receivers) {
        deliveryStrategy.deliver(message, receivers);
        return true;
    }

    @Override
    public void markTransportProcessesAsAvailable(final int numberOfAvailableTransportProcesses) {
        acceptingStrategy.informTransportAvailable(numberOfAvailableTransportProcesses);
    }

    @Override
    public void messageDeliveryStarted(final T message) {
        statisticsCollector.informDeliveryStarted();
    }

    @Override
    public void messageDeliverySuccess(final T message) {
        statisticsCollector.informMessageDeliverySuccess();
        this.acceptingStrategy.markSuccessfulDelivered(message);
    }

    @Override
    public void messageDeliveryFailure(final T message, final Exception cause) {
        this.acceptingStrategy.markDeliveryAborted(message);
        statisticsCollector.informMessageDeliveryFailed();
    }

    @Override
    public void messageDeliveryPreempted(final T message) {
        statisticsCollector.informMessageDeliverySuccess();
        this.acceptingStrategy.markSuccessfulDelivered(message);
    }

}
