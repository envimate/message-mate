package com.envimate.messageMate.shared.channelMessageBus.givenWhenThen;

import com.envimate.messageMate.channel.Channel;
import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.qcec.shared.TestAction;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.qcec.shared.TestValidation;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.Semaphore;

import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.EXCEPTION;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.SUT;
import static com.envimate.messageMate.shared.channelMessageBus.givenWhenThen.ChannelMessageBusTestProperties.EXECUTION_END_SEMAPHORE;
import static java.util.concurrent.TimeUnit.SECONDS;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class Then<T> {
    private final SetupBuilder<T> setupBuilder;
    private final ActionBuilder<T> actionBuilder;


    public void then(final TestValidationBuilder<T> testValidationBuilder) throws InterruptedException {
        final Setup<T> setup = buildSetup(setupBuilder);

        final TestEnvironment testEnvironment = setup.testEnvironment;
        final T t = setup.t;
        executeTestAction(actionBuilder, t, testEnvironment);
        closeSut(t);

        final TestValidation validation = testValidationBuilder.build();
        validation.validate(testEnvironment);
    }

    private Setup<T> buildSetup(final SetupBuilder<T> setupBuilder) {
        final Setup<T> setup = setupBuilder.build();
        final TestEnvironment testEnvironment = setup.testEnvironment;
        final T t = setup.t;
        final List<SetupAction<T>> setupActions = setup.setupActions;
        try {
            setupActions.forEach(setupAction -> setupAction.execute(t, testEnvironment));
        } catch (final Exception e) {
            testEnvironment.setProperty(EXCEPTION, e);
        }
        testEnvironment.setProperty(SUT, t);
        return setup;
    }

    private void executeTestAction(final ActionBuilder<T> actionBuilder, final T t, final TestEnvironment testEnvironment) {
        final List<TestAction<T>> actions = actionBuilder.build();
        try {
            for (final TestAction<T> testAction : actions) {
                testAction.execute(t, testEnvironment);
            }
        } catch (final Exception e) {
            testEnvironment.setProperty(EXCEPTION, e);
        }
        if (testEnvironment.has(EXECUTION_END_SEMAPHORE)) {
            final Semaphore blockingSemaphoreToReleaseAfterExecution = testEnvironment.getPropertyAsType(EXECUTION_END_SEMAPHORE, Semaphore.class);
            blockingSemaphoreToReleaseAfterExecution.release(1000);
        }
    }

    private void closeSut(final T t) throws InterruptedException {
        final int timeout = 3;
        if (t instanceof Channel) {
            final Channel<?> channel = (Channel) t;
            channel.close(true);
            if (!channel.awaitTermination(timeout, SECONDS)) {
                throw new RuntimeException("Channel did shutdown within timeout interval.");
            }
        }
        if (t instanceof MessageBus) {
            final MessageBus messageBus = (MessageBus) t;
            messageBus.close(true);
            if (!messageBus.awaitTermination(timeout, SECONDS)) {
                throw new RuntimeException("Messagebus did shutdown within timeout interval.");
            }
        }
    }
}
