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
import com.envimate.messageMate.messageBus.exception.MessageBusExceptionHandler;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.qcec.shared.TestEnvironmentProperty;
import lombok.RequiredArgsConstructor;

import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.RESULT;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class MessageBusTestExceptionHandler {

    public static MessageBusExceptionHandler allExceptionAsResultHandlingTestExceptionHandler(final TestEnvironment testEnvironment) {
        return allExceptionHandlingTestExceptionHandler(testEnvironment, RESULT);
    }

    public static MessageBusExceptionHandler allExceptionHandlingTestExceptionHandler(final TestEnvironment testEnvironment, final TestEnvironmentProperty exceptionProperty) {
        return new MessageBusExceptionHandler() {
            @Override
            public boolean shouldDeliveryChannelErrorBeHandledAndDeliveryAborted(final ProcessingContext<?> message, final Exception e, final Channel<?> channel) {
                return true;
            }

            @Override
            public void handleDeliveryChannelException(final ProcessingContext<?> message, final Exception e, final Channel<?> channel) {
                testEnvironment.setProperty(exceptionProperty, e);
            }

            @Override
            public void handleFilterException(final ProcessingContext<?> message, final Exception e, final Channel<?> channel) {
                testEnvironment.setProperty(exceptionProperty, e);
            }
        };
    }

    public static MessageBusExceptionHandler allExceptionIgnoringExceptionHandler() {
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
