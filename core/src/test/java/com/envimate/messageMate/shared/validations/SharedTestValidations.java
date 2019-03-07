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

package com.envimate.messageMate.shared.validations;

import com.envimate.messageMate.qcec.shared.TestEnvironment;
import lombok.RequiredArgsConstructor;

import java.util.Date;
import java.util.List;

import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.*;
import static lombok.AccessLevel.PRIVATE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.fail;

@RequiredArgsConstructor(access = PRIVATE)
public final class SharedTestValidations {

    public static void assertResultAndExpectedResultAreEqual(final TestEnvironment testEnvironment) {
        final Object expectedResult = testEnvironment.getProperty(EXPECTED_RESULT);
        assertResultEqualsExpected(testEnvironment, expectedResult);
    }

    public static void assertResultEqualsExpected(final TestEnvironment testEnvironment, final Object expectedResult) {
        final Object result = testEnvironment.getProperty(RESULT);
        assertEquals(result, expectedResult);
    }

    public static void assertEquals(final Object result, final Object expectedResult) {
        if (expectedResult instanceof Number && result instanceof Number) {
            final double resultAsDouble = ((Number) result).doubleValue();
            final double expectedAsDouble = ((Number) expectedResult).doubleValue();
            assertThat(resultAsDouble, equalTo(expectedAsDouble));
        } else {
            assertThat(result, equalTo(expectedResult));
        }
    }


    public static void assertResultOfClass(final TestEnvironment testEnvironment, final Class<?> expectedResultClass) {
        final Object result = testEnvironment.getProperty(RESULT);
        assertThat(result.getClass(), equalTo(expectedResultClass));
    }

    public static void assertNoResultSet(final TestEnvironment testEnvironment) {
        if (testEnvironment.has(RESULT)) {
            final Object result = testEnvironment.getProperty(RESULT);
            fail("Unexpected result: " + result);
        }
    }

    public static void assertNoExceptionThrown(final TestEnvironment testEnvironment) {
        if (testEnvironment.has(EXCEPTION)) {
            final Exception exception = testEnvironment.getPropertyAsType(EXCEPTION, Exception.class);
            fail("Unexpected exception", exception);
        }
    }

    public static void assertExceptionThrownOfType(final TestEnvironment testEnvironment, final Class<?> expectedExceptionClass) {
        final Exception exception = testEnvironment.getPropertyAsType(EXCEPTION, Exception.class);
        assertThat(exception.getClass(), equalTo(expectedExceptionClass));
    }

    public static void assertTimestampToBeInTheLastXSeconds(final TestEnvironment testEnvironment, final long maximumSecondsDifference) {
        final Date now = new Date();
        final Date timestamp = testEnvironment.getPropertyAsType(RESULT, Date.class);
        final long secondsDifference = (now.getTime() - timestamp.getTime()) / 1000;
        assertThat(secondsDifference, lessThanOrEqualTo(maximumSecondsDifference));
    }

    public static void assertListOfSize(final TestEnvironment testEnvironment, final int expectedSize) {
        final List<?> list = testEnvironment.getPropertyAsType(RESULT, List.class);
        assertThat(list.size(), equalTo(expectedSize));
    }
}