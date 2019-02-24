package com.envimate.messageMate.qcec.querying.givenWhenThen;

import com.envimate.messageMate.qcec.querying.config.TestQueryResolver;
import com.envimate.messageMate.qcec.shared.TestAction;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.qcec.shared.TestValidation;
import lombok.RequiredArgsConstructor;

import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.EXCEPTION;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.RESULT;
import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
public class Then {
    private final TestQueryResolver testQueryResolver;
    private final QueryActionBuilder queryActionBuilder;

    public void expect(final QueryValidationBuilder queryValidationBuilder) {
        final TestAction<TestQueryResolver> testAction = queryActionBuilder.build();
        final TestEnvironment testEnvironment = testQueryResolver.getEnvironment();
        try {
            final Object result = testAction.execute(testQueryResolver, testEnvironment);
            testEnvironment.setProperty(RESULT, result);
        } catch (final Exception e) {
            testEnvironment.setProperty(EXCEPTION, e);
        }
        final TestValidation validation = queryValidationBuilder.build();
        validation.validate(testEnvironment);
    }
}
