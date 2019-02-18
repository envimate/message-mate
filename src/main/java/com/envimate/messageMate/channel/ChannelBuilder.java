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
import com.envimate.messageMate.configuration.PipeConfiguration;
import com.envimate.messageMate.pipe.Pipe;
import com.envimate.messageMate.pipe.PipeBuilder;

import static com.envimate.messageMate.channel.ChannelImpl.channel;
import static com.envimate.messageMate.channel.action.actionHandling.DefaultActionHandlerSet.defaultActionHandlerSet;
import static com.envimate.messageMate.configuration.ExceptionCatchingCondition.allThrowingExceptionCondition;
import static com.envimate.messageMate.qcec.domainBus.enforcing.NotNullEnforcer.ensureNotNull;

public class ChannelBuilder<T> {
    private Action<T> action;
    private Pipe<ProcessingContext<T>> prePipe;
    private Pipe<ProcessingContext<T>> processPipe;
    private Pipe<ProcessingContext<T>> postPipe;
    private ActionHandlerSet<T> actionHandlerSet;

    public static <T> Channel<T> aChannelWithDefaultAction(final Action<T> defaultAction) {
        return new ChannelBuilder<T>()
                .withDefaultAction(defaultAction)
                .build();
    }

    public static <T> ChannelBuilder<T> aChannel(final Class<T> channelTypeClass) {
        return new ChannelBuilder<>();
    }

    public ChannelBuilder<T> withDefaultAction(final Action<T> action) {
        this.action = action;
        return this;
    }

    public ChannelBuilder<T> withPrePipe(final Pipe<ProcessingContext<T>> prePipe) {
        this.prePipe = prePipe;
        return this;
    }

    public ChannelBuilder<T> withProcessPipe(final Pipe<ProcessingContext<T>> processPipe) {
        this.processPipe = processPipe;
        return this;
    }

    public ChannelBuilder<T> withPostPipe(final Pipe<ProcessingContext<T>> postPipe) {
        this.postPipe = postPipe;
        return this;
    }

    public ChannelBuilder<T> withActionHandlerSet(final ActionHandlerSet<T> actionHandlerSet) {
        this.actionHandlerSet = actionHandlerSet;
        return this;
    }

    public Channel<T> build() {
        ensureNotNull(action, "Action must not be null");
        final Pipe<ProcessingContext<T>> configurationPipe = createSimplePipeIfAbsent(null);
        final Pipe<ProcessingContext<T>> prePipe = createSimplePipeIfAbsent(this.prePipe);
        final Pipe<ProcessingContext<T>> processPipe = createSimplePipeIfAbsent(this.processPipe);
        final Pipe<ProcessingContext<T>> postPipe = createSimplePipeIfAbsent(this.postPipe);
        final ActionHandlerSet<T> actionHandlerSet = createDefaultActionHandlerSetIfAbsent();
        return channel(this.action, configurationPipe, prePipe, processPipe, postPipe, actionHandlerSet);
    }

    private Pipe<ProcessingContext<T>> createSimplePipeIfAbsent(final Pipe<ProcessingContext<T>> optionalPipe) {
        if (optionalPipe != null) {
            return optionalPipe;
        } else {
            final PipeConfiguration pipeConfiguration = PipeConfiguration.defaultConfiguration();
            pipeConfiguration.setExceptionCatchingCondition(allThrowingExceptionCondition());
            return PipeBuilder.<ProcessingContext<T>>aPipe()
                    .withConfiguration(pipeConfiguration)
                    .build();
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
