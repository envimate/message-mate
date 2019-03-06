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

package com.envimate.messageMate.pipe;

import com.envimate.messageMate.pipe.configuration.AsynchronousConfiguration;
import com.envimate.messageMate.pipe.error.ErrorThrowingPipeErrorHandler;
import com.envimate.messageMate.pipe.error.PipeErrorHandler;
import com.envimate.messageMate.pipe.events.PipeEventListener;
import com.envimate.messageMate.pipe.events.SimplePipeEventListener;
import com.envimate.messageMate.pipe.statistics.PipeStatisticsCollector;
import com.envimate.messageMate.pipe.transport.TransportMechanism;
import com.envimate.messageMate.subscribing.Subscriber;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.CopyOnWriteArrayList;

import static com.envimate.messageMate.pipe.PipeType.ASYNCHRONOUS;
import static com.envimate.messageMate.pipe.PipeType.SYNCHRONOUS;
import static com.envimate.messageMate.pipe.statistics.AtomicPipeStatisticsCollector.atomicPipeStatisticsCollector;
import static com.envimate.messageMate.pipe.transport.TransportMechanismFactory.transportMechanism;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class PipeBuilder<T> {
    private PipeType pipeType = SYNCHRONOUS;
    private PipeStatisticsCollector statisticsCollector = atomicPipeStatisticsCollector();
    private PipeErrorHandler<T> errorHandler = new ErrorThrowingPipeErrorHandler<>();
    private PipeEventListener<T> eventListener;
    private AsynchronousConfiguration asynchronousConfiguration;

    public static <T> PipeBuilder<T> aPipe() {
        return new PipeBuilder<>();
    }

    public static <T> PipeBuilder<T> aPipeForClass(final Class<T> tClass) {
        return new PipeBuilder<>();
    }

    public PipeBuilder<T> ofType(final PipeType pipeType) {
        this.pipeType = pipeType;
        return this;
    }

    public PipeBuilder<T> withAsynchronousConfiguration(final AsynchronousConfiguration configuration) {
        this.asynchronousConfiguration = configuration;
        return this;
    }

    public PipeBuilder<T> withStatisticsCollector(final PipeStatisticsCollector statisticsCollector) {
        this.statisticsCollector = statisticsCollector;
        return this;
    }

    public PipeBuilder<T> withEventListener(final PipeEventListener<T> eventListener) {
        this.eventListener = eventListener;
        return this;
    }

    public PipeBuilder<T> withErrorHandler(final PipeErrorHandler<T> errorHandler) {
        this.errorHandler = errorHandler;
        return this;
    }

    public Pipe<T> build() {
        final PipeEventListener<T> eventListener = createEventListener();
        final CopyOnWriteArrayList<Subscriber<T>> subscribers = new CopyOnWriteArrayList<>();
        if (pipeType.equals(ASYNCHRONOUS) && asynchronousConfiguration == null) {
            throw new IllegalArgumentException("Asynchronous configuration required.");
        }
        final TransportMechanism<T> tTransportMechanism = transportMechanism(pipeType, eventListener, errorHandler,
                subscribers, asynchronousConfiguration);
        return new PipeImpl<>(tTransportMechanism, statisticsCollector, subscribers);
    }

    private PipeEventListener<T> createEventListener() {
        if (eventListener != null) {
            return eventListener;
        } else {
            return new SimplePipeEventListener<>(statisticsCollector);
        }
    }

}
