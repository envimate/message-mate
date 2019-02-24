package com.envimate.messageMate.messageBus.givenWhenThen;

import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.qcec.shared.TestAction;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.qcec.shared.TestValidation;
import com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.SetupAction;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.Semaphore;

import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.EXCEPTION;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.SUT;
import static com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeMessageBusTestProperties.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class Then {
    private final MessageBusSetupBuilder setupBuilder;
    private final MessageBusActionBuilder actionBuilder;


    public void then(final MessageBusValidationBuilder testValidationBuilder) throws InterruptedException {
        final MessageBusSetup setup = buildSetup(setupBuilder);

        final TestEnvironment testEnvironment = setup.testEnvironment;
        final MessageBus messageBus = setup.messageBus;
        executeTestAction(actionBuilder, messageBus, testEnvironment);

        final TestValidation validation = testValidationBuilder.build();
        validation.validate(testEnvironment);
        closeSut(messageBus); //TODO: sehr gefährlich -> definitiv remove oder move nach validation
    }

    private MessageBusSetup buildSetup(final MessageBusSetupBuilder setupBuilder) {
        final MessageBusSetup setup = setupBuilder.build();
        final TestEnvironment testEnvironment = setup.testEnvironment;
        final MessageBus messageBus = setup.messageBus;
        final List<SetupAction<MessageBus>> setupActions = setup.setupActions;
        try {
            for (final SetupAction<MessageBus> setupAction : setupActions) {
                setupAction.execute(messageBus, testEnvironment);
            }
        } catch (final Exception e) {
            testEnvironment.setProperty(EXCEPTION, e);
        }
        testEnvironment.setProperty(SUT, messageBus);
        return setup;
    }

    private void executeTestAction(final MessageBusActionBuilder actionBuilder, final MessageBus messageBus, final TestEnvironment testEnvironment) {
        final List<TestAction<MessageBus>> actions = actionBuilder.build();
        try {
            for (final TestAction<MessageBus> testAction : actions) {
                testAction.execute(messageBus, testEnvironment);
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
        if (testEnvironment.has(EXECUTION_END_SEMAPHORE)) {
            final Semaphore blockingSemaphoreToReleaseAfterExecution = testEnvironment.getPropertyAsType(EXECUTION_END_SEMAPHORE, Semaphore.class);
            blockingSemaphoreToReleaseAfterExecution.release(1000);
        }

        //Some Tests need a minimal sleep here
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void closeSut(final MessageBus messageBus) throws InterruptedException {
        final int timeout = 3;
        messageBus.close(true);
        if (!messageBus.awaitTermination(timeout, SECONDS)) {
            throw new RuntimeException("Messagebus did shutdown within timeout interval.");
        }
    }
}
