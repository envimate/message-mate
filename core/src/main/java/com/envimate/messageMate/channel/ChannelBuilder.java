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

package com.envimate.messageMate.channel;

import com.envimate.messageMate.channel.action.Action;
import com.envimate.messageMate.channel.action.actionHandling.ActionHandlerSet;
import com.envimate.messageMate.channel.error.ChannelExceptionHandler;
import com.envimate.messageMate.channel.events.ChannelEventListener;
import com.envimate.messageMate.channel.statistics.ChannelStatisticsCollector;
import com.envimate.messageMate.channel.statistics.PipeStatisticsBasedChannelStatisticsCollector;
import com.envimate.messageMate.pipe.Pipe;
import com.envimate.messageMate.pipe.PipeBuilder;
import com.envimate.messageMate.pipe.PipeType;
import com.envimate.messageMate.pipe.configuration.AsynchronousConfiguration;
import com.envimate.messageMate.pipe.error.PipeErrorHandler;

import static com.envimate.messageMate.channel.ChannelImpl.channel;
import static com.envimate.messageMate.channel.ChannelType.SYNCHRONOUS;
import static com.envimate.messageMate.channel.action.actionHandling.DefaultActionHandlerSet.defaultActionHandlerSet;
import static com.envimate.messageMate.channel.error.ErrorThrowingChannelExceptionHandler.errorThrowingChannelExceptionHandler;
import static com.envimate.messageMate.channel.events.SimpleChannelEventListener.simpleChannelEventListener;
import static com.envimate.messageMate.channel.statistics.PipeStatisticsBasedChannelStatisticsCollector.pipeStatisticsBasedChannelStatisticsCollector;
import static com.envimate.messageMate.qcec.domainBus.enforcing.NotNullEnforcer.ensureNotNull;

public class ChannelBuilder<T> {
    private Action<T> action;
    private ActionHandlerSet<T> actionHandlerSet;
    private ChannelEventListener<ProcessingContext<T>> eventListener;
    private ChannelStatisticsCollector statisticsCollector;
    private ChannelExceptionHandler<T> channelExceptionHandler = errorThrowingChannelExceptionHandler();
    private ChannelType type = SYNCHRONOUS;
    private AsynchronousConfiguration asynchronousConfiguration;

    public static <T> Channel<T> aChannelWithDefaultAction(final Action<T> defaultAction) {
        return new ChannelBuilder<T>()
                .withDefaultAction(defaultAction)
                .build();
    }

    public static <T> ChannelBuilder<T> aChannel() {
        return new ChannelBuilder<>();
    }

    public static <T> ChannelBuilder<T> aChannel(final Class<T> channelTypeClass) {
        return new ChannelBuilder<>();
    }

    public ChannelBuilder<T> forType(final ChannelType type) {
        this.type = type;
        return this;
    }

    public ChannelBuilder<T> withAsynchronousConfiguration(final AsynchronousConfiguration configuration) {
        this.asynchronousConfiguration = configuration;
        return this;
    }

    public ChannelBuilder<T> withDefaultAction(final Action<T> action) {
        this.action = action;
        return this;
    }

    public ChannelBuilder<T> withChannelExceptionHandler(final ChannelExceptionHandler<T> channelExceptionHandler) {
        this.channelExceptionHandler = channelExceptionHandler;
        return this;
    }

    public ChannelBuilder<T> withActionHandlerSet(final ActionHandlerSet<T> actionHandlerSet) {
        this.actionHandlerSet = actionHandlerSet;
        return this;
    }

    public Channel<T> build() {
        ensureNotNull(action, "Action must not be null");
        final Pipe<ProcessingContext<T>> acceptingPipe = createAcceptingPipe();
        final Pipe<ProcessingContext<T>> prePipe = createSynchronousPipe();
        final Pipe<ProcessingContext<T>> processPipe = createSynchronousPipe();
        final Pipe<ProcessingContext<T>> postPipe = createDeliveringPipe();
        createStatisticsCollectorAndEventListenerSetup(acceptingPipe, postPipe);
        final ActionHandlerSet<T> actionHandlerSet = createDefaultActionHandlerSetIfAbsent();
        return channel(this.action, acceptingPipe, prePipe, processPipe, postPipe, eventListener, statisticsCollector,
                actionHandlerSet, channelExceptionHandler);
    }

    private Pipe<ProcessingContext<T>> createAcceptingPipe() {
        switch (type) {
            case SYNCHRONOUS:
                return createSynchronousPipe();
            case ASYNCHRONOUS:
                return PipeBuilder.<ProcessingContext<T>>aPipe()
                        .ofType(PipeType.ASYNCHRONOUS)
                        .withAsynchronousConfiguration(asynchronousConfiguration)
                        .build();
            default:
                throw new IllegalArgumentException("Unsupported channel type: " + type);
        }
    }

    private Pipe<ProcessingContext<T>> createSynchronousPipe() {
        final PipeBuilder<ProcessingContext<T>> pipeBuilder = PipeBuilder.aPipe();
        return pipeBuilder.ofType(PipeType.SYNCHRONOUS).build();
    }

    private Pipe<ProcessingContext<T>> createDeliveringPipe() {
        return PipeBuilder.<ProcessingContext<T>>aPipe()
                .ofType(PipeType.SYNCHRONOUS)
                .withErrorHandler(new PipeErrorHandler<ProcessingContext<T>>() {
                    @Override
                    public boolean shouldErrorBeHandledAndDeliveryAborted(final ProcessingContext<T> message, final Exception e) {
                        return channelExceptionHandler.shouldSubscriberErrorBeHandledAndDeliveryAborted(message, e);
                    }

                    @Override
                    public void handleException(final ProcessingContext<T> message, final Exception e) {
                        channelExceptionHandler.handleSubscriberException(message, e);
                    }
                }).build();
    }

    private void createStatisticsCollectorAndEventListenerSetup(final Pipe<ProcessingContext<T>> acceptingPipe,
                                                                final Pipe<ProcessingContext<T>> postPipe) {
        if (eventListener == null && statisticsCollector == null) {
            final PipeStatisticsBasedChannelStatisticsCollector statisticsCollector =
                    pipeStatisticsBasedChannelStatisticsCollector(acceptingPipe, postPipe);
            this.statisticsCollector = statisticsCollector;
            this.eventListener = simpleChannelEventListener(statisticsCollector);
        }
    }

    private ActionHandlerSet<T> createDefaultActionHandlerSetIfAbsent() {
        if (this.actionHandlerSet != null) {
            return this.actionHandlerSet;
        } else {
            return defaultActionHandlerSet();
        }
    }
}
