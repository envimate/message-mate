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

package com.envimate.messageMate.messageBus.givenWhenThen;

import com.envimate.messageMate.channel.Channel;
import com.envimate.messageMate.channel.ProcessingContext;
import com.envimate.messageMate.messageBus.error.MessageBusExceptionHandler;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.shared.subscriber.TestException;
import lombok.RequiredArgsConstructor;

import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.EXCEPTION;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.EXPECTED_RESULT;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.RESULT;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class MessageBusTestExceptionHandler {

    public static MessageBusExceptionHandler allExceptionHandlingTestExceptionHandler(final TestEnvironment testEnvironment) {
        return new MessageBusExceptionHandler() {
            @Override
            public boolean shouldDeliveryChannelErrorBeHandledAndDeliveryAborted(final ProcessingContext<?> message, final Exception e, final Channel<?> channel) {
                return true;
            }

            @Override
            public void handleDeliveryChannelException(final ProcessingContext<?> message, final Exception e, final Channel<?> channel) {
                testEnvironment.setProperty(RESULT, e);
            }

            @Override
            public void handleFilterException(final ProcessingContext<?> message, final Exception e, final Channel<?> channel) {
                testEnvironment.setProperty(RESULT, e);
            }
        };
    }

    public static  MessageBusExceptionHandler testExceptionIgnoringExceptionHandler(final TestEnvironment testEnvironment) {
        return new MessageBusExceptionHandler() {
            @Override
            public boolean shouldDeliveryChannelErrorBeHandledAndDeliveryAborted(final ProcessingContext<?> message, final Exception e, final Channel<?> channel) {
                return !(e instanceof TestException);
            }

            @Override
            public void handleDeliveryChannelException(final ProcessingContext<?> message, final Exception e, final Channel<?> channel) {
                testEnvironment.setProperty(EXCEPTION, e);
            }

            @Override
            public void handleFilterException(final ProcessingContext<?> message, final Exception e, final Channel<?> channel) {
                testEnvironment.setProperty(EXCEPTION, e);
            }
        };
    }

    public static  MessageBusExceptionHandler allExceptionIgnoringExceptionHandler() {
        return new MessageBusExceptionHandler() {
            @Override
            public boolean shouldDeliveryChannelErrorBeHandledAndDeliveryAborted(final ProcessingContext<?> message, final Exception e, final Channel<?> channel) {
                return true;
            }

            @Override
            public void handleDeliveryChannelException(final ProcessingContext<?> message, final Exception e, final Channel<?> channel) {
            }

            @Override
            public void handleFilterException(final ProcessingContext<?> message, final Exception e, final Channel<?> channel) {
            }
        };
    }
}
