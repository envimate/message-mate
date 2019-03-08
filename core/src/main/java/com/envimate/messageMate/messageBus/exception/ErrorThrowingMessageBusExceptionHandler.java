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

package com.envimate.messageMate.messageBus.exception;

import com.envimate.messageMate.channel.Channel;
import com.envimate.messageMate.channel.ProcessingContext;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

/**
 * The default {@code MessageBusExceptionHandler} implementation, that rethrows all exceptions.
 */
@RequiredArgsConstructor(access = PRIVATE)
public final class ErrorThrowingMessageBusExceptionHandler implements MessageBusExceptionHandler {

    /**
     * Factory method to create a new {@code ErrorThrowingMessageBusExceptionHandler}.
     *
     * @return the new {@code ErrorThrowingMessageBusExceptionHandler}.
     */
    public static ErrorThrowingMessageBusExceptionHandler errorThrowingMessageBusExceptionHandler() {
        return new ErrorThrowingMessageBusExceptionHandler();
    }

    @Override
    public boolean shouldDeliveryChannelErrorBeHandledAndDeliveryAborted(final ProcessingContext<?> message, final Exception e,
                                                                         final Channel<?> channel) {
        return true;
    }

    @Override
    public void handleDeliveryChannelException(final ProcessingContext<?> message, final Exception e, final Channel<?> channel) {
        if (e instanceof RuntimeException) {
            throw (RuntimeException) e;
        } else {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void handleFilterException(final ProcessingContext<?> message, final Exception e, final Channel<?> channel) {
        if (e instanceof RuntimeException) {
            throw (RuntimeException) e;
        } else {
            throw new RuntimeException(e);
        }
    }
}
