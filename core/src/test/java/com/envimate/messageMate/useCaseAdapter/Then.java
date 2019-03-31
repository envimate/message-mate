package com.envimate.messageMate.useCaseAdapter;

import com.envimate.messageMate.qcec.shared.TestAction;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import lombok.RequiredArgsConstructor;

import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.EXCEPTION;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
public class Then {
    private final UseCaseAdapterSetupBuilder setupBuilder;
    private final UseCaseAdapterActionBuilder actionBuilder;

    public void then(final UseCaseAdapterValidationBuilder validationBuilder) {
        final UseCaseAdapterSetup setup = setupBuilder.build();
        final TestEnvironment testEnvironment = setup.getTestEnvironment();
        final TestAction<TestUseCase> testAction = actionBuilder.build();
        final TestUseCase testUseCase = setup.getTestUseCase();
        try {
            testAction.execute(testUseCase, testEnvironment);
        } catch (Exception e) {
            testEnvironment.setProperty(EXCEPTION, e);
        }
        try {
            MILLISECONDS.sleep(20);
        } catch (InterruptedException e) {
            testEnvironment.setProperty(EXCEPTION, e);
        }
        final UseCaseAdapterValidationBuilder.UseCaseAdapterTestValidation validation = validationBuilder.build();
        validation.validate(testUseCase, testEnvironment);
    }
}
