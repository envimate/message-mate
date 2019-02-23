package com.envimate.messageMate.channel.givenWhenThen;

import com.envimate.messageMate.channel.Channel;
import com.envimate.messageMate.qcec.shared.TestAction;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.qcec.shared.TestValidation;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.Semaphore;

import static com.envimate.messageMate.channel.givenWhenThen.ChannelTestProperties.*;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.EXCEPTION;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.SUT;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
public class Then {
    private final ChannelSetupBuilder channelSetupBuilder;
    private final ChannelActionBuilder channelActionBuilder;

    public void then(final ChannelValidationBuilder channelValidationBuilder) {
        final TestEnvironment testEnvironment = channelSetupBuilder.build();
        final List<TestAction<Channel<TestMessage>>> testActions = channelActionBuilder.build();
        @SuppressWarnings("unchecked")
        final Channel<TestMessage> channel = (Channel<TestMessage>) testEnvironment.getProperty(SUT);
        try {
            for (final TestAction<Channel<TestMessage>> testAction : testActions) {
                testAction.execute(channel, testEnvironment);
                if (testEnvironment.has(SLEEP_BETWEEN_EXECUTION_STEPS)) {
                    final Long sleepDuration = testEnvironment.getPropertyAsType(SLEEP_BETWEEN_EXECUTION_STEPS, Long.class);
                    MILLISECONDS.sleep(sleepDuration);
                }
            }
            if (testEnvironment.has(SLEEP_AFTER_EXECUTION)) {
                final Long sleepDuration = testEnvironment.getPropertyAsType(SLEEP_AFTER_EXECUTION, Long.class);
                MILLISECONDS.sleep(sleepDuration);
            }
        } catch (final Exception e) {
            testEnvironment.setProperty(EXCEPTION, e);
        }
        final TestValidation testValidation = channelValidationBuilder.build();
        testValidation.validate(testEnvironment);
        if (testEnvironment.has(SEMAPHORE_TO_CLEAN_UP)) {
            final Semaphore semaphore = testEnvironment.getPropertyAsType(SEMAPHORE_TO_CLEAN_UP, Semaphore.class);
            semaphore.release(100);
        }
        if (testEnvironment.has(SLEEP_BEFORE_CLOSE)) {
            final Long sleepDuration = testEnvironment.getPropertyAsType(SLEEP_BEFORE_CLOSE, Long.class);
            try {
                MILLISECONDS.sleep(sleepDuration);
            } catch (final InterruptedException e) {
                testEnvironment.setProperty(EXCEPTION, e);
            }
        }
        channel.close(false);
    }
}
