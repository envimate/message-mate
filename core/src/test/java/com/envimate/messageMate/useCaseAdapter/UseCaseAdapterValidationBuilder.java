package com.envimate.messageMate.useCaseAdapter;

import com.envimate.messageMate.qcec.shared.TestValidation;
import lombok.RequiredArgsConstructor;

import static com.envimate.messageMate.shared.validations.SharedTestValidations.assertNoExceptionThrown;
import static com.envimate.messageMate.shared.validations.SharedTestValidations.assertResultAndExpectedResultAreEqual;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class UseCaseAdapterValidationBuilder {
    private final TestValidation testValidation;

    private static UseCaseAdapterValidationBuilder asValidation(final TestValidation testValidation) {
        return new UseCaseAdapterValidationBuilder(testValidation);
    }

    public static UseCaseAdapterValidationBuilder expectTheUseCaseToBeInvokedOnce() {
        return asValidation(testEnvironment -> {
            assertNoExceptionThrown(testEnvironment);
            assertResultAndExpectedResultAreEqual(testEnvironment);
        });
    }

    public TestValidation build() {
        return testValidation;
    }
}
