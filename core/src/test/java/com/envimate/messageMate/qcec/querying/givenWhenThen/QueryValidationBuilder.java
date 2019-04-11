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

package com.envimate.messageMate.qcec.querying.givenWhenThen;

import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.qcec.shared.TestValidation;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.*;
import static lombok.AccessLevel.PRIVATE;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

@RequiredArgsConstructor(access = PRIVATE)
public final class QueryValidationBuilder {
    private final TestValidation validation;

    public static QueryValidationBuilder theCorrectResult() {
        return new QueryValidationBuilder(testEnvironment -> {
            ensureNoExceptionOccurred(testEnvironment);
            final Object result = testEnvironment.getProperty(RESULT);
            final Object expectedResult = testEnvironment.getProperty(EXPECTED_RESULT);
            assertThat(result, equalTo(expectedResult));
        });
    }

    public static QueryValidationBuilder theResult(final Object expectedResult) {
        return new QueryValidationBuilder(testEnvironment -> {
            ensureNoExceptionOccurred(testEnvironment);
            final Object result = testEnvironment.getProperty(RESULT);
            assertThat(result, equalTo(expectedResult));
        });
    }


    public static QueryValidationBuilder aExceptionWithMessageMatchingRegex(final String messageRegex) {
        return new QueryValidationBuilder(testEnvironment -> {
            final Exception exception = testEnvironment.getPropertyAsType(EXCEPTION, Exception.class);
            final String message = exception.getMessage();
            final Pattern pattern = Pattern.compile(messageRegex);
            final Matcher matcher = pattern.matcher(message);
            assertThat(matcher.matches(), equalTo(true));
        });
    }

    public static QueryValidationBuilder theThrownException() {
        return aExceptionForNoResultButOneWasRequired();
    }

    public static QueryValidationBuilder aExceptionForNoResultButOneWasRequired() {
        return new QueryValidationBuilder(testEnvironment -> {
            final Exception exception = testEnvironment.getPropertyAsType(EXCEPTION, Exception.class);
            final String expectedExceptionMessage = testEnvironment.getPropertyAsType(EXPECTED_EXCEPTION_MESSAGE, String.class);
            assertThat(exception.getMessage(), equalTo(expectedExceptionMessage));
        });
    }

    private static void ensureNoExceptionOccurred(final TestEnvironment testEnvironment) {
        final boolean exceptionOccurred = testEnvironment.has(EXCEPTION);
        if (exceptionOccurred) {
            final Exception thrownException = testEnvironment.getPropertyAsType(EXCEPTION, Exception.class);
            fail("Expected no exception but got " + thrownException.getClass(), thrownException);
        }
    }

    public TestValidation build() {
        return validation;
    }
}
