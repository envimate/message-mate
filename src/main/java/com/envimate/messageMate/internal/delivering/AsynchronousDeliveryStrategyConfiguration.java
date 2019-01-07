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

import com.envimate.messageMate.configuration.ChannelConfiguration;
import com.envimate.messageMate.configuration.ExceptionCatchingCondition;
import com.envimate.messageMate.configuration.MessageBusConfiguration;
import lombok.*;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AsynchronousDeliveryStrategyConfiguration {

    @Getter
    private final BlockingQueue<Runnable> workingQueue;

    @Getter
    private final TimeUnit timeUnit;

    @Getter
    private final int timeout;

    @Getter
    private final int maximumPoolSize;

    @Getter
    private final int corePoolSize;

    @Getter
    private final ExceptionCatchingCondition exceptionCatchingCondition;

    public static AsynchronousDeliveryStrategyConfiguration asynchronousDeliveryStrategyConfiguration(
            @NonNull final BlockingQueue<Runnable> workingQueue,
            @NonNull final TimeUnit timeUnit,
            final int timeout,
            final int maximumPoolSize,
            final int corePoolSize,
            final ExceptionCatchingCondition exceptionCatchingCondition) {
        return new AsynchronousDeliveryStrategyConfiguration(workingQueue, timeUnit, timeout, maximumPoolSize,
                corePoolSize, exceptionCatchingCondition);
    }

    public static AsynchronousDeliveryStrategyConfiguration fromMessageBusConfiguration(
            final MessageBusConfiguration configuration) {
        final BlockingQueue<Runnable> threadPoolWorkingQueue = configuration.getThreadPoolWorkingQueue();
        final TimeUnit timeoutTimeUnit = configuration.getTimeoutTimeUnit();
        final int maximumTimeout = configuration.getMaximumTimeout();
        final int maximumPoolSize = configuration.getMaximumPoolSize();
        final int corePoolSize = configuration.getCorePoolSize();
        final ExceptionCatchingCondition exceptionCatchingCondition = configuration.getExceptionCatchingCondition();
        return asynchronousDeliveryStrategyConfiguration(threadPoolWorkingQueue, timeoutTimeUnit, maximumTimeout,
                maximumPoolSize, corePoolSize, exceptionCatchingCondition);
    }

    public static AsynchronousDeliveryStrategyConfiguration fromChannelConfiguration(
            final ChannelConfiguration configuration) {
        final BlockingQueue<Runnable> threadPoolWorkingQueue = configuration.getThreadPoolWorkingQueue();
        final TimeUnit timeoutTimeUnit = configuration.getTimeoutTimeUnit();
        final int maximumTimeout = configuration.getMaximumTimeout();
        final int maximumPoolSize = configuration.getMaximumPoolSize();
        final int corePoolSize = configuration.getCorePoolSize();
        final ExceptionCatchingCondition exceptionCatchingCondition = configuration.getExceptionCatchingCondition();
        return asynchronousDeliveryStrategyConfiguration(threadPoolWorkingQueue, timeoutTimeUnit, maximumTimeout,
                maximumPoolSize, corePoolSize, exceptionCatchingCondition);
    }

}
