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

package com.envimate.messageMate.processingContext;

import com.envimate.messageMate.channel.ChannelProcessingFrame;
import com.envimate.messageMate.channel.action.Action;
import com.envimate.messageMate.identification.CorrelationId;
import com.envimate.messageMate.identification.MessageId;
import com.envimate.messageMate.messageBus.EventType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

import static com.envimate.messageMate.identification.CorrelationId.correlationIdFor;
import static com.envimate.messageMate.identification.MessageId.newUniqueMessageId;
import static com.envimate.messageMate.internal.enforcing.NotNullEnforcer.ensureNotNull;

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
 * @see <a href="https://github.com/envimate/message-mate#processing-context">Message Mate Documentation</a>
 */

//TODO: errorPayload: test for MB + for Channel
@ToString
@EqualsAndHashCode
public final class ProcessingContext<T> {
    @Getter
    private final EventType eventType;
    @Getter
    private final MessageId messageId;
    @Getter
    private final Map<Object, Object> contextMetaData;
    @Getter
    @Setter
    private CorrelationId correlationId;
    @Getter
    @Setter
    private T payload;
    @Getter
    @Setter
    private Object errorPayload;

    @Getter
    @Setter
    private ChannelProcessingFrame<T> initialProcessingFrame;

    @Getter
    @Setter
    private ChannelProcessingFrame<T> currentProcessingFrame;


    private ProcessingContext(final EventType eventType,
                              final MessageId messageId,
                              final CorrelationId correlationId,
                              final T payload,
                              final Object errorPayload,
                              final Map<Object, Object> contextMetaData,
                              final ChannelProcessingFrame<T> initialProcessingFrame,
                              final ChannelProcessingFrame<T> currentProcessingFrame) {
        this.eventType = eventType;
        ensureNotNull(messageId, "messageId");
        this.messageId = messageId;
        this.correlationId = correlationId;
        this.contextMetaData = contextMetaData;
        ensureNotNull(contextMetaData, "contextMetaData");
        this.payload = payload;
        this.errorPayload = errorPayload;
        this.initialProcessingFrame = initialProcessingFrame;
        this.currentProcessingFrame = currentProcessingFrame;
    }


    /**
     * Factory method to create a new {@code ProcessingContext} for a given payload.
     *
     * @param payload the message to envelope
     * @param <T>     the type of the message
     * @return a new {@code ProcessingContext} object
     */
    public static <T> ProcessingContext<T> processingContext(final T payload) {
        final Map<Object, Object> contextMetaData = new HashMap<>();
        final MessageId messageId = newUniqueMessageId();
        return new ProcessingContext<>(null, messageId, null, payload, null, contextMetaData, null, null);
    }

    public static <T> ProcessingContext<T> processingContext(final EventType eventType, final T payload) {
        final Map<Object, Object> contextMetaData = new HashMap<>();
        final MessageId messageId = newUniqueMessageId();
        return new ProcessingContext<>(eventType, messageId, null, payload, null, contextMetaData, null, null);
    }

    public static <T> ProcessingContext<T> processingContextForError(final EventType eventType, final Object errorPayload) {
        final Map<Object, Object> contextMetaData = new HashMap<>();
        final MessageId messageId = newUniqueMessageId();
        return new ProcessingContext<>(eventType, messageId, null, null, errorPayload, contextMetaData, null, null);
    }

    public static <T> ProcessingContext<T> processingContext(final EventType eventType, final T payload, final MessageId messageId) {
        final Map<Object, Object> contextMetaData = new HashMap<>();
        return new ProcessingContext<>(eventType, messageId, null, payload, null, contextMetaData, null, null);
    }

    public static <T> ProcessingContext<T> processingContextForError(final EventType eventType, final Object errorPayload, final MessageId messageId) {
        final Map<Object, Object> contextMetaData = new HashMap<>();
        return new ProcessingContext<>(eventType, messageId, null, null, errorPayload, contextMetaData, null, null);
    }

