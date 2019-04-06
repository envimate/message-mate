package com.envimate.messageMate.useCaseAdapter;

import com.envimate.messageMate.messageFunction.ResponseFuture;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.qcec.shared.TestEnvironmentProperty;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static com.envimate.messageMate.shared.validations.SharedTestValidations.*;
import static com.envimate.messageMate.useCaseAdapter.UseCaseAdapterTestProperties.RETRIEVE_ERROR_FROM_FUTURE;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static lombok.AccessLevel.PRIVATE;
import static org.junit.jupiter.api.Assertions.fail;

@RequiredArgsConstructor(access = PRIVATE)
public final class UseCaseAdapterValidationBuilder {
    private final UseCaseAdapterTestValidation testValidation;

    private static UseCaseAdapterValidationBuilder asValidation(final UseCaseAdapterTestValidation testValidation) {
        return new UseCaseAdapterValidationBuilder(testValidation);
    }

    public static UseCaseAdapterValidationBuilder expectTheUseCaseToBeInvokedOnce() {
        return asValidation((testUseCase, testEnvironment) -> {
            assertNoExceptionThrown(testEnvironment);
            final Object expectedResult = testUseCase.getExpectedResult(testEnvironment);
            assertResultEqualsExpected(testEnvironment, expectedResult);
        });
    }

    public static UseCaseAdapterValidationBuilder expectTheResponseToBeReceivedByTheMessageFunction() {
        return asValidation((testUseCase, testEnvironment) -> {
            assertNoExceptionThrown(testEnvironment);
            final Object expectedResult = testUseCase.getExpectedResult(testEnvironment);
            final ResponseFuture responseFuture = testEnvironment.getPropertyAsType(TestEnvironmentProperty.RESULT, ResponseFuture.class);
            try {
                final Object result;
                if(testEnvironment.has(RETRIEVE_ERROR_FROM_FUTURE)) {
                    result = responseFuture.getErrorResponse(10, MILLISECONDS);
                }else {
                    result = responseFuture.get(10, MILLISECONDS);
                }
                assertEquals(result, expectedResult);
            } catch (final InterruptedException | TimeoutException e) {
                fail(e);
            } catch (final ExecutionException e) {
                final Throwable testException = e.getCause();
                assertEquals(expectedResult, testException);
            }
        });
    }

    public static UseCaseAdapterValidationBuilder expectAExceptionOfType(final Class<?> expectedExceptionClass) {
        return asValidation((testUseCase, testEnvironment) -> {
            assertExceptionThrownOfType(testEnvironment, expectedExceptionClass);
        });
    }

    public UseCaseAdapterTestValidation build() {
        return testValidation;
    }

    interface UseCaseAdapterTestValidation {
        void validate(TestUseCase testUseCase, TestEnvironment testEnvironment);
    }
}
