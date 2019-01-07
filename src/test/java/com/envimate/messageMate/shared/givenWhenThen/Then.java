package com.envimate.messageMate.shared.givenWhenThen;

import com.envimate.messageMate.shared.context.TestExecutionContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.envimate.messageMate.shared.context.TestExecutionProperty.SUT;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class Then<T> {
    private final Setup<T> setup;


    public void then(final TestValidationBuilder<T> testValidationBuilder) {
        final List<TestValidation<T>> validations = testValidationBuilder.build();
        final TestExecutionContext testExecutionContext = setup.testExecutionContext;
        @SuppressWarnings("unchecked")
        final T t = (T) testExecutionContext.getProperty(SUT);
        for (final TestValidation<T> validation : validations) {
            validation.validate(t, testExecutionContext);
        }
    }
}
