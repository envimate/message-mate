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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

/**
 * Message specific root object for all information related to the processing of a message.
 *
 * <p>Each {@code ProcessingContext} envelopes the sent message. The message can be accesses with the {@code getPayload()} and
 * {@code setPayload()} methods. All {@code Actions} and {@code Filter} get access to the {@code ProcessingContext} object.
 * This allows them to share data using the {@code ProcessingContext's} context meta date object. It is a
 * {@code Map<Object, Object}, that can be accessed with {@code getContextMetaData()}.</p>
 *
 * <p>In case several {@code Channel} are chained together, a message traverses different channel in a specific order. The
 * transitions are handled via {@code Actions}. Given such a chained {@code Channel} scenario, the history can be of interest.
 * The history is represented in form of a linked list of {@code ChannelProcessingFrames}. For each traversed {@code Channel},
 * a new {@code ChannelProcessingFrame} is added at the end of the list. Once the final {@code Action} is reached, it is also
 * saved in the frame. The {@code ProcessingContext} object gives access to the initial {@code ChannelProcessingFrame} with
 * {@code getInitialProcessingFrame()}. The frame of the current {@code Channel} ( or last if outside of one), can be accessed
 * with {@code getCurrentProcessingFrame()}. An exception is the {@code Call} {@code Action}. This {@code Action} is always
 * executed once it was created and will never be the final action of a {@code Channel}. In case a {@code Call} is executed,
 * an extra {@code ChannelProcessingFrame} is added with the {@code Call} {@code Action}, to represent the branching of the flow.
 * All subsequent {@code Channel} will be contained normally in the list of frames.</p>
 *
 * @param <T> the type of the processing {@code Channel}
 */
@ToString
@EqualsAndHashCode
public final class ProcessingContext<T> {
    @Getter
    private final Map<Object, Object> contextMetaData;

    @Getter
    @Setter
    private T payload;

    @Getter
    @Setter
    private ChannelProcessingFrame<T> initialProcessingFrame;

    @Setter
    @Getter
    private ChannelProcessingFrame<T> currentProcessingFrame;

    private ProcessingContext(final Map<Object, Object> contextMetaData, final T payload,
                              final ChannelProcessingFrame<T> initialProcessingFrame,
                              final ChannelProcessingFrame<T> currentProcessingFrame) {
        this.contextMetaData = contextMetaData;
        this.payload = payload;
        this.initialProcessingFrame = initialProcessingFrame;
        this.currentProcessingFrame = currentProcessingFrame;
    }

    /**
     * Factory method to create a new {@code ProcessingContext} for a given payload.
     *
     * @param payload the message to envelope
     * @param <T> the type of the message
     * @return a new {@code ProcessingContext} object
     */
    public static <T> ProcessingContext<T> processingContext(final T payload) {
        final Map<Object, Object> contextMetaData = new HashMap<>();
        return new ProcessingContext<>(contextMetaData, payload, null, null);
    }

    /**
     * Factory method to create a new {@code ProcessingContext} for a given payload and a filled meta data map.
     *
     * @param payload the message to envelope
     * @param contextMetaData the map to store shared meta data into
     * @param <T> the type of the message
     * @return a new {@code ProcessingContext} object
     */
    public static <T> ProcessingContext<T> processingContext(final T payload, final Map<Object, Object> contextMetaData) {
        return new ProcessingContext<>(contextMetaData, payload, null, null);
    }

    /**
     * Returns, whether the default {@code Action} was overwritten for the current {@code Channel}
     *
     * @return {@code true} if the {@code Action} was changed and {@code false} otherwise
     */
    public boolean actionWasChanged() {
        return currentProcessingFrame.getAction() != null;
    }

    /**
     * Returns the {@code Action}, that overwrites the default one, if existing.
     *
     * @return the changed {@code Action} or null of no new {@code Action} was set
     */
    public Action<T> getAction() {
        return currentProcessingFrame.getAction();
    }

    /**
     * Overwrites the {@code Channel's} default {@code Action}.
     *
     * <p>At the end of the current {@code Channel} not the default {@code Action} is executed anymore. Instead the overwriting
     * {@code Action} is executed.</p>
     *
     * @param action the new {@code Action}
     */
    public void changeAction(final Action<T> action) {
        currentProcessingFrame.setAction(action);
    }
}