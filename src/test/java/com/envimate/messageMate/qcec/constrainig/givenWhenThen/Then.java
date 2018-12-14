package com.envimate.messageMate.qcec.constrainig.givenWhenThen;

import com.envimate.messageMate.qcec.shared.TestAction;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.qcec.shared.TestValidation;
import lombok.RequiredArgsConstructor;

import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.EXCEPTION;
import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
public class Then {
    private final TestConstraintEnforcer testConstraintEnforcer;
    private final ConstraintActionBuilder constraintActionBuilder;


    public void then(final ConstraintValidationBuilder constraintValidationBuilder) {
        final TestEnvironment testEnvironment = testConstraintEnforcer.getEnvironment();
        final TestAction<TestConstraintEnforcer> testAction = constraintActionBuilder.build();
        try {
            testAction.execute(testConstraintEnforcer, testEnvironment);
        } catch (final Exception e) {
            testEnvironment.setProperty(EXCEPTION, e);
        }
        final TestValidation validation = constraintValidationBuilder.build();
        validation.validate(testEnvironment);
    }
}
