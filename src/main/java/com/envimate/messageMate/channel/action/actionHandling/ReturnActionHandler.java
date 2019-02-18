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

package com.envimate.messageMate.channel.action.actionHandling;

import com.envimate.messageMate.channel.ChannelProcessingFrame;
import com.envimate.messageMate.channel.ProcessingContext;
import com.envimate.messageMate.channel.action.Action;
import com.envimate.messageMate.channel.action.Call;
import com.envimate.messageMate.channel.action.Return;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class ReturnActionHandler<T> implements ActionHandler<Return<T>, T> {

    public static <T> ReturnActionHandler<T> returnActionHandler() {
        return new ReturnActionHandler<>();
    }

    @Override
    public void handle(final Return<T> returnAction, final ProcessingContext<T> processingContext) {
        final ChannelProcessingFrame<T> currentProcessingFrame = processingContext.getCurrentProcessingFrame();
        final ChannelProcessingFrame<T> callProcessingFrame = locateLastNotYetReturnedCallProcessingFrame(currentProcessingFrame);

        final Call<T> callAction = (Call<T>) callProcessingFrame.getAction();
        callAction.setReturnFrame(currentProcessingFrame);
        returnAction.setRelatedCallFrame(callProcessingFrame);

        final ChannelProcessingFrame<T> nextProcessingFrame = callAction.getProcessingFrameToContinueAfterReturn();
        currentProcessingFrame.setNextFrame(nextProcessingFrame);
        nextProcessingFrame.setPreviousFrame(currentProcessingFrame);

        processingContext.setCurrentProcessingFrame(nextProcessingFrame);
    }

    private ChannelProcessingFrame<T> locateLastNotYetReturnedCallProcessingFrame(final ChannelProcessingFrame<T> latestFrame) {
        ChannelProcessingFrame<T> currentProcessingFrame = latestFrame;
        while (currentProcessingFrame != null) {
            final Action<T> action = currentProcessingFrame.getAction();
            if (action instanceof Call) {
                final Call<T> callAction = (Call<T>) action;
                final ChannelProcessingFrame<T> returnFrame = callAction.getReturnFrame();
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
