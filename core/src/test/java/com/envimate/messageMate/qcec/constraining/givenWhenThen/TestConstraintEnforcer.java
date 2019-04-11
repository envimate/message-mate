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

import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.qcec.shared.TestReceiver;
import com.envimate.messageMate.qcec.shared.testConstraints.TestConstraint;
import com.envimate.messageMate.subscribing.SubscriptionId;

import java.util.function.Consumer;

import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.EXPECTED_RECEIVERS;

public abstract class TestConstraintEnforcer {
    private final TestEnvironment testEnvironment = TestEnvironment.emptyTestEnvironment();

    public TestEnvironment getEnvironment() {
        return testEnvironment;
    }

    public abstract void enforce(Object constraint);

    public abstract <T> TestConstraintEnforcer withASubscriber(Class<T> constraintClass, Consumer<T> consumer);

    public abstract <T> SubscriptionId subscribing(Class<T> constraintClass, Consumer<T> consumer);

    public abstract void unsubscribe(SubscriptionId subscriptionId);

    public TestConstraintEnforcer withSeveralSubscriber() {
        final int numberOfSubscriber = 5;
        for (int i = 0; i < numberOfSubscriber; i++) {
            final TestReceiver<TestConstraint> receiver = TestReceiver.aTestReceiver();
            testEnvironment.addToListProperty(EXPECTED_RECEIVERS, receiver);
            withASubscriber(TestConstraint.class, receiver);
        }
        return this;
    }
}
