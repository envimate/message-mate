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
import com.envimate.messageMate.messageBus.exception.MessageBusExceptionHandler;
import com.envimate.messageMate.processingContext.ProcessingContext;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.qcec.shared.TestEnvironmentProperty;
import com.envimate.messageMate.shared.subscriber.TestException;
import lombok.RequiredArgsConstructor;

import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.EXCEPTION;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.RESULT;
import static com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeChannelMessageBusSharedTestProperties.EXCEPTION_OCCURRED_DURING_DELIVERY;
import static com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeChannelMessageBusSharedTestProperties.EXCEPTION_OCCURRED_INSIDE_FILTER;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class MessageBusTestExceptionHandler {
    public final static String TEST_PROPERTY_TO_ENSURE_HANDLER_CALLED_ONCE = "TEST_PROPERTY_TO_ENSURE_HANDLER_CALLED_ONCE";

    public static MessageBusExceptionHandler allExceptionAsResultHandlingTestExceptionHandler(final TestEnvironment testEnvironment) {
        return allExceptionHandlingTestExceptionHandler(testEnvironment, RESULT);
    }

    public static MessageBusExceptionHandler allExceptionHandlingTestExceptionHandler(final TestEnvironment testEnvironment, final TestEnvironmentProperty exceptionProperty) {
        return new MessageBusExceptionHandler() {
            @Override
            public boolean shouldDeliveryChannelErrorBeHandledAndDeliveryAborted(final ProcessingContext<Object> message, final Exception e, final Channel<Object> channel) {
                return true;
            }

            @Override
            public void handleDeliveryChannelException(final ProcessingContext<Object> message, final Exception e, final Channel<Object> channel) {
                testEnvironment.setPropertyIfNotSet(TEST_PROPERTY_TO_ENSURE_HANDLER_CALLED_ONCE, true);
                testEnvironment.setPropertyIfNotSet(exceptionProperty, e);
                testEnvironment.setPropertyIfNotSet(EXCEPTION_OCCURRED_DURING_DELIVERY, true);
            }

            @Override
            public void handleFilterException(final ProcessingContext<Object> message, final Exception e, final Channel<Object> channel) {
                testEnvironment.setPropertyIfNotSet(TEST_PROPERTY_TO_ENSURE_HANDLER_CALLED_ONCE, true);
                testEnvironment.setPropertyIfNotSet(exceptionProperty, e);
                testEnvironment.setPropertyIfNotSet(EXCEPTION_OCCURRED_INSIDE_FILTER, true);
            }
        };
    }

    public static MessageBusExceptionHandler allExceptionIgnoringExceptionHandler() {
        return new MessageBusExceptionHandler() {
            @Override
            public boolean shouldDeliveryChannelErrorBeHandledAndDeliveryAborted(final ProcessingContext<Object> message, final Exception e, final Channel<Object> channel) {
                return true;
            }

            @Override
            public void handleDeliveryChannelException(final ProcessingContext<Object> message, final Exception e, final Channel<Object> channel) {
            }

            @Override
            public void handleFilterException(final ProcessingContext<Object> message, final Exception e, final Channel<Object> channel) {
            }
        };
    }

    public static MessageBusExceptionHandler testExceptionAllowingExceptionHandler(final TestEnvironment testEnvironment) {
        return new MessageBusExceptionHandler() {
            @Override
            public boolean shouldDeliveryChannelErrorBeHandledAndDeliveryAborted(final ProcessingContext<Object> message, final Exception e, final Channel<Object> channel) {
                return !(e instanceof TestException);
            }

            @Override
            public void handleDeliveryChannelException(final ProcessingContext<Object> message, final Exception e, final Channel<Object> channel) {
                testEnvironment.setPropertyIfNotSet(TEST_PROPERTY_TO_ENSURE_HANDLER_CALLED_ONCE, true);
                testEnvironment.setPropertyIfNotSet(EXCEPTION, e);
            }

            @Override
            public void handleFilterException(final ProcessingContext<Object> message, final Exception e, final Channel<Object> channel) {
                testEnvironment.setPropertyIfNotSet(TEST_PROPERTY_TO_ENSURE_HANDLER_CALLED_ONCE, true);
                testEnvironment.setPropertyIfNotSet(EXCEPTION, e);
            }
        };
    }
}
