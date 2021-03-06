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
import com.envimate.messageMate.qcec.domainBus.DocumentBusBuilder;
import com.envimate.messageMate.shared.environment.TestEnvironment;
import com.envimate.messageMate.qcec.shared.TestReceiver;
import com.envimate.messageMate.qcec.shared.testConstraints.SpecificConstraint;
import com.envimate.messageMate.qcec.shared.testConstraints.TestConstraint;
import com.envimate.messageMate.qcec.shared.testEvents.EndingEvent;
import com.envimate.messageMate.qcec.shared.testEvents.SpecificEvent;
import com.envimate.messageMate.qcec.shared.testEvents.TestEvent;
import com.envimate.messageMate.qcec.shared.testQueries.SpecificQuery;
import com.envimate.messageMate.qcec.shared.testQueries.TestQuery;
import lombok.RequiredArgsConstructor;

import static com.envimate.messageMate.shared.environment.TestEnvironmentProperty.*;
import static com.envimate.messageMate.qcec.shared.testConstraints.SpecificConstraint.specificConstraintWithId;
import static com.envimate.messageMate.qcec.shared.testQueries.SpecificQuery.specificQueryWithId;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public class TestDocumentBusBuilder {
    private final DocumentBus documentBus = DocumentBusBuilder.aDefaultDocumentBus();
    private final TestEnvironment testEnvironment = TestEnvironment.emptyTestEnvironment();

    public static TestDocumentBusBuilder aDocumentBus() {
        return new TestDocumentBusBuilder();
    }

    public TestDocumentBusBuilder withSeveralQuerySubscriberThatUnsubscribeAfterASpecificEventWasReceived() {
        final int numberOfReceiver = 5;
        final int partialResult = 10;
        for (int i = 0; i < numberOfReceiver; i++) {
            documentBus.answer(TestQuery.class)
                    .until(EndingEvent.class, t -> true)
                    .using(q -> q.addPartialResult(partialResult));
        }
        final int expectedResult = partialResult * numberOfReceiver;
        testEnvironment.setProperty(EXPECTED_RESULT, expectedResult);
        return this;
    }

    public TestDocumentBusBuilder withSeveralSubscriberThatOnlyTakeSpecificQueries() {
        final int numberOfReceiver = 5;
        final int idOfInterest = 123;
        for (int i = 0; i < numberOfReceiver; i++) {
            final TestReceiver<SpecificQuery> receiver = TestReceiver.aTestReceiver();
            documentBus.answer(SpecificQuery.class)
                    .onlyIf(specificQuery -> specificQuery.getId() == idOfInterest)
                    .using(receiver);
            testEnvironment.addToListProperty(EXPECTED_RECEIVERS, receiver);
        }
        final SpecificQuery specificQuery = specificQueryWithId(idOfInterest);
        testEnvironment.setProperty(TEST_OBJECT, specificQuery);
        return this;
    }

    public TestDocumentBusBuilder withSeveralConstraintSubscriberThatUnsubscribeAfterASpecificEventWasReceived() {
        final int numberOfReceiver = 5;
        for (int i = 0; i < numberOfReceiver; i++) {
            final TestReceiver<TestConstraint> receiver = TestReceiver.aTestReceiver();
            documentBus.ensure(TestConstraint.class)
                    .until(EndingEvent.class, t -> true)
                    .using(receiver);
            testEnvironment.addToListProperty(EXPECTED_RECEIVERS, receiver);
        }
        return this;
    }

    public TestDocumentBusBuilder withSeveralSubscriberThatOnlyTakeSpecificConstraints() {
        final int numberOfReceiver = 5;
        final int idOfInterest = 123;
        for (int i = 0; i < numberOfReceiver; i++) {
            final TestReceiver<SpecificConstraint> receiver = TestReceiver.aTestReceiver();
            documentBus.ensure(SpecificConstraint.class)
                    .onlyIf(specificConstraint -> specificConstraint.getId() == idOfInterest)
                    .using(receiver);
            testEnvironment.addToListProperty(EXPECTED_RECEIVERS, receiver);
        }
        final SpecificConstraint specificConstraint = specificConstraintWithId(idOfInterest);
        testEnvironment.setProperty(TEST_OBJECT, specificConstraint);
        return this;
    }

    public TestDocumentBusBuilder withSeveralEventSubscriberThatUnsubscribeAfterASpecificEventWasReceived() {
        final int numberOfReceiver = 5;
        for (int i = 0; i < numberOfReceiver; i++) {
            final TestReceiver<TestEvent> receiver = TestReceiver.aTestReceiver();
            documentBus.reactTo(TestEvent.class)
                    .until(EndingEvent.class, t -> true)
                    .using(receiver);
            testEnvironment.addToListProperty(EXPECTED_RECEIVERS, receiver);
        }
        return this;
    }

    public TestDocumentBusBuilder withSeveralSubscriberThatOnlyTakeSpecificEvents() {
        final int numberOfReceiver = 5;
        final int idOfInterest = 123;
        for (int i = 0; i < numberOfReceiver; i++) {
            final TestReceiver<SpecificEvent> receiver = TestReceiver.aTestReceiver();
            documentBus.reactTo(SpecificEvent.class)
                    .onlyIf(specificEvent -> specificEvent.getId() == idOfInterest)
                    .using(receiver);
            testEnvironment.addToListProperty(EXPECTED_RECEIVERS, receiver);
        }
        final SpecificEvent specificEvent = SpecificEvent.specificEventWithId(idOfInterest);
        testEnvironment.setProperty(TEST_OBJECT, specificEvent);
        return this;
    }

    public TestDocumentBusBuilder withASubscriberForTheUnscubscribeEvent() {
        final TestReceiver<SpecificEvent> receiver = TestReceiver.aTestReceiver();
        documentBus.reactTo(SpecificEvent.class)
                .until(SpecificEvent.class)
                .using(receiver);
        testEnvironment.addToListProperty(EXPECTED_RECEIVERS, receiver);
        final SpecificEvent specificEvent = SpecificEvent.specificEventWithId(0);
        testEnvironment.setProperty(TEST_OBJECT, specificEvent);
        return this;
    }

    public TestEnvironment getTestEnvironment() {
        return testEnvironment;
    }

    public DocumentBus build() {
        return documentBus;
    }
}
