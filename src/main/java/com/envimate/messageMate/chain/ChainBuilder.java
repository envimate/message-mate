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

package com.envimate.messageMate.chain;

import com.envimate.messageMate.chain.action.Action;
import com.envimate.messageMate.chain.action.actionHandling.ActionHandlerSet;
import com.envimate.messageMate.channel.Channel;
import com.envimate.messageMate.channel.ChannelBuilder;
import com.envimate.messageMate.configuration.ChannelConfiguration;

import static com.envimate.messageMate.chain.ChainImpl.chain;
import static com.envimate.messageMate.chain.action.actionHandling.DefaultActionHandlerSet.defaultActionHandlerSet;
import static com.envimate.messageMate.configuration.ExceptionCatchingCondition.allThrowingExceptionCondition;
import static com.envimate.messageMate.qcec.domainBus.enforcing.NotNullEnforcer.ensureNotNull;

public class ChainBuilder<T> {
    private Action<T> action;
    private Channel<ProcessingContext<T>> preChannel;
    private Channel<ProcessingContext<T>> processChannel;
    private Channel<ProcessingContext<T>> postChannel;
    private ActionHandlerSet<T> actionHandlerSet;

    public static <T> Chain<T> aChainWithDefaultAction(final Action<T> defaultAction) {
        return new ChainBuilder<T>()
                .withDefaultAction(defaultAction)
                .build();
    }

    public static <T> ChainBuilder<T> aChain(final Class<T> chainTypeClass) {
        return new ChainBuilder<>();
    }

    public ChainBuilder<T> withDefaultAction(final Action<T> action) {
        this.action = action;
        return this;
    }

    public ChainBuilder<T> withPreChannel(final Channel<ProcessingContext<T>> preChannel) {
        this.preChannel = preChannel;
        return this;
    }

    public ChainBuilder<T> withProcessChannel(final Channel<ProcessingContext<T>> processChannel) {
        this.processChannel = processChannel;
        return this;
    }

    public ChainBuilder<T> withPostChannel(final Channel<ProcessingContext<T>> postChannel) {
        this.postChannel = postChannel;
        return this;
    }

    public ChainBuilder<T> withActionHandlerSet(final ActionHandlerSet<T> actionHandlerSet) {
        this.actionHandlerSet = actionHandlerSet;
        return this;
    }

    public Chain<T> build() {
        ensureNotNull(action, "Action must not be null");
        final Channel<ProcessingContext<T>> preChannel = createSimpleChannelIfAbsent(this.preChannel);
        final Channel<ProcessingContext<T>> processChannel = createSimpleChannelIfAbsent(this.processChannel);
        final Channel<ProcessingContext<T>> postChannel = createSimpleChannelIfAbsent(this.postChannel);
        final ActionHandlerSet<T> actionHandlerSet = createDefaultActionHandlerSetIfAbsent();
        return chain(this.action, preChannel, processChannel, postChannel, actionHandlerSet);
    }

    private Channel<ProcessingContext<T>> createSimpleChannelIfAbsent(final Channel<ProcessingContext<T>> optionalChannel) {
        if (optionalChannel != null) {
            return optionalChannel;
        } else {
            final ChannelConfiguration channelConfiguration = ChannelConfiguration.defaultConfiguration();
            channelConfiguration.setExceptionCatchingCondition(allThrowingExceptionCondition());
            return ChannelBuilder.<ProcessingContext<T>>aChannel()
                    .withConfiguration(channelConfiguration)
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
