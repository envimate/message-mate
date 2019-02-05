package com.envimate.messageMate.chain;

import com.envimate.messageMate.qcec.shared.TestAction;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.qcec.shared.TestValidation;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import lombok.RequiredArgsConstructor;

import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.EXCEPTION;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.SUT;
import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
public class Then {
    private final ChainSetupBuilder chainSetupBuilder;
    private final ChainActionBuilder chainActionBuilder;

    public void then(final ChainValidationBuilder chainValidationBuilder) {
        final TestEnvironment testEnvironment = chainSetupBuilder.build();
        final TestAction<Chain<TestMessage>> testAction = chainActionBuilder.build();
        @SuppressWarnings("unchecked")
        final Chain<TestMessage> chain = (Chain<TestMessage>) testEnvironment.getProperty(SUT);
        try {
            testAction.execute(chain, testEnvironment);
        } catch (final Exception e) {
            testEnvironment.setProperty(EXCEPTION, e);
        }
        final TestValidation testValidation = chainValidationBuilder.build();
        testValidation.validate(testEnvironment);
    }
}
