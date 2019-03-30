package com.envimate.messageMate.useCaseAdapter;

import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.qcec.shared.TestAction;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.qcec.shared.TestValidation;
import lombok.RequiredArgsConstructor;

import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.EXCEPTION;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.MOCK;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
public class Then {
    private final UseCaseAdapterSetupBuilder setupBuilder;
    private final UseCaseAdapterActionBuilder actionBuilder;

    public void then(final UseCaseAdapterValidationBuilder validationBuilder) {
        final TestEnvironment testEnvironment = setupBuilder.build();
        final TestAction<MessageBus> testAction = actionBuilder.build();
        final MessageBus messageBus = testEnvironment.getPropertyAsType(MOCK, MessageBus.class);
        try {
            testAction.execute(messageBus, testEnvironment);
        } catch (Exception e) {
            testEnvironment.setProperty(EXCEPTION, e);
        }
        try {
            MILLISECONDS.sleep(10);
        } catch (InterruptedException e) {
            testEnvironment.setProperty(EXCEPTION, e);
        }
        final TestValidation validation = validationBuilder.build();
        validation.validate(testEnvironment);
    }
}
