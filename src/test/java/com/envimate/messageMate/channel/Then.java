package com.envimate.messageMate.channel;

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
    private final ChannelSetupBuilder channelSetupBuilder;
    private final ChannelActionBuilder channelActionBuilder;

    public void then(final ChannelValidationBuilder channelValidationBuilder) {
        final TestEnvironment testEnvironment = channelSetupBuilder.build();
        final TestAction<Channel<TestMessage>> testAction = channelActionBuilder.build();
        @SuppressWarnings("unchecked")
        final Channel<TestMessage> channel = (Channel<TestMessage>) testEnvironment.getProperty(SUT);
        try {
            testAction.execute(channel, testEnvironment);
        } catch (final Exception e) {
            testEnvironment.setProperty(EXCEPTION, e);
        }
        final TestValidation testValidation = channelValidationBuilder.build();
        testValidation.validate(testEnvironment);
    }
}
