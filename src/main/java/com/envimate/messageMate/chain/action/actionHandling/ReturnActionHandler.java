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

import com.envimate.messageMate.chain.ChainProcessingFrame;
import com.envimate.messageMate.chain.ProcessingContext;
import com.envimate.messageMate.chain.action.Action;
import com.envimate.messageMate.chain.action.Call;
import com.envimate.messageMate.chain.action.Return;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class ReturnActionHandler<T> implements ActionHandler<Return<T>, T> {

    public static <T> ReturnActionHandler<T> returnActionHandler() {
        return new ReturnActionHandler<>();
    }

    @Override
    public void handle(final Return<T> returnAction, final ProcessingContext<T> processingContext) {
        final ChainProcessingFrame<T> currentProcessingFrame = processingContext.getCurrentProcessingFrame();
        final ChainProcessingFrame<T> callProcessingFrame = locateLastNotYetReturnedCallProcessingFrame(currentProcessingFrame);

        final Call<T> callAction = (Call<T>) callProcessingFrame.getAction();
        callAction.setReturnFrame(currentProcessingFrame);
        returnAction.setRelatedCallFrame(callProcessingFrame);

        final ChainProcessingFrame<T> nextProcessingFrame = callAction.getProcessingFrameToContinueAfterReturn();
        currentProcessingFrame.setNextFrame(nextProcessingFrame);
        nextProcessingFrame.setPreviousFrame(currentProcessingFrame);

        processingContext.setCurrentProcessingFrame(nextProcessingFrame);
    }

    private ChainProcessingFrame<T> locateLastNotYetReturnedCallProcessingFrame(final ChainProcessingFrame<T> latestFrame) {
        ChainProcessingFrame<T> currentProcessingFrame = latestFrame;
        while (currentProcessingFrame != null) {
            final Action<T> action = currentProcessingFrame.getAction();
            if (action instanceof Call) {
                final Call<T> callAction = (Call<T>) action;
                final ChainProcessingFrame<T> returnFrame = callAction.getReturnFrame();
                if (returnFrame == null) {
                    return currentProcessingFrame;
                } else {
                    currentProcessingFrame = currentProcessingFrame.getPreviousFrame();
                }
            } else {
                currentProcessingFrame = currentProcessingFrame.getPreviousFrame();
            }
        }
        throw new ReturnWithoutCallException();
    }
}
