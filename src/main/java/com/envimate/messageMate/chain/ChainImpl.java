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
import com.envimate.messageMate.chain.action.actionHandling.ActionHandler;
import com.envimate.messageMate.chain.action.actionHandling.ActionHandlerSet;
import com.envimate.messageMate.channel.Channel;
import com.envimate.messageMate.filtering.Filter;

import java.util.List;
import java.util.function.Consumer;

import static com.envimate.messageMate.chain.ChainProcessingFrame.processingFrame;

final class ChainImpl<T> implements Chain<T> {
    private final Channel<ProcessingContext<T>> preChannel;
    private final Channel<ProcessingContext<T>> processChannel;
    private final Channel<ProcessingContext<T>> postChannel;
    private final Action<T> defaultAction;
    private final ActionHandlerSet<T> actionHandlerSet;

    private ChainImpl(final Channel<ProcessingContext<T>> preChannel, final Channel<ProcessingContext<T>> processChannel,
                      final Channel<ProcessingContext<T>> postChannel, final Action<T> defaultAction,
                      final ActionHandlerSet<T> actionHandlerSet) {
        this.preChannel = preChannel;
        this.processChannel = processChannel;
        this.postChannel = postChannel;
        this.defaultAction = defaultAction;
        this.actionHandlerSet = actionHandlerSet;

        preChannel.subscribe(processChannel::send);
        processChannel.subscribe(postChannel::send);
        postChannel.subscribe(new ConsumerExecutingActionSetByFilterOrDefaultAction());
    }

    static <T> Chain<T> chain(final Action<T> defaultAction,
                              final Channel<ProcessingContext<T>> preChannel,
                              final Channel<ProcessingContext<T>> processChannel,
                              final Channel<ProcessingContext<T>> postChannel,
                              final ActionHandlerSet<T> actionHandlerSet) {
        return new ChainImpl<>(preChannel, processChannel, postChannel, defaultAction, actionHandlerSet);
    }

    @Override
    public void accept(final ProcessingContext<T> processingContext) {
        advanceChainProcessingFrameHistory(processingContext);
        preChannel.send(processingContext);
    }

    private void advanceChainProcessingFrameHistory(final ProcessingContext<T> processingContext) {
        final ChainProcessingFrame<T> previousProcessingFrame = processingContext.getCurrentProcessingFrame();
        final ChainProcessingFrame<T> currentProcessingFrame = processingFrame(this);
        if (noPreviousChainTraversed(previousProcessingFrame)) {
            processingContext.setInitialProcessingFrame(currentProcessingFrame);
        } else {
            previousProcessingFrame.setNextFrame(currentProcessingFrame);
            currentProcessingFrame.setPreviousFrame(previousProcessingFrame);
        }
        processingContext.setCurrentProcessingFrame(currentProcessingFrame);
    }

    private boolean noPreviousChainTraversed(final ChainProcessingFrame<T> previousProcessingFrame) {
        return previousProcessingFrame == null;
    }

    @Override
    public void addPreFilter(final Filter<ProcessingContext<T>> filter) {
        preChannel.add(filter);
    }

    @Override
    public void addPreFilter(final Filter<ProcessingContext<T>> filter, final int position) {
        preChannel.add(filter, position);
    }

    @Override
    public List<Filter<ProcessingContext<T>>> getPreFilter() {
        return preChannel.getFilter();
    }

    @Override
    public void removePreFilter(final Filter<ProcessingContext<T>> filter) {
        preChannel.remove(filter);
    }

    @Override
    public void addProcessFilter(final Filter<ProcessingContext<T>> filter) {
        processChannel.add(filter);
    }

    @Override
    public void addProcessFilter(final Filter<ProcessingContext<T>> filter, final int position) {
        processChannel.add(filter, position);
    }

    @Override
    public List<Filter<ProcessingContext<T>>> getProcessFilter() {
        return processChannel.getFilter();
    }

    @Override
    public void removeProcessFilter(final Filter<ProcessingContext<T>> filter) {
        processChannel.remove(filter);
    }

    @Override
    public void addPostFilter(final Filter<ProcessingContext<T>> filter) {
        postChannel.add(filter);
    }

    @Override
    public void addPostFilter(final Filter<ProcessingContext<T>> filter, final int position) {
        postChannel.add(filter, position);
    }

    @Override
    public List<Filter<ProcessingContext<T>>> getPostFilter() {
        return postChannel.getFilter();
    }

    @Override
    public void removePostFilter(final Filter<ProcessingContext<T>> filter) {
        postChannel.remove(filter);
    }

    @Override
    public Action<T> getDefaultAction() {
        return defaultAction;
    }

    private final class ConsumerExecutingActionSetByFilterOrDefaultAction implements Consumer<ProcessingContext<T>> {

        @Override
        public void accept(final ProcessingContext<T> processingContext) {
            final Action<T> action;
            final ChainProcessingFrame<T> currentProcessingFrame = processingContext.getCurrentProcessingFrame();
            final Action<T> actionSetByFilter = currentProcessingFrame.getAction();
            if (actionSetByFilter != null) {
                action = actionSetByFilter;
            } else {
                action = defaultAction;
                currentProcessingFrame.setAction(defaultAction);
            }
            final ActionHandler<Action<T>, T> actionHandler = actionHandlerSet.getActionHandlerFor(action);
            actionHandler.handle(action, processingContext);
        }
    }
}
