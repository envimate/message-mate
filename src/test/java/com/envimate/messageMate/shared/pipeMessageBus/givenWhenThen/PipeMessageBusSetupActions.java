package com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen;

import com.envimate.messageMate.error.DeliveryFailedMessage;
import com.envimate.messageMate.filtering.Filter;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.shared.subscriber.*;
import com.envimate.messageMate.shared.testMessages.TestMessageOfInterest;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.Semaphore;

import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.EXPECTED_RECEIVERS;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.EXPECTED_RESULT;
import static com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeMessageBusTestProperties.*;
import static com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.TestFilter.*;
import static com.envimate.messageMate.shared.subscriber.BlockingTestSubscriber.blockingTestSubscriber;
import static com.envimate.messageMate.shared.subscriber.ErrorThrowingTestSubscriber.errorThrowingTestSubscriber;
import static com.envimate.messageMate.shared.subscriber.SimpleTestSubscriber.deliveryPreemptingSubscriber;
import static com.envimate.messageMate.shared.subscriber.SimpleTestSubscriber.testSubscriber;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class PipeMessageBusSetupActions {

    public static void addASingleSubscriber(final PipeMessageBusSutActions sutActions, final TestEnvironment testEnvironment) {
        addASingleSubscriber(sutActions, testEnvironment, TestMessageOfInterest.class);
    }

    public static <T> void addASingleSubscriber(final PipeMessageBusSutActions sutActions, final TestEnvironment testEnvironment,
                                                final Class<T> clazz) {
        final SimpleTestSubscriber<T> subscriber = testSubscriber();
        sutActions.subscribe(clazz, subscriber);
        testEnvironment.addToListProperty(EXPECTED_RECEIVERS, subscriber);
        testEnvironment.addToListProperty(INITIAL_SUBSCRIBER, subscriber);
    }

    public static void addSeveralSubscriber(final PipeMessageBusSutActions sutActions, final TestEnvironment testEnvironment, final int numberOfReceivers) {
        for (int i = 0; i < numberOfReceivers; i++) {
            addASingleSubscriber(sutActions, testEnvironment);
        }
    }

    public static void addAFilterThatChangesTheContentOfEveryMessage(final PipeMessageBusSutActions sutActions, final TestEnvironment testEnvironment) {
        testEnvironment.setProperty(EXPECTED_CHANGED_CONTENT, TestFilter.CHANGED_CONTENT);
        final Filter<TestMessageOfInterest> filter = aContentChangingFilter();
        sutActions.addFilter(filter);
    }

    public static void addAFilterThatDropsMessages(final PipeMessageBusSutActions sutActions, final TestEnvironment testEnvironment) {
        final Filter<Object> filter = aMessageDroppingFilter();
        sutActions.addFilter(filter);
    }

    public static void addAnInvalidFilterThatDoesNotUseAnyFilterMethods(final PipeMessageBusSutActions sutActions, final TestEnvironment testEnvironment) {
        final Filter<Object> filter = aMessageFilterThatDoesNotCallAnyMethod();
        sutActions.addFilter(filter);
    }

    public static void addTwoFilterOnSpecificPositions(final PipeMessageBusSutActions sutActions, final TestEnvironment testEnvironment) {
        final String firstAppend = "1nd";
        final String secondAppend = "2nd";
        testEnvironment.setProperty(EXPECTED_CHANGED_CONTENT, TestMessageOfInterest.CONTENT + firstAppend + secondAppend);
        final Filter<Object> filter1 = aContentAppendingFilter(secondAppend);
        sutActions.addFilter(filter1, 0);
        testEnvironment.addToListProperty(EXPECTED_FILTER, filter1);
        final Filter<Object> filter2 = aContentAppendingFilter(firstAppend);
        sutActions.addFilter(filter2, 0);
        testEnvironment.addToListProperty(EXPECTED_FILTER, filter2);
    }

    public static void addAFilterAtAnInvalidPosition(final PipeMessageBusSutActions sutActions, final TestEnvironment testEnvironment, final int position) {
        sutActions.addFilter(null, position);
    }

    public static void addAFilterThatThrowsExceptions(final PipeMessageBusSutActions sutActions, final TestEnvironment testEnvironment) {
        final TestException exception = new TestException();
        final Filter<Object> filter = anErrorThrowingFilter(exception);
        sutActions.addFilter(filter);
        testEnvironment.setProperty(EXPECTED_RESULT, exception);
    }

    public static void addASubscriberThatBlocksWhenAccepting(final PipeMessageBusSutActions sutActions, final TestEnvironment testEnvironment) {
        final Semaphore semaphore = new Semaphore(0);
        final BlockingTestSubscriber<TestMessageOfInterest> subscriber = blockingTestSubscriber(semaphore);
        sutActions.subscribe(TestMessageOfInterest.class, subscriber);
        testEnvironment.addToListProperty(EXPECTED_RECEIVERS, subscriber);
        testEnvironment.setProperty(EXECUTION_END_SEMAPHORE, semaphore);
    }

    public static void addAnErrorAcceptingSubscriber(final PipeMessageBusSutActions sutActions, final TestEnvironment testEnvironment) {
        @SuppressWarnings("rawtypes")
        final SimpleTestSubscriber<DeliveryFailedMessage> errorSubscriber = testSubscriber();
        sutActions.subscribe(DeliveryFailedMessage.class, errorSubscriber);
        testEnvironment.setProperty(ERROR_SUBSCRIBER, errorSubscriber);
    }

    public static TestSubscriber<TestMessageOfInterest> addAnErrorThrowingSubscriber(final PipeMessageBusSutActions sutActions, final TestEnvironment testEnvironment) {
        final ErrorThrowingTestSubscriber<TestMessageOfInterest> subscriber = errorThrowingTestSubscriber();
        sutActions.subscribe(TestMessageOfInterest.class, subscriber);
        return subscriber;
    }

    public static void addSeveralDeliveryInterruptingSubscriber(final PipeMessageBusSutActions sutActions, final TestEnvironment testEnvironment, final int numberOfReceivers) {
        for (int i = 0; i < numberOfReceivers; i++) {
            final SimpleTestSubscriber<TestMessageOfInterest> subscriber = deliveryPreemptingSubscriber();
            sutActions.subscribe(TestMessageOfInterest.class, subscriber);
            testEnvironment.addToListProperty(POTENTIAL_RECEIVERS, subscriber);
        }
    }

}
