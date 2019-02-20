package com.envimate.messageMate.pipe.givenWhenThen;

import com.envimate.messageMate.pipe.Pipe;
import com.envimate.messageMate.pipe.PipeBuilder;
import com.envimate.messageMate.pipe.config.PipeTestConfig;
import com.envimate.messageMate.pipe.error.PipeErrorHandler;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeMessageBusSutActions;
import com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.SetupAction;
import com.envimate.messageMate.shared.subscriber.TestException;
import com.envimate.messageMate.shared.testMessages.TestMessage;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import static com.envimate.messageMate.pipe.givenWhenThen.PipeTestActions.pipeTestActions;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.EXCEPTION;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.RESULT;
import static com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeMessageBusSetupActions.*;

public class PipeSetupBuilder {
    private final TestEnvironment testEnvironment = TestEnvironment.emptyTestEnvironment();
    private final List<SetupAction<Pipe<TestMessage>>> setupActions = new LinkedList<>();
    private final PipeBuilder<TestMessage> pipeBuilder = PipeBuilder.aPipe();

    public static PipeSetupBuilder aConfiguredPipe(final PipeTestConfig testConfig) {
        return new PipeSetupBuilder()
                .configuredWith(testConfig);
    }

    public PipeSetupBuilder withoutASubscriber() {
        return this;
    }

    public PipeSetupBuilder withASingleSubscriber() {
        setupActions.add((t, testEnvironment) -> addASingleSubscriber(sutActions(t), testEnvironment));
        return this;
    }

    public PipeSetupBuilder withSeveralSubscriber(final int numberOfReceivers) {
        setupActions.add((t, testEnvironment) -> addSeveralSubscriber(sutActions(t), testEnvironment, numberOfReceivers));
        return this;
    }

    public PipeSetupBuilder withASubscriberThatBlocksWhenAccepting() {
        setupActions.add((t, testEnvironment) -> addASubscriberThatBlocksWhenAccepting(sutActions(t), testEnvironment));
        return this;
    }

    public PipeSetupBuilder withSeveralDeliveryInterruptingSubscriber(final int numberOfReceivers) {
        setupActions.add((t, testEnvironment) -> addSeveralDeliveryInterruptingSubscriber(sutActions(t), testEnvironment, numberOfReceivers));
        return this;
    }

    public PipeSetupBuilder withACustomErrorHandler() {
        pipeBuilder.withErrorHandler(errorHandler((e) -> testEnvironment.setProperty(RESULT, e)));
        return this;
    }

    public PipeSetupBuilder withACustomErrorHandlerThatSuppressException() {
        pipeBuilder.withErrorHandler(errorHandler((e) -> testEnvironment.setProperty(EXCEPTION, e), TestException.class));
        return this;
    }


    public PipeSetupBuilder causingErrorsWhenDelivering() {
        pipeBuilder.withErrorHandler(errorHandler((e) -> {
        }));
        return this;
    }

    private PipeSetupBuilder configuredWith(final PipeTestConfig testConfig) {
        pipeBuilder.ofType(testConfig.pipeType)
                .withAsynchronousConfiguration(testConfig.asynchronousConfiguration);
        return this;
    }

    public PipeSetup build() {
        final Pipe<TestMessage> pipe = pipeBuilder.build();
        return PipeSetup.setup(pipe, testEnvironment, setupActions);
    }

    private PipeMessageBusSutActions sutActions(final Pipe<TestMessage> pipe) {
        return pipeTestActions(pipe);
    }

    private PipeErrorHandler<TestMessage> errorHandler(final Consumer<Exception> exceptionHandler,
                                                       final Class<?>... ignoredExceptionsClasses) {
        return new PipeErrorHandler<TestMessage>() {
            @Override
            public boolean shouldErrorBeHandledAndDeliveryAborted(final TestMessage message, final Exception e) {
                for (final Class<?> ignoredExceptionClass : ignoredExceptionsClasses) {
                    if (e.getClass().equals(ignoredExceptionClass)) {
                        return false;
                    }
                }
                return true;
            }

            @Override
            public void handleException(final TestMessage message, final Exception e) {
                exceptionHandler.accept(e);
            }
        };
    }
}
