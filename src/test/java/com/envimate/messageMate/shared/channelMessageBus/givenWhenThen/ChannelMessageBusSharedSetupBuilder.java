package com.envimate.messageMate.shared.channelMessageBus.givenWhenThen;

import com.envimate.messageMate.qcec.shared.TestEnvironment;

import java.util.LinkedList;
import java.util.List;

import static com.envimate.messageMate.shared.channelMessageBus.givenWhenThen.ChannelMessageBusSetupActions.*;

public abstract class ChannelMessageBusSharedSetupBuilder<T> implements SetupBuilder<T> {
    protected final TestEnvironment testEnvironment = TestEnvironment.emptyTestEnvironment();
    protected final List<SetupAction<T>> setupActions = new LinkedList<>();

    public ChannelMessageBusSharedSetupBuilder<T> withoutASubscriber() {
        return this;
    }

    public ChannelMessageBusSharedSetupBuilder<T> withASingleSubscriber() {
        setupActions.add((t, testEnvironment) -> addASingleSubscriber(sutActions(t), testEnvironment));
        return this;
    }

    public ChannelMessageBusSharedSetupBuilder<T> withSeveralSubscriber(final int numberOfReceivers) {
        setupActions.add((t, testEnvironment) -> addSeveralSubscriber(sutActions(t), testEnvironment, numberOfReceivers));
        return this;
    }

    public ChannelMessageBusSharedSetupBuilder<T> withAFilterThatChangesTheContentOfEveryMessage() {
        setupActions.add((t, testEnvironment) -> addAFilterThatChangesTheContentOfEveryMessage(sutActions(t), testEnvironment));
        return this;
    }

    public ChannelMessageBusSharedSetupBuilder<T> withAFilterThatDropsWrongMessages() {
        setupActions.add((t, testEnvironment) -> addAFilterThatDropsWrongMessages(sutActions(t), testEnvironment));
        return this;
    }

    public ChannelMessageBusSharedSetupBuilder<T> withAFilterThatReplacesWrongMessages() {
        setupActions.add((t, testEnvironment) -> addAFilterThatReplacesWrongMessages(sutActions(t), testEnvironment));
        return this;
    }

    public ChannelMessageBusSharedSetupBuilder<T> withAnInvalidFilterThatDoesNotUseAnyFilterMethods() {
        setupActions.add((t, testEnvironment) -> addAnInvalidFilterThatDoesNotUseAnyFilterMethods(sutActions(t), testEnvironment));
        return this;
    }

    public ChannelMessageBusSharedSetupBuilder<T> withTwoFilterOnSpecificPositions() {
        setupActions.add((t, testEnvironment) -> addTwoFilterOnSpecificPositions(sutActions(t), testEnvironment));
        return this;
    }

    public ChannelMessageBusSharedSetupBuilder<T> withAFilterAtAnInvalidPosition(final int position) {
        setupActions.add((t, testEnvironment) -> addAFilterAtAnInvalidPosition(sutActions(t), testEnvironment, position));
        return this;
    }

    public ChannelMessageBusSharedSetupBuilder<T> withASubscriberThatBlocksWhenAccepting() {
        setupActions.add((t, testEnvironment) -> addASubscriberThatBlocksWhenAccepting(sutActions(t), testEnvironment));
        return this;
    }

    public ChannelMessageBusSharedSetupBuilder<T> withAnErrorAcceptingSubscriber() {
        setupActions.add((t, testEnvironment) -> addAnErrorAcceptingSubscriber(sutActions(t), testEnvironment));
        return this;
    }

    public ChannelMessageBusSharedSetupBuilder<T> withAnErrorThrowingSubscriber() {
        setupActions.add((t, testEnvironment) -> addAnErrorThrowingSubscriber(sutActions(t), testEnvironment));
        return this;
    }

    public ChannelMessageBusSharedSetupBuilder<T> withSeveralDeliveryInterruptingSubscriber(final int numberOfReceivers) {
        setupActions.add((t, testEnvironment) -> addSeveralDeliveryInterruptingSubscriber(sutActions(t), testEnvironment, numberOfReceivers));
        return this;
    }

    protected abstract ChannelMessageBusSutActions sutActions(final T t);
}
