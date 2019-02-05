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

package com.envimate.messageMate.chain.action.actionHandling;

import com.envimate.messageMate.chain.Chain;
import com.envimate.messageMate.chain.ChainProcessingFrame;
import com.envimate.messageMate.chain.ProcessingContext;
import com.envimate.messageMate.chain.action.Jump;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class JumpActionHandler<T> implements ActionHandler<Jump<T>, T> {

    public static <T> JumpActionHandler<T> jumpActionHandler() {
        return new JumpActionHandler<>();
    }

    @Override
    public void handle(final Jump<T> jump, final ProcessingContext<T> processingContext) {
        final Chain<T> targetChain = jump.getTargetChain();
        final ChainProcessingFrame<T> finishedProcessingFrame = processingContext.getCurrentProcessingFrame();
        finishedProcessingFrame.setAction(jump);
        targetChain.accept(processingContext);
    }
}

