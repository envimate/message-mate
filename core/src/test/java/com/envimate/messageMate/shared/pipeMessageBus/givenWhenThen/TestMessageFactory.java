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

package com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen;

import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.shared.testMessages.InvalidTestMessage;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import com.envimate.messageMate.shared.testMessages.TestMessageOfInterest;
import lombok.RequiredArgsConstructor;

import java.util.function.Supplier;

import static com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeChannelMessageBusSharedTestProperties.MESSAGES_SEND_OF_INTEREST;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class TestMessageFactory implements MessageFactory {
    private final Supplier<TestMessage> messageSupplier;
    private final int numberOfMessages;

    public static TestMessageFactory testMessageFactoryForValidMessages(final int numberOfMessages,
                                                                        final TestEnvironment testEnvironment) {
        return new TestMessageFactory(() -> {
            final TestMessageOfInterest message = TestMessageOfInterest.messageOfInterest();
            testEnvironment.addToListProperty(MESSAGES_SEND_OF_INTEREST, message);
            return message;
        }, numberOfMessages);
    }

    public static TestMessageFactory testMessageFactoryForInvalidMessages(final int numberOfMessages) {
        return new TestMessageFactory(InvalidTestMessage::invalidTestMessage, numberOfMessages);
    }

    public static TestMessageFactory testMessageFactoryForRandomValidOrInvalidTestMessages(
            final int numberOfMessages,
            final TestEnvironment testEnvironment) {
        return new TestMessageFactory(() -> {
            if (Math.random() < 0.5) {
                final TestMessageOfInterest message = TestMessageOfInterest.messageOfInterest();
                testEnvironment.addToListProperty(MESSAGES_SEND_OF_INTEREST, message);
                return message;
            } else {
                return InvalidTestMessage.invalidTestMessage();
            }
        }, numberOfMessages);
    }

    @Override
    public TestMessage createMessage() {
        return messageSupplier.get();
    }

    @Override
    public int numberOfMessages() {
        return numberOfMessages;
    }
}
