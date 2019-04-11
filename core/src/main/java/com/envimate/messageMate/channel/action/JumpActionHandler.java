/*
 * Copyright (c) 2019 envimate GmbH - https://envimate.com/.
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

package com.envimate.messageMate.channel.action;

import com.envimate.messageMate.channel.Channel;
import com.envimate.messageMate.channel.ChannelProcessingFrame;
import com.envimate.messageMate.processingContext.ProcessingContext;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

/**
 * The {@code ActionHandler} implementation for the {@code Jump} {@code Action}. This handler will take the message and sends
 * it on the given target the type of messages of the {@code Channel}.
 *
 * @param <T> the type of messages of the {@code Channel}
 * @see <a href="https://github.com/envimate/message-mate#jump">Message Mate Documentation</a>
 */
@RequiredArgsConstructor(access = PRIVATE)
public final class JumpActionHandler<T> implements ActionHandler<Jump<T>, T> {

    /**
     * Factory method for a new {@code JumpActionHandler}.
     *
     * @param <T> the type of messages of the {@code Channel}
     * @return a new {@code JumpActionHandler}
     */
    public static <T> JumpActionHandler<T> jumpActionHandler() {
        return new JumpActionHandler<>();
    }

    /**
     * Takes the message and sends in on the given {@code Channel}.
     *
     * @param jump              the {@code Jump} {@code Action}
     * @param processingContext the message
     */
    @Override
    public void handle(final Jump<T> jump, final ProcessingContext<T> processingContext) {
        final Channel<T> targetChannel = jump.getTargetChannel();
        final ChannelProcessingFrame<T> finishedProcessingFrame = processingContext.getCurrentProcessingFrame();
        finishedProcessingFrame.setAction(jump);
        targetChannel.send(processingContext);
    }
}