    /**
     * Factory method to create a new {@code ProcessingContext} for a given payload and {@code CorrelationId}.
     *
     * @param payload       the message to envelope
     * @param correlationId the {@code CorrelationId} to be used
     * @param <T>           the type of the message
     * @return a new {@code ProcessingContext} object
     */
    public static <T> ProcessingContext<T> processingContext(final T payload, final CorrelationId correlationId) {
        final Map<Object, Object> metaData = new HashMap<>();
        final MessageId messageId = newUniqueMessageId();
        return new ProcessingContext<>(null, messageId, correlationId, payload, null, metaData, null, null);
    }

    public static <T> ProcessingContext<T> processingContextForError(final Object errorPayload, final CorrelationId correlationId) {
        final Map<Object, Object> metaData = new HashMap<>();
        final MessageId messageId = newUniqueMessageId();
        return new ProcessingContext<>(null, messageId, correlationId, null, errorPayload, metaData, null, null);
    }

    public static <T> ProcessingContext<T> processingContextForPayloadAndError(final EventType eventType, final T payload, final Object errorPayload) {
        final Map<Object, Object> contextMetaData = new HashMap<>();
        final MessageId messageId = newUniqueMessageId();
        return new ProcessingContext<>(eventType, messageId, null, payload, errorPayload, contextMetaData, null, null);
    }

    public static <T> ProcessingContext<T> processingContextForPayloadAndError(final EventType eventType, final CorrelationId correlationId, final T payload, final Object errorPayload) {
        final Map<Object, Object> contextMetaData = new HashMap<>();
        final MessageId messageId = newUniqueMessageId();
        return new ProcessingContext<>(eventType, messageId, correlationId, payload, errorPayload, contextMetaData, null, null);
    }

    /**
     * Factory method to create a new {@code ProcessingContext} for a given payload and a {@code MessageId}, {@code CorrelationId}
     * combination.
     *
     * @param payload       the message to envelope
     * @param messageId     the {@code MessageId} of the message
     * @param correlationId the {@code CorrelationId} to be used
     * @param <T>           the type of the message
     * @return a new {@code ProcessingContext} object
     */
    //TODO: clean up
    public static <T> ProcessingContext<T> processingContext(final T payload, final MessageId messageId,
                                                             final CorrelationId correlationId) {
        final Map<Object, Object> metaData = new HashMap<>();
        return new ProcessingContext<>(null, messageId, correlationId, payload, null, metaData, null, null);
    }

    public static <T> ProcessingContext<T> processingContext(final EventType eventType, final T payload,
                                                             final CorrelationId correlationId) {
        final Map<Object, Object> metaData = new HashMap<>();
        final MessageId messageId = MessageId.newUniqueMessageId();
        return new ProcessingContext<>(eventType, messageId, correlationId, payload, null, metaData, null, null);
    }

    /**
     * Factory method to create a new {@code ProcessingContext} for a given payload and a filled meta data map.
     *
     * @param payload         the message to envelope
     * @param contextMetaData the map to store shared meta data into
     * @param <T>             the type of the message
     * @return a new {@code ProcessingContext} object
     */
    public static <T> ProcessingContext<T> processingContext(final T payload, final Map<Object, Object> contextMetaData) {
        final MessageId messageId = newUniqueMessageId();
        return new ProcessingContext<>(null, messageId, null, payload, null, contextMetaData, null, null);
    }

    public static <T> ProcessingContext<T> processingContext(final EventType eventType, final MessageId messageId,
                                                             final CorrelationId correlationId,
                                                             final T payload,
                                                             final Object errorPayload,
                                                             final Map<Object, Object> contextMetaData,
                                                             final ChannelProcessingFrame<T> initialProcessingFrame,
                                                             final ChannelProcessingFrame<T> currentProcessingFrame) {
        return new ProcessingContext<>(eventType, messageId, correlationId, payload, errorPayload, contextMetaData, initialProcessingFrame, currentProcessingFrame);
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

    /**
     * Creates a {@code CorrelationId} matching the current {@code ProcessingContext's}{@code MessageId}.
     *
     * @return a new, related {@code CorrelationId}
     */
    public CorrelationId generateCorrelationIdForAnswer() {
        return correlationIdFor(messageId);
    }
}
