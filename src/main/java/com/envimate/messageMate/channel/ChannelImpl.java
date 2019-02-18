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
import com.envimate.messageMate.channel.action.actionHandling.ActionHandler;
import com.envimate.messageMate.channel.action.actionHandling.ActionHandlerSet;
import com.envimate.messageMate.pipe.Pipe;
import com.envimate.messageMate.filtering.Filter;

import java.util.List;
import java.util.function.Consumer;

import static com.envimate.messageMate.channel.ChannelProcessingFrame.processingFrame;

final class ChannelImpl<T> implements Channel<T> {
    private final Pipe<ProcessingContext<T>> prePipe;
    private final Pipe<ProcessingContext<T>> processPipe;
    private final Pipe<ProcessingContext<T>> postPipe;
    private final Action<T> defaultAction;
    private final ActionHandlerSet<T> actionHandlerSet;

    private ChannelImpl(final Pipe<ProcessingContext<T>> prePipe, final Pipe<ProcessingContext<T>> processPipe,
                        final Pipe<ProcessingContext<T>> postPipe, final Action<T> defaultAction,
                        final ActionHandlerSet<T> actionHandlerSet) {
        this.prePipe = prePipe;
        this.processPipe = processPipe;
        this.postPipe = postPipe;
        this.defaultAction = defaultAction;
        this.actionHandlerSet = actionHandlerSet;

        prePipe.subscribe(processPipe::send);
        processPipe.subscribe(postPipe::send);
        postPipe.subscribe(new ConsumerExecutingActionSetByFilterOrDefaultAction());
    }

    static <T> Channel<T> channel(final Action<T> defaultAction,
                                  final Pipe<ProcessingContext<T>> prePipe,
                                  final Pipe<ProcessingContext<T>> processPipe,
                                  final Pipe<ProcessingContext<T>> postPipe,
                                  final ActionHandlerSet<T> actionHandlerSet) {
        return new ChannelImpl<>(prePipe, processPipe, postPipe, defaultAction, actionHandlerSet);
    }

    @Override
    public void accept(final ProcessingContext<T> processingContext) {
        advanceChannelProcessingFrameHistory(processingContext);
        prePipe.send(processingContext);
    }

    private void advanceChannelProcessingFrameHistory(final ProcessingContext<T> processingContext) {
        final ChannelProcessingFrame<T> previousProcessingFrame = processingContext.getCurrentProcessingFrame();
        final ChannelProcessingFrame<T> currentProcessingFrame = processingFrame(this);
        if (noPreviousChannelTraversed(previousProcessingFrame)) {
            processingContext.setInitialProcessingFrame(currentProcessingFrame);
        } else {
            previousProcessingFrame.setNextFrame(currentProcessingFrame);
            currentProcessingFrame.setPreviousFrame(previousProcessingFrame);
        }
        processingContext.setCurrentProcessingFrame(currentProcessingFrame);
    }

    private boolean noPreviousChannelTraversed(final ChannelProcessingFrame<T> previousProcessingFrame) {
        return previousProcessingFrame == null;
    }

    @Override
    public void addPreFilter(final Filter<ProcessingContext<T>> filter) {
        prePipe.add(filter);
    }

    @Override
    public void addPreFilter(final Filter<ProcessingContext<T>> filter, final int position) {
        prePipe.add(filter, position);
    }

    @Override
    public List<Filter<ProcessingContext<T>>> getPreFilter() {
        return prePipe.getFilter();
    }

    @Override
    public void removePreFilter(final Filter<ProcessingContext<T>> filter) {
        prePipe.remove(filter);
    }

    @Override
    public void addProcessFilter(final Filter<ProcessingContext<T>> filter) {
        processPipe.add(filter);
    }

    @Override
    public void addProcessFilter(final Filter<ProcessingContext<T>> filter, final int position) {
        processPipe.add(filter, position);
    }

    @Override
    public List<Filter<ProcessingContext<T>>> getProcessFilter() {
        return processPipe.getFilter();
    }

    @Override
    public void removeProcessFilter(final Filter<ProcessingContext<T>> filter) {
        processPipe.remove(filter);
    }

    @Override
    public void addPostFilter(final Filter<ProcessingContext<T>> filter) {
        postPipe.add(filter);
    }

    @Override
    public void addPostFilter(final Filter<ProcessingContext<T>> filter, final int position) {
        postPipe.add(filter, position);
    }

    @Override
    public List<Filter<ProcessingContext<T>>> getPostFilter() {
        return postPipe.getFilter();
    }

    @Override
    public void removePostFilter(final Filter<ProcessingContext<T>> filter) {
        postPipe.remove(filter);
    }

    @Override
    public Action<T> getDefaultAction() {
        return defaultAction;
    }

    private final class ConsumerExecutingActionSetByFilterOrDefaultAction implements Consumer<ProcessingContext<T>> {

        @Override
        public void accept(final ProcessingContext<T> processingContext) {
            final Action<T> action;
            final ChannelProcessingFrame<T> currentProcessingFrame = processingContext.getCurrentProcessingFrame();
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
