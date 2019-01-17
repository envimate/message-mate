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

import com.envimate.messageMate.error.ExceptionInSubscriberException;
import com.envimate.messageMate.error.NoSuitableSubscriberException;
import com.envimate.messageMate.internal.eventloop.DeliveryEventLoop;
import com.envimate.messageMate.subscribing.Subscriber;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/*
depending of implementation of Queue, queue can grow into infinity
 */
final class AsynchronousDeliveryStrategy<T> implements DeliveryStrategy<T> {
    private final ThreadPoolExecutor threadPoolExecutor;
    private final DeliveryEventLoop<T> eventLoop;

    AsynchronousDeliveryStrategy(final AsynchronousDeliveryStrategyConfiguration configuration,
                                 final DeliveryEventLoop<T> eventLoop) {
        final BlockingQueue<Runnable> workingQueue = configuration.getWorkingQueue();
        final TimeUnit timeUnit = configuration.getTimeUnit();
        final int timeout = configuration.getTimeout();
        final int maximumPoolSize = configuration.getMaximumPoolSize();
        final int corePoolSize = configuration.getCorePoolSize();
        threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, timeout, timeUnit, workingQueue);
        this.eventLoop = eventLoop;
    }

    @Override
    public void deliver(final T message, final List<Subscriber<T>> subscriberList) {
        eventLoop.messageDeliveryStarted(message);
        final List<Subscriber<T>> localList = new LinkedList<>(subscriberList);
        if (localList.isEmpty()) {
            final NoSuitableSubscriberException cause = new NoSuitableSubscriberException();
            eventLoop.messageDeliveryFailure(message, cause);
        } else {
            final AtomicInteger remainingDeliveries = new AtomicInteger(localList.size());
            try {
                for (final Subscriber<T> subscriber : localList) {
                    threadPoolExecutor.execute(() -> {
                                try {
                                    subscriber.accept(message);
                                } catch (final Exception e) {
                                    final ExceptionInSubscriberException cause = new ExceptionInSubscriberException(e);
                                    eventLoop.messageDeliveryFailure(message, cause);
                                    return;
                                }
                                if (remainingDeliveries.decrementAndGet() == 0) {
                                    eventLoop.messageDeliverySuccess(message);
                                }
                            }
                    );

                }
            } catch (final RejectedExecutionException e) {
                if (!threadPoolExecutor.isShutdown()) {
                    throw e;
                }
            }
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
    public boolean awaitTermination(final Date deadline) throws InterruptedException {
        final long currentTimeMillis = System.currentTimeMillis();
        final long deadlineMillis = deadline.getTime();
        if (deadlineMillis <= currentTimeMillis) {
            return threadPoolExecutor.isShutdown();
        } else {
            final long remainingMilliseconds = deadlineMillis - currentTimeMillis;
            return threadPoolExecutor.awaitTermination(remainingMilliseconds, MILLISECONDS);
        }
    }

    @Override
    public boolean isShutdown() {
        return threadPoolExecutor.isShutdown();
    }
}
