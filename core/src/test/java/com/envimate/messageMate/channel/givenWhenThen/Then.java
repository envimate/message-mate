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

package com.envimate.messageMate.channel.givenWhenThen;

import com.envimate.messageMate.channel.Channel;
import com.envimate.messageMate.qcec.shared.TestAction;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.qcec.shared.TestValidation;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.Semaphore;

import static com.envimate.messageMate.channel.givenWhenThen.ChannelTestProperties.*;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.EXCEPTION;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.SUT;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
public class Then {
    private final ChannelSetupBuilder channelSetupBuilder;
    private final ChannelActionBuilder channelActionBuilder;

    public void then(final ChannelValidationBuilder channelValidationBuilder) {
        final TestEnvironment testEnvironment = channelSetupBuilder.build();
        final List<TestAction<Channel<TestMessage>>> testActions = channelActionBuilder.build();
        @SuppressWarnings("unchecked")
        final Channel<TestMessage> channel = (Channel<TestMessage>) testEnvironment.getProperty(SUT);
        try {
            for (final TestAction<Channel<TestMessage>> testAction : testActions) {
                testAction.execute(channel, testEnvironment);
                if (testEnvironment.has(SLEEP_BETWEEN_EXECUTION_STEPS)) {
                    final Long sleepDuration = testEnvironment.getPropertyAsType(SLEEP_BETWEEN_EXECUTION_STEPS, Long.class);
                    MILLISECONDS.sleep(sleepDuration);
                }
            }
            if (testEnvironment.has(SLEEP_AFTER_EXECUTION)) {
                final Long sleepDuration = testEnvironment.getPropertyAsType(SLEEP_AFTER_EXECUTION, Long.class);
                MILLISECONDS.sleep(sleepDuration);
            }
        } catch (final Exception e) {
            testEnvironment.setPropertyIfNotSet(EXCEPTION, e);
        }
        final TestValidation testValidation = channelValidationBuilder.build();
        testValidation.validate(testEnvironment);
        if (testEnvironment.has(SEMAPHORE_TO_CLEAN_UP)) {
            final Semaphore semaphore = testEnvironment.getPropertyAsType(SEMAPHORE_TO_CLEAN_UP, Semaphore.class);
            semaphore.release(100);
        }
        if (testEnvironment.has(SLEEP_BEFORE_CLOSE)) {
            final Long sleepDuration = testEnvironment.getPropertyAsType(SLEEP_BEFORE_CLOSE, Long.class);
            try {
                MILLISECONDS.sleep(sleepDuration);
            } catch (final InterruptedException e) {
                testEnvironment.setPropertyIfNotSet(EXCEPTION, e);
            }
        }
        channel.close(false);
    }
}
