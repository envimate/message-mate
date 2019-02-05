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
import lombok.Getter;
import lombok.Setter;

public final class ChainProcessingFrame<T> {
    @Getter
    private final Chain<T> chain;

    @Getter
    @Setter
    private ChainProcessingFrame<T> previousFrame;

    @Getter
    @Setter
    private ChainProcessingFrame<T> nextFrame;

    @Getter
    @Setter
    private Action<T> action;

    private ChainProcessingFrame(final Chain<T> chain) {
        this.chain = chain;
    }

    private ChainProcessingFrame(final Chain<T> chain, final ChainProcessingFrame<T> previousFrame,
                                 final ChainProcessingFrame<T> nextFrame, final Action<T> action) {
        this.chain = chain;
        this.previousFrame = previousFrame;
        this.nextFrame = nextFrame;
        this.action = action;
    }

    public static <T> ChainProcessingFrame<T> processingFrame(final Chain<T> chain) {
        return new ChainProcessingFrame<>(chain);
    }

    public ChainProcessingFrame<T> copy() {
        return new ChainProcessingFrame<>(chain, previousFrame, nextFrame, action);
    }
}
