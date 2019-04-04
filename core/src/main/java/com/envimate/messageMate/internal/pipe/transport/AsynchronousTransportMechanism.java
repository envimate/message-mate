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

package com.envimate.messageMate.internal.pipe.transport;

import com.envimate.messageMate.internal.exceptions.BubbleUpWrappedException;
import com.envimate.messageMate.internal.pipe.events.PipeEventListener;
import com.envimate.messageMate.subscribing.Subscriber;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static lombok.AccessLevel.PUBLIC;

@RequiredArgsConstructor(access = PUBLIC)
public final class AsynchronousTransportMechanism<T> implements TransportMechanism<T> {
    private final PipeEventListener<T> eventListener;
    private final SynchronousDelivery<T> synchronousDelivery;
    private final List<Subscriber<T>> subscribers;
    private final ThreadPoolExecutor threadPoolExecutor;

    @Override
    public void transport(final T message) {
        eventListener.messageAccepted(message);
        eventListener.messageQueued(message);
        try {
            threadPoolExecutor.execute(() -> {
                eventListener.messageDequeued(message);
                try {
                    synchronousDelivery.deliver(message, subscribers);
                } catch (BubbleUpWrappedException e) {
                    throw (RuntimeException) e.getCause();
                }
            });
        } catch (final RejectedExecutionException e) {
            throw new PipeWaitingQueueIsFullException();
        }
    }

    @Override
    public void close(final boolean finishRemainingTasks) {
        if (finishRemainingTasks) {
            threadPoolExecutor.shutdown();
        } else {
            threadPoolExecutor.shutdownNow();
        }
    }

    @Override
    public boolean isShutdown() {
        return threadPoolExecutor.isShutdown();
    }

    @Override
    public boolean awaitTermination(final int timeout, final TimeUnit timeUnit) throws InterruptedException {
        return threadPoolExecutor.awaitTermination(timeout, timeUnit);
    }

}
