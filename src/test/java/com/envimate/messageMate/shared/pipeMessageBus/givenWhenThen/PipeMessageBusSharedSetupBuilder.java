package com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen;

import com.envimate.messageMate.qcec.shared.TestEnvironment;

import java.util.LinkedList;
import java.util.List;

import static com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeMessageBusSetupActions.*;

public abstract class PipeMessageBusSharedSetupBuilder<T> implements SetupBuilder<T> {
    protected final TestEnvironment testEnvironment = TestEnvironment.emptyTestEnvironment();
    protected final List<SetupAction<T>> setupActions = new LinkedList<>();

    public PipeMessageBusSharedSetupBuilder<T> withoutASubscriber() {
        return this;
    }

    public PipeMessageBusSharedSetupBuilder<T> withASingleSubscriber() {
        setupActions.add((t, testEnvironment) -> addASingleSubscriber(sutActions(t), testEnvironment));
        return this;
    }

    public PipeMessageBusSharedSetupBuilder<T> withSeveralSubscriber(final int numberOfReceivers) {
        setupActions.add((t, testEnvironment) -> addSeveralSubscriber(sutActions(t), testEnvironment, numberOfReceivers));
        return this;
    }

    public PipeMessageBusSharedSetupBuilder<T> withAFilterThatChangesTheContentOfEveryMessage() {
        setupActions.add((t, testEnvironment) -> addAFilterThatChangesTheContentOfEveryMessage(sutActions(t), testEnvironment));
        return this;
    }

    public PipeMessageBusSharedSetupBuilder<T> withAFilterThatDropsWrongMessages() {
        setupActions.add((t, testEnvironment) -> addAFilterThatDropsWrongMessages(sutActions(t), testEnvironment));
        return this;
    }

    public PipeMessageBusSharedSetupBuilder<T> withAFilterThatReplacesWrongMessages() {
        setupActions.add((t, testEnvironment) -> addAFilterThatReplacesWrongMessages(sutActions(t), testEnvironment));
        return this;
    }

    public PipeMessageBusSharedSetupBuilder<T> withAnInvalidFilterThatDoesNotUseAnyFilterMethods() {
        setupActions.add((t, testEnvironment) -> addAnInvalidFilterThatDoesNotUseAnyFilterMethods(sutActions(t), testEnvironment));
        return this;
    }

    public PipeMessageBusSharedSetupBuilder<T> withTwoFilterOnSpecificPositions() {
        setupActions.add((t, testEnvironment) -> addTwoFilterOnSpecificPositions(sutActions(t), testEnvironment));
        return this;
    }

    public PipeMessageBusSharedSetupBuilder<T> withAFilterAtAnInvalidPosition(final int position) {
        setupActions.add((t, testEnvironment) -> addAFilterAtAnInvalidPosition(sutActions(t), testEnvironment, position));
        return this;
    }

    public PipeMessageBusSharedSetupBuilder<T> withASubscriberThatBlocksWhenAccepting() {
        setupActions.add((t, testEnvironment) -> addASubscriberThatBlocksWhenAccepting(sutActions(t), testEnvironment));
        return this;
    }

    public PipeMessageBusSharedSetupBuilder<T> withAnErrorAcceptingSubscriber() {
        setupActions.add((t, testEnvironment) -> addAnErrorAcceptingSubscriber(sutActions(t), testEnvironment));
        return this;
    }

    public PipeMessageBusSharedSetupBuilder<T> withAnErrorThrowingSubscriber() {
        setupActions.add((t, testEnvironment) -> addAnErrorThrowingSubscriber(sutActions(t), testEnvironment));
        return this;
    }

    public PipeMessageBusSharedSetupBuilder<T> withSeveralDeliveryInterruptingSubscriber(final int numberOfReceivers) {
        setupActions.add((t, testEnvironment) -> addSeveralDeliveryInterruptingSubscriber(sutActions(t), testEnvironment, numberOfReceivers));
        return this;
    }

    protected abstract PipeMessageBusSutActions sutActions(final T t);
}
