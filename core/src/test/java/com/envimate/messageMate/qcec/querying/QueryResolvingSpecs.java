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

package com.envimate.messageMate.qcec.querying;

import com.envimate.messageMate.qcec.querying.config.TestQueryResolver;
import com.envimate.messageMate.qcec.shared.testQueries.SpecificQuery;
import com.envimate.messageMate.qcec.shared.testQueries.SuperclassTestQuery;
import com.envimate.messageMate.qcec.shared.testQueries.TestQuery;
import org.junit.jupiter.api.Test;

import static com.envimate.messageMate.qcec.querying.givenWhenThen.Given.given;
import static com.envimate.messageMate.qcec.querying.givenWhenThen.QueryActionBuilder.*;
import static com.envimate.messageMate.qcec.querying.givenWhenThen.QueryValidationBuilder.*;
import static com.envimate.messageMate.qcec.shared.testQueries.SubclassingTestQuery.aSubclassingQuery;

public interface QueryResolvingSpecs {

    @Test
    default void testQueryResolver_whenResolvingAQuery_aValidResultIsReturned(final TestQueryResolver aQueryResolver) {
        given(aQueryResolver)
                .when(aQueryIsExecuted())
                .expect(theCorrectResult());
    }

    @Test
    default void testQueryResolver_whenResolvingAQueryWithARequiredResult_aValidResultIsReturned(final TestQueryResolver aQueryResolver) {
        given(aQueryResolver)
                .when(aQueryIsExecutedThatRequiresAResult())
                .expect(theCorrectResult());
    }

    @Test
    default void testQueryResolver_whenResolvingAQueryWithPartialResults_aValidResultIsReturned(final TestQueryResolver aQueryResolver) {
        given(aQueryResolver)
                .when(aQueryIsExecutedThatCollectsPartialResults())
                .expect(theCorrectResult());
    }

    @Test
    default void testQueryResolver_whenResolvingAQueryWithARequiredResult_throwsExceptionWhenNoResultsIsObtained(final TestQueryResolver aQueryResolver) {
        given(aQueryResolver)
                .when(aQueryIsExecutedThatRequiresAResultButDoesntProvideOne())
                .expect(aExceptionWithMessageMatchingRegex("^Expected a query result for query .+$"));
    }

    @Test
    default void testQueryResolver_unsubscribe(final TestQueryResolver aQueryResolver) {
        given(aQueryResolver)
                .when(anRecipientIsUnsubscribedBeforeAQueryIsExecuted())
                .expect(theCorrectResult());
    }

    @Test
    default void testQueryResolver_queryIsDeliveredToAllSuperClasses(final TestQueryResolver aQueryResolver) {
        final int expectedResult = 5;
        final int invalidResponse = 1000;
        given(aQueryResolver
                .withASubscriber(SuperclassTestQuery.class, q -> q.reportMatch(expectedResult))
                .withASubscriber(TestQuery.class, q -> q.setResult(invalidResponse)))
                .when(theQueryIsExecuted(aSubclassingQuery()))
                .expect(theResult(expectedResult));
    }

    @Test
    default void testQueryResolver_queryCanBeStoppedEarly(final TestQueryResolver aQueryResolver) {
        final int expectedResult = 5;
        final int invalidResponse = 1000;
        given(aQueryResolver
                .withASubscriber(TestQuery.class, q -> {
                    q.setResult(expectedResult);
                    q.finishQuery();
                })
                .withASubscriber(TestQuery.class, q -> q.setResult(invalidResponse)))
                .when(theQueryIsExecuted(TestQuery.aTestQuery()))
                .expect(theResult(expectedResult));
    }

    @Test
    default void testQueryResolver_returnsNoResultWhenExceptionIsThrown(final TestQueryResolver aQueryResolver) {
        given(aQueryResolver)
                .when(aQueryIsExecutedThatThrowsAnException())
                .expect(theThrownException());
    }

    @Test
    default void testQueryResolver_throwsExceptionWhenExceptionIsThrownButAResultIsExpected(final TestQueryResolver aQueryResolver) {
        given(aQueryResolver)
                .when(aQueryIsExecutedThatExpectsAResultButDidThrowAnException())
                .expect(aExceptionForNoResultButOneWasRequired());
    }

    @Test
    default void testQueryResolver_allowsDifferentRegisteredQueries(final TestQueryResolver aQueryResolver) {
        final int expectedResult = 5;
        given(aQueryResolver
                .withASubscriber(TestQuery.class, q -> {
                    q.setResult(expectedResult);
                    q.finishQuery();
                })
                .withASubscriber(SpecificQuery.class, specificQuery -> {
                }))
                .when(theQueryIsExecuted(TestQuery.aTestQuery()))
                .expect(theResult(expectedResult));
    }
}
