package com.envimate.messageMate.serializedMessageBus;

import com.envimate.messageMate.qcec.shared.TestAction;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.qcec.shared.TestValidation;
import lombok.RequiredArgsConstructor;

import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.EXCEPTION;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
public final class Then {
    private final SerializedMessageBusSetupBuilder setupBuilder;
    private final SerializedMessageBusActionBuilder actionBuilder;

    public void then(final SerializedMessageBusValidationBuilder validationBuilder) {
        final SerializedMessageBusSetupBuilder.SerializedMessageBusSetup setup = setupBuilder.build();
        final SerializedMessageBus serializedMessageBus = setup.getSerializedMessageBus();
        final TestEnvironment testEnvironment = setup.getTestEnvironment();

        final TestAction<SerializedMessageBus> action = actionBuilder.build();
        try {
            action.execute(serializedMessageBus, testEnvironment);
        } catch (final Exception e) {
            testEnvironment.setPropertyIfNotSet(EXCEPTION, e);
        }
        try {
            MILLISECONDS.sleep(15);
        } catch (final InterruptedException e) {
            testEnvironment.setPropertyIfNotSet(EXCEPTION, e);
        }
        final TestValidation testValidation = validationBuilder.build();
        testValidation.validate(testEnvironment);
    }
}
