package com.envimate.messageMate.internal.pipe.givenWhenThen;

import com.envimate.messageMate.internal.pipe.error.PipeErrorHandler;
import com.envimate.messageMate.shared.environment.TestEnvironment;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;

import static com.envimate.messageMate.shared.properties.SharedTestProperties.EXPECTED_AND_IGNORED_EXCEPTION;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
final class PipeTestErrorHandler implements PipeErrorHandler<TestMessage> {
    private final Consumer<Exception> exceptionHandlerForNotIgnoredExceptions;
    private final TestEnvironment testEnvironment;
    private final Class<?>[] ignoredExceptionsClasses;

    static PipeTestErrorHandler pipeTestErrorHandler(final Consumer<Exception> exceptionHandlerForNotIgnoredExceptions,
                                                     final TestEnvironment testEnvironment,
                                                     final Class<?>... ignoredExceptionsClasses) {
        return new PipeTestErrorHandler(exceptionHandlerForNotIgnoredExceptions, testEnvironment, ignoredExceptionsClasses);
    }

    @Override
    public boolean shouldErrorBeHandledAndDeliveryAborted(final TestMessage message, final Exception e) {
        for (final Class<?> ignoredExceptionClass : ignoredExceptionsClasses) {
            if (e.getClass().equals(ignoredExceptionClass)) {
                testEnvironment.addToListProperty(EXPECTED_AND_IGNORED_EXCEPTION, e);
                return false;
            }
        }
        return true;
    }

    @Override
    public void handleException(final TestMessage message, final Exception e) {
        exceptionHandlerForNotIgnoredExceptions.accept(e);
    }
}
