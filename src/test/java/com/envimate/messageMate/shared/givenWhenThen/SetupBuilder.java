package com.envimate.messageMate.shared.givenWhenThen;

import com.envimate.messageMate.error.DeliveryFailedMessage;
import com.envimate.messageMate.filtering.Filter;
import com.envimate.messageMate.shared.context.TestExecutionContext;
import com.envimate.messageMate.shared.subscriber.ErrorThrowingTestSubscriber;
import com.envimate.messageMate.shared.subscriber.SimpleTestSubscriber;
import com.envimate.messageMate.shared.testMessages.TestMessageOfInterest;
import com.envimate.messageMate.subscribing.Subscriber;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

import static com.envimate.messageMate.shared.context.TestExecutionProperty.*;
import static com.envimate.messageMate.shared.subscriber.BlockingTestSubscriber.blockingTestSubscriber;
import static com.envimate.messageMate.shared.subscriber.ErrorThrowingTestSubscriber.errorThrowingTestSubscriber;
import static com.envimate.messageMate.shared.subscriber.SimpleTestSubscriber.testSubscriber;
import static lombok.AccessLevel.PROTECTED;

@RequiredArgsConstructor(access = PROTECTED)
public abstract class SetupBuilder<T> {

    protected final TestExecutionContext executionContext = TestExecutionContext.emptyExecutionContext();
    protected final List<SetupAction<T>> setupActions = new LinkedList<>();

    public SetupBuilder<T> withASingleSubscriber() {
        final SetupBuilder<T> that = this;
        setupActions.add((t, executionContext) -> {
            final SimpleTestSubscriber<TestMessageOfInterest> subscriber = testSubscriber();
            that.subscribe(t, TestMessageOfInterest.class, subscriber);
            executionContext.addToListProperty(EXPECTED_RECEIVERS, subscriber);
            executionContext.addToListProperty(INITIAL_SUBSCRIBER, subscriber);
        });
        return this;
    }

    public SetupBuilder<T> withSeveralSubscriber(final int numberOfReceivers) {
        setupActions.add((t, executionContext) -> {
            for (int i = 0; i < numberOfReceivers; i++) {
                final SimpleTestSubscriber<TestMessageOfInterest> subscriber = testSubscriber();
                subscribe(t, TestMessageOfInterest.class, subscriber);
                executionContext.addToListProperty(EXPECTED_RECEIVERS, subscriber);
                executionContext.addToListProperty(INITIAL_SUBSCRIBER, subscriber);
            }
        });
        return this;
    }

    public SetupBuilder<T> withAFilterThatChangesTheContentOfEveryMessage() {
        final SetupBuilder<T> that = this;
        setupActions.add((t, executionContext) -> {
            executionContext.setProperty(EXPECTED_CHANGED_CONTENT, TestFilter.CHANGED_CONTENT);
            that.addFilterThatChangesTheContent(t);
        });
        return this;
    }

    protected abstract void addFilterThatChangesTheContent(T t);

    public SetupBuilder<T> withAFilterThatDropsWrongMessages() {
        final SetupBuilder<T> that = this;
        setupActions.add((t, executionContext) -> that.addFilterThatDropsMessages(t));
        return this;
    }

    protected abstract void addFilterThatDropsMessages(T t);

    public SetupBuilder<T> withAFilterThatReplacesWrongMessages() {
        final SetupBuilder<T> that = this;
        setupActions.add((t, executionContext) -> {
            executionContext.setProperty(EXECUTE_MESSAGE_BUS_IN_OWN_THREAD, true);
            that.addFilterThatReplacesWrongMessage(t);
        });
        return this;
    }

    protected abstract void addFilterThatReplacesWrongMessage(T t);

    public SetupBuilder<T> withAnInvalidFilterThatDoesNotUseAnyFilterMethods() {
        final SetupBuilder<T> that = this;
        setupActions.add((t, executionContext) -> that.addFilterThatDoesNotCallAnyFilterMethod(t));
        return this;
    }

    public SetupBuilder<T> withTwoFilterOnSpecificPositions() {
        final SetupBuilder<T> that = this;
        final String firstAppend = "1nd";
        final String secondAppend = "2nd";
        executionContext.setProperty(EXPECTED_CHANGED_CONTENT, TestMessageOfInterest.CONTENT + firstAppend + secondAppend);
        setupActions.add((t, executionContext) -> {
            final Filter<?> filter = that.addFilterAtPositionThatAppendsTheContent(secondAppend, 0, t);
            executionContext.addToListProperty(EXPECTED_FILTER, filter);
        });
        setupActions.add((t, executionContext) -> {
            final Filter<?> filter = that.addFilterAtPositionThatAppendsTheContent(firstAppend, 0, t);
            executionContext.addToListProperty(EXPECTED_FILTER, filter);
        });
        return this;
    }

    public SetupBuilder<T> withAFilterAtAnInvalidPosition(final int position) {
        final SetupBuilder<T> that = this;
        setupActions.add((t, executionContext) -> that.addFilterAtPositionThatAppendsTheContent(null, position, t));
        return this;
    }

    protected abstract void addFilterThatDoesNotCallAnyFilterMethod(T t);

    protected abstract Filter<?> addFilterAtPositionThatAppendsTheContent(String contentToAppend, int position, T t);

    public SetupBuilder<T> withASubscriberThatBlocksWhenAccepting() {
        final SetupBuilder<T> that = this;
        setupActions.add((t, executionContext) -> {
            final Semaphore semaphore = new Semaphore(0);
            that.subscribe(t, TestMessageOfInterest.class, blockingTestSubscriber(semaphore));
            executionContext.setProperty(EXECUTION_END_SEMAPHORE, semaphore);
        });
        return this;
    }

    public SetupBuilder<T> withAnErrorAcceptingSubscriber() {
        final SetupBuilder<T> that = this;
        setupActions.add((t, executionContext) -> {
            @SuppressWarnings("rawtypes")
            final SimpleTestSubscriber<DeliveryFailedMessage> errorSubscriber = testSubscriber();
            that.subscribe(t, DeliveryFailedMessage.class, errorSubscriber);
            executionContext.setProperty(ERROR_SUBSCRIBER, errorSubscriber);
        });
        return this;
    }

    public SetupBuilder<T> withAnErrorThrowingSubscriber() {
        final SetupBuilder<T> that = this;
        setupActions.add((t, executionContext) -> {
            final ErrorThrowingTestSubscriber<TestMessageOfInterest> subscriber = errorThrowingTestSubscriber();
            that.subscribe(t, TestMessageOfInterest.class, subscriber);
        });
        return this;
    }

    public SetupBuilder<T> withSeveralDeliveryInterruptingSubscriber(final int numberOfReceivers) {
        final SetupBuilder<T> that = this;
        setupActions.add((t, executionContext) -> {
            for (int i = 0; i < numberOfReceivers; i++) {
                final SimpleTestSubscriber<TestMessageOfInterest> subscriber = SimpleTestSubscriber.interruptingSubscriber();
                that.subscribe(t, TestMessageOfInterest.class, subscriber);
                executionContext.addToListProperty(POTENTIAL_RECEIVERS, subscriber);
            }
        });
        return this;
    }

    public SetupBuilder<T> withoutASubscriber() {
        return this;
    }


    public abstract Setup<T> build();

    public abstract <R> void subscribe(T t, Class<R> rClass, Subscriber<R> subscriber);
}
