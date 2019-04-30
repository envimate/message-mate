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

package com.envimate.messageMate.qcec.documentBus.givenWhenThen;

import com.envimate.messageMate.qcec.domainBus.DocumentBus;
import com.envimate.messageMate.shared.givenWhenThen.TestAction;
import com.envimate.messageMate.qcec.shared.testConstraints.SpecificConstraint;
import com.envimate.messageMate.qcec.shared.testConstraints.TestConstraint;
import com.envimate.messageMate.qcec.shared.testEvents.EndingEvent;
import com.envimate.messageMate.qcec.shared.testEvents.SpecificEvent;
import com.envimate.messageMate.qcec.shared.testEvents.TestEvent;
import com.envimate.messageMate.qcec.shared.testQueries.SpecificQuery;
import com.envimate.messageMate.qcec.shared.testQueries.TestQuery;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;

import static com.envimate.messageMate.shared.environment.TestEnvironmentProperty.EXPECTED_RESULT;
import static com.envimate.messageMate.shared.environment.TestEnvironmentProperty.TEST_OBJECT;
import static com.envimate.messageMate.qcec.shared.testConstraints.SpecificConstraint.specificConstraintWithId;
import static com.envimate.messageMate.qcec.shared.testEvents.SpecificEvent.specificEventWithId;
import static com.envimate.messageMate.qcec.shared.testEvents.TestEvent.testEvent;
import static com.envimate.messageMate.qcec.shared.testQueries.SpecificQuery.specificQueryWithId;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DocumentBusActionBuilder {
    private final TestAction<DocumentBus> action;

    public static DocumentBusActionBuilder aQueryTheEventAndASecondQueryAreSend() {
        return new DocumentBusActionBuilder((documentBus, testEnvironment) -> {
            final List<Object> results = new LinkedList<>();
            final Integer firstResult = documentBus.queryRequired(TestQuery.aTestQuery());
            results.add(firstResult);

            final EndingEvent event = EndingEvent.endingEvent();
            documentBus.publish(event);
            results.add(event);

            final Integer secondResult = documentBus.queryRequired(TestQuery.aTestQuery());
            results.add(secondResult);
            return results;
        });
    }

    public static DocumentBusActionBuilder oneQueryOfInterestAndSeveralOtherAreSend() {
        return new DocumentBusActionBuilder((documentBus, testEnvironment) -> {
            final int numberOfQueries = 3;
            for (int i = 0; i < numberOfQueries; i++) {
                documentBus.query(specificQueryWithId(i));
            }
            final SpecificQuery specificQuery = testEnvironment.getPropertyAsType(TEST_OBJECT, SpecificQuery.class);
            documentBus.query(specificQuery);
            for (int i = 0; i < numberOfQueries; i++) {
                documentBus.query(specificQueryWithId(i * i));
            }
            return null;
        });
    }

    public static DocumentBusActionBuilder aConstraintTheEventAndASecondConstraintAreSend() {
        return new DocumentBusActionBuilder((documentBus, testEnvironment) -> {
            final TestConstraint constraint1 = TestConstraint.testConstraint();
            documentBus.enforce(constraint1);
            testEnvironment.setProperty(EXPECTED_RESULT, constraint1);

            final EndingEvent event = EndingEvent.endingEvent();
            documentBus.publish(event);

            final TestConstraint constraint2 = TestConstraint.testConstraint();
            documentBus.enforce(constraint2);
            return null;
        });
    }

    public static DocumentBusActionBuilder oneConstraintOfInterestAndSeveralOtherAreSend() {
        return new DocumentBusActionBuilder((documentBus, testEnvironment) -> {
            final int numberOfEvents = 3;
            for (int i = 0; i < numberOfEvents; i++) {
                documentBus.enforce(specificConstraintWithId(i));
            }
            final SpecificConstraint constraint = testEnvironment.getPropertyAsType(TEST_OBJECT, SpecificConstraint.class);
            documentBus.enforce(constraint);
            for (int i = 0; i < numberOfEvents; i++) {
                documentBus.enforce(specificConstraintWithId(i * i));
            }
            return null;
        });
    }

    public static DocumentBusActionBuilder anEventThenTheUnsubscribeEventAndAThirdEventAreSend() {
        return new DocumentBusActionBuilder((documentBus, testEnvironment) -> {
            final TestEvent event1 = testEvent();
            documentBus.publish(event1);
            testEnvironment.setProperty(EXPECTED_RESULT, event1);

            final EndingEvent event = EndingEvent.endingEvent();
            documentBus.publish(event);

            final TestEvent event2 = testEvent();
            documentBus.publish(event2);
            return null;
        });
    }

    public static DocumentBusActionBuilder oneEventOfInterestAndSeveralOtherAreSend() {
        return new DocumentBusActionBuilder((documentBus, testEnvironment) -> {
            final int numberOfEvents = 3;
            for (int i = 0; i < numberOfEvents; i++) {
                documentBus.publish(specificEventWithId(i));
            }
            final SpecificEvent specificEvent = testEnvironment.getPropertyAsType(TEST_OBJECT, SpecificEvent.class);
            documentBus.publish(specificEvent);
            for (int i = 0; i < numberOfEvents; i++) {
                documentBus.publish(specificEventWithId(i * i));
            }
            return null;
        });
    }

    public static DocumentBusActionBuilder anEventIsSend() {
        return new DocumentBusActionBuilder((documentBus, testEnvironment) -> {
            final SpecificEvent specificEvent = testEnvironment.getPropertyAsType(TEST_OBJECT, SpecificEvent.class);
            documentBus.publish(specificEvent);
            return null;
        });
    }

    public TestAction<DocumentBus> build() {
        return action;
    }
}
