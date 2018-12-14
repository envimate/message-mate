package com.envimate.messageMate.qcec.eventing.givenWhenThen;

import com.envimate.messageMate.qcec.shared.TestAction;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.qcec.shared.TestValidation;
import lombok.RequiredArgsConstructor;

import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.EXCEPTION;
import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
public class Then {
    private final TestEventBus testEventBus;
    private final EventBusActionBuilder eventBusActionBuilder;


    public void then(final EventBusValidationBuilder eventBusValidationBuilder) {
        final TestAction<TestEventBus> action = eventBusActionBuilder.build();
        final TestEnvironment testEnvironment = testEventBus.getTestEnvironment();
        try {
            action.execute(testEventBus, testEnvironment);
        } catch (final Exception e) {
            testEnvironment.setProperty(EXCEPTION, e);
        }
        final TestValidation validation = eventBusValidationBuilder.build();
        validation.validate(testEnvironment);
    }
}
