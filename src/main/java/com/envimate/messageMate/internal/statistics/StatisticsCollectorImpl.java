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

package com.envimate.messageMate.internal.statistics;

import lombok.RequiredArgsConstructor;

import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import static com.envimate.messageMate.internal.statistics.MessageStatistics.messageStatistics;
import static java.math.BigInteger.valueOf;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
final class StatisticsCollectorImpl implements StatisticsCollector {
    private final AtomicLong messagesAccepted = new AtomicLong(0);
    private final AtomicLong messagesDropped = new AtomicLong(0);
    private final AtomicLong messagesReplaced = new AtomicLong(0);
    private final AtomicLong messagesForgotten = new AtomicLong(0);
    private final AtomicLong messagesDeliverySuccessful = new AtomicLong(0);
    private final AtomicLong messagesDeliveryFailed = new AtomicLong(0);
    private final AtomicLong waitingMessages = new AtomicLong(0);
    private final AtomicLong currentlyTransportedMessages = new AtomicLong(0);
    private final AtomicLong currentlyDeliveredMessages = new AtomicLong(0);

    static StatisticsCollectorImpl statisticsCollector() {
        return new StatisticsCollectorImpl();
    }

    @Override
    public void informMessageAccepted() {
        messagesAccepted.incrementAndGet();
    }

    @Override
    public void informMessageQueued() {
        waitingMessages.incrementAndGet();
    }

    @Override
    public void informMessageDequeued() {
        waitingMessages.decrementAndGet();
    }

    @Override
    public void informMessageDropped() {
        messagesDropped.incrementAndGet();
    }

    @Override
    public void informMessageReplaced() {
        messagesReplaced.incrementAndGet();
    }

    @Override
    public void informMessageForgotten() {
        messagesForgotten.incrementAndGet();
    }

    @Override
    public void informTransportStarted() {
        currentlyTransportedMessages.incrementAndGet();
    }

    @Override
    public void informMessageTransportFinished() {
        currentlyTransportedMessages.decrementAndGet();
    }

    @Override
    public void informDeliveryStarted() {
        final long tmp = currentlyDeliveredMessages.incrementAndGet();
    }

    @Override
    public void informMessageDeliverySuccess() {
        currentlyDeliveredMessages.decrementAndGet();
        messagesDeliverySuccessful.incrementAndGet();
    }

    @Override
    public void informMessageDeliveryFailed() {
        currentlyDeliveredMessages.decrementAndGet();
        messagesDeliveryFailed.incrementAndGet();
    }

    @Override
    public MessageStatistics getCurrentStatistics() {
        final long accepted;
        final long dropped;
        final long failed;
        final long replaced;
        final long forgotten;
        final long successful;
        final long waiting;
        final long currentlyTransported;
        final long currentlyDelivered;
        final Date timestamp;
        synchronized (this) {
            accepted = messagesAccepted.get();
            dropped = messagesDropped.get();
            failed = messagesDeliveryFailed.get();
            replaced = messagesReplaced.get();
            forgotten = messagesForgotten.get();
            successful = messagesDeliverySuccessful.get();
            waiting = waitingMessages.get();
            currentlyTransported = currentlyTransportedMessages.get();
            currentlyDelivered = currentlyDeliveredMessages.get();
            timestamp = new Date();
        }
        return messageStatistics(timestamp, valueOf(accepted), valueOf(successful), valueOf(failed),
                valueOf(dropped), valueOf(replaced), valueOf(forgotten), valueOf(waiting),
                valueOf(currentlyTransported), valueOf(currentlyDelivered));
    }
}
