package com.envimate.messageMate.qcec.querying.givenWhenThen;

import com.envimate.messageMate.qcec.queryresolving.InvalidQueryDueToExceptionOccurredInReceiverException;
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

    public static QueryValidationBuilder expectNoResult() {
        return new QueryValidationBuilder(testEnvironment -> {
            ensureNoExceptionOccurred(testEnvironment);
            final Optional<?> result = testEnvironment.getPropertyAsType(RESULT, Optional.class);
            assertFalse(result.isPresent());
        });
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
