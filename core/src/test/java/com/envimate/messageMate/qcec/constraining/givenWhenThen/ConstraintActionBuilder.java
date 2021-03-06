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

package com.envimate.messageMate.qcec.constraining.givenWhenThen;

import com.envimate.messageMate.shared.givenWhenThen.TestAction;
import com.envimate.messageMate.qcec.shared.TestReceiver;
import com.envimate.messageMate.qcec.shared.testConstraints.TestConstraint;
import com.envimate.messageMate.subscribing.SubscriptionId;
import lombok.RequiredArgsConstructor;

import static com.envimate.messageMate.shared.environment.TestEnvironmentProperty.*;
import static com.envimate.messageMate.qcec.shared.TestReceiver.aTestReceiver;
import static com.envimate.messageMate.qcec.shared.testConstraints.TestConstraint.testConstraint;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class ConstraintActionBuilder {
    private final TestAction<TestConstraintEnforcer> testAction;

    public static ConstraintActionBuilder aPassingConstraintIsEnforced() {
        return new ConstraintActionBuilder((testConstraintEnforcer, testEnvironment) -> {
            final TestConstraint testConstraint = testConstraint();
            testEnvironment.setProperty(TEST_OBJECT, testConstraint);
            testConstraintEnforcer.enforce(testConstraint);
            return null;
        });
    }

    public static ConstraintActionBuilder anExceptionCausingConstraintIsEnforced() {
        return new ConstraintActionBuilder((testConstraintEnforcer, testEnvironment) -> {
            final TestConstraint testConstraint = testConstraint();
            testEnvironment.setProperty(TEST_OBJECT, testConstraint);
            final String expectedExceptionMessage = "Constraint exception";
            testEnvironment.setProperty(EXPECTED_EXCEPTION_MESSAGE, expectedExceptionMessage);
            testConstraintEnforcer.withASubscriber(TestConstraint.class, c -> {
                throw new RuntimeException(expectedExceptionMessage);
            });
            testConstraintEnforcer.enforce(testConstraint);
            return null;
        });
    }

    public static ConstraintActionBuilder anReceiverUnsubscribes() {
        return new ConstraintActionBuilder((testConstraintEnforcer, testEnvironment) -> {
            final TestReceiver<TestConstraint> unsubscribingReceiver = aTestReceiver();
            final TestReceiver<TestConstraint> remainingReceiver = aTestReceiver();

            final SubscriptionId subscriptionId = testConstraintEnforcer.subscribing(TestConstraint.class, unsubscribingReceiver);
            testConstraintEnforcer.subscribing(TestConstraint.class, remainingReceiver);
            testConstraintEnforcer.unsubscribe(subscriptionId);

            testEnvironment.addToListProperty(EXPECTED_RECEIVERS, remainingReceiver);

            final TestConstraint testConstraint = testConstraint();
            testEnvironment.setProperty(TEST_OBJECT, testConstraint);
            testConstraintEnforcer.enforce(testConstraint);
            return null;
        });
    }

    public TestAction<TestConstraintEnforcer> build() {
        return testAction;
    }
}
