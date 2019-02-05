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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

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
    private ChainProcessingFrame<T> initialProcessingFrame;

    @Setter
    @Getter
    private ChainProcessingFrame<T> currentProcessingFrame;

    private ProcessingContext(final Map<Object, Object> contextMetaData, final T payload,
                              final ChainProcessingFrame<T> initialProcessingFrame,
                              final ChainProcessingFrame<T> currentProcessingFrame) {
        this.contextMetaData = contextMetaData;
        this.payload = payload;
        this.initialProcessingFrame = initialProcessingFrame;
        this.currentProcessingFrame = currentProcessingFrame;
    }

    public static <T> ProcessingContext<T> processingContext(final T payload) {
        final Map<Object, Object> contextMetaData = new HashMap<>();
        return new ProcessingContext<>(contextMetaData, payload, null, null);
    }

    public static <T> ProcessingContext<T> processingContext(final T payload, final Map<Object, Object> contextMetaData) {
        return new ProcessingContext<>(contextMetaData, payload, null, null);
    }
}
