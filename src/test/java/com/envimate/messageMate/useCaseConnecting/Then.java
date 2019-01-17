package com.envimate.messageMate.useCaseConnecting;

import com.envimate.messageMate.qcec.shared.TestAction;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.qcec.shared.TestValidation;
import lombok.RequiredArgsConstructor;

import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.EXCEPTION;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.SUT;
import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
public final class Then {
    private final UseCaseConnectorSetupBuilder setupBuilder;
    private final UseCaseConnectorActionBuilder actionBuilder;

    public void expect(final UseCaseConnectorValidationBuilder validationBuilder) {
        final TestEnvironment testEnvironment = setupBuilder.build();
        final TestAction<UseCaseConnector> testAction = actionBuilder.build();
        final UseCaseConnector useCaseConnector = testEnvironment.getPropertyAsType(SUT, UseCaseConnector.class);
        try {
            testAction.execute(useCaseConnector, testEnvironment);
        } catch (final Exception e) {
            testEnvironment.setProperty(EXCEPTION, e);
        }
        final TestValidation validation = validationBuilder.build();
        validation.validate(testEnvironment);
    }

}
