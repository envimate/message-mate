package com.envimate.messageMate.useCases;

import com.envimate.messageMate.processingContext.EventType;
import com.envimate.messageMate.qcec.shared.TestAction;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import lombok.RequiredArgsConstructor;

import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.EXCEPTION;
import static com.envimate.messageMate.useCases.UseCaseInvocationTestProperties.EVENT_TYPE;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
public class Then {
    private final UseCaseInvocationSetupBuilder setupBuilder;
    private final UseCaseInvocationActionBuilder actionBuilder;

    public void then(final UseCaseInvocationValidationBuilder validationBuilder) {
        final UseCaseInvocationSetup setup = setupBuilder.build();
        final TestEnvironment testEnvironment = setup.getTestEnvironment();
        final TestAction<TestUseCase> testAction = actionBuilder.build();
        final TestUseCase testUseCase = setup.getTestUseCase();
        final EventType eventType = testUseCase.getEventType();
        testEnvironment.setPropertyIfNotSet(EVENT_TYPE, eventType);
        try {
            testAction.execute(testUseCase, testEnvironment);
        } catch (final Exception e) {
            testEnvironment.setProperty(EXCEPTION, e);
        }
        try {
            MILLISECONDS.sleep(10);
        } catch (final InterruptedException e) {
            testEnvironment.setProperty(EXCEPTION, e);
        }
        final UseCaseInvocationValidationBuilder.UseCaseAdapterTestValidation validation = validationBuilder.build();
        validation.validate(testUseCase, testEnvironment);
    }
}
