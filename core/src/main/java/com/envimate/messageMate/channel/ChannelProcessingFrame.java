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
import lombok.Getter;
import lombok.Setter;

/**
 * The {@code ProcessingContext} object stores the history of the traversed {@code Channels} in form of a linked list of
 * {@code ChannelProcessingFrames}. For each {@code Channel} one {@code ChannelProcessingFrame} is added to the end of the list.
 *
 * <p>Each {@code ProcessingContext} stores the corresponding {@code Channel}, the {@code Action} that was executed as well as
 * its preceding and succeeding frame. In case of the first frame in the list, the {@code getPreviousFrame()} returns null.
 * Respective for the last frame the {@code getNextFrame()} returns null.</p>
 *
 * @param <T>
 */
public final class ChannelProcessingFrame<T> {
    @Getter
    private final Channel<T> channel;

    @Getter
    @Setter
    private ChannelProcessingFrame<T> previousFrame;

    @Getter
    @Setter
    private ChannelProcessingFrame<T> nextFrame;

    @Getter
    @Setter
    private Action<T> action;

    private ChannelProcessingFrame(final Channel<T> channel) {
        this.channel = channel;
    }

    private ChannelProcessingFrame(final Channel<T> channel, final ChannelProcessingFrame<T> previousFrame,
                                   final ChannelProcessingFrame<T> nextFrame, final Action<T> action) {
        this.channel = channel;
        this.previousFrame = previousFrame;
        this.nextFrame = nextFrame;
        this.action = action;
    }

    /**
     * Factory method to create a new {@code ChannelProcessingFrame} associated with the given {@code Channel}.
     *
     * @param channel the {@code Channel} this frame relates to
     * @param <T>     the type of the {@code Channel}
     * @return a new {@code ChannelProcessingFrame}
     */
    public static <T> ChannelProcessingFrame<T> processingFrame(final Channel<T> channel) {
        return new ChannelProcessingFrame<>(channel);
    }

    /**
     * Creates a exact shallow copy of the current {@code ChannelProcessingFrame}.
     *
     * @return a shallow copy of the current {@code ChannelProcessingFrame}
     */
    public ChannelProcessingFrame<T> copy() {
        return new ChannelProcessingFrame<>(channel, previousFrame, nextFrame, action);
    }
}
