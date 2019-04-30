/*
 * Copyright (c) 2019 envimate GmbH - https://envimate.com/.
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

import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.shared.givenWhenThen.TestAction;
import com.envimate.messageMate.shared.environment.TestEnvironment;
import com.envimate.messageMate.shared.givenWhenThen.TestValidation;
import com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.SetupAction;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.Semaphore;

import static com.envimate.messageMate.shared.environment.TestEnvironmentProperty.EXCEPTION;
import static com.envimate.messageMate.shared.environment.TestEnvironmentProperty.SUT;
import static com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeChannelMessageBusSharedTestProperties.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class Then {
    private final MessageBusSetupBuilder setupBuilder;
    private final MessageBusActionBuilder actionBuilder;

    public void then(final MessageBusValidationBuilder testValidationBuilder) {
        final MessageBusSetup setup = buildSetup(setupBuilder);

        final TestEnvironment testEnvironment = setup.getTestEnvironment();
        final MessageBus messageBus = setup.getMessageBus();
        executeTestAction(actionBuilder, messageBus, testEnvironment);

        final TestValidation validation = testValidationBuilder.build();
        validation.validate(testEnvironment);
        try {
            closeSut(messageBus);
        } catch (final InterruptedException e) {
            testEnvironment.setPropertyIfNotSet(EXCEPTION, e);
        }
    }

    private MessageBusSetup buildSetup(final MessageBusSetupBuilder setupBuilder) {
        final MessageBusSetup setup = setupBuilder.build();
        final TestEnvironment testEnvironment = setup.getTestEnvironment();
        final MessageBus messageBus = setup.getMessageBus();
        testEnvironment.setProperty(SUT, messageBus);
        final List<SetupAction<MessageBus>> setupActions = setup.getSetupActions();
        try {
            for (final SetupAction<MessageBus> setupAction : setupActions) {
                setupAction.execute(messageBus, testEnvironment);
            }
        } catch (final Exception e) {
            testEnvironment.setPropertyIfNotSet(EXCEPTION, e);
        }
        return setup;
    }

    private void executeTestAction(final MessageBusActionBuilder actionBuilder,
                                   final MessageBus messageBus,
                                   final TestEnvironment testEnvironment) {
        final List<TestAction<MessageBus>> actions = actionBuilder.build();
        try {
            for (final TestAction<MessageBus> testAction : actions) {
                testAction.execute(messageBus, testEnvironment);
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
        if (testEnvironment.has(EXECUTION_END_SEMAPHORE)) {
            final Semaphore blockingSemaphoreToReleaseAfterExecution = getExecutionEndSemaphore(testEnvironment);
            blockingSemaphoreToReleaseAfterExecution.release(1000);
        }

        //Some Tests need a minimal sleep here
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Semaphore getExecutionEndSemaphore(final TestEnvironment testEnvironment) {
        return testEnvironment.getPropertyAsType(EXECUTION_END_SEMAPHORE, Semaphore.class);
    }

    private void closeSut(final MessageBus messageBus) throws InterruptedException {
        final int timeout = 3;
        messageBus.close(true);
        if (!messageBus.awaitTermination(timeout, SECONDS)) {
            throw new RuntimeException("Messagebus did shutdown within timeout interval.");
        }
    }
}
