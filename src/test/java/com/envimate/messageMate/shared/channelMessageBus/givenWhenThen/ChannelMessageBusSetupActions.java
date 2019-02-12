package com.envimate.messageMate.shared.channelMessageBus.givenWhenThen;

import com.envimate.messageMate.error.DeliveryFailedMessage;
import com.envimate.messageMate.filtering.Filter;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.shared.subscriber.ErrorThrowingTestSubscriber;
import com.envimate.messageMate.shared.subscriber.SimpleTestSubscriber;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import com.envimate.messageMate.shared.testMessages.TestMessageOfInterest;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.Semaphore;

import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.EXPECTED_RECEIVERS;
import static com.envimate.messageMate.shared.channelMessageBus.givenWhenThen.ChannelMessageBusTestProperties.*;
import static com.envimate.messageMate.shared.channelMessageBus.givenWhenThen.TestFilter.*;
import static com.envimate.messageMate.shared.subscriber.BlockingTestSubscriber.blockingTestSubscriber;
import static com.envimate.messageMate.shared.subscriber.ErrorThrowingTestSubscriber.errorThrowingTestSubscriber;
import static com.envimate.messageMate.shared.subscriber.SimpleTestSubscriber.interruptingSubscriber;
import static com.envimate.messageMate.shared.subscriber.SimpleTestSubscriber.testSubscriber;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class ChannelMessageBusSetupActions {

    public static void addASingleSubscriber(final ChannelMessageBusSutActions sutActions, final TestEnvironment testEnvironment) {
        final SimpleTestSubscriber<TestMessageOfInterest> subscriber = testSubscriber();
        sutActions.subscribe(TestMessageOfInterest.class, subscriber);
        testEnvironment.addToListProperty(EXPECTED_RECEIVERS, subscriber);
        testEnvironment.addToListProperty(INITIAL_SUBSCRIBER, subscriber);
    }

    public static void addSeveralSubscriber(final ChannelMessageBusSutActions sutActions, final TestEnvironment testEnvironment, final int numberOfReceivers) {
        for (int i = 0; i < numberOfReceivers; i++) {
            addASingleSubscriber(sutActions, testEnvironment);
        }
    }

    public static void addAFilterThatChangesTheContentOfEveryMessage(final ChannelMessageBusSutActions sutActions, final TestEnvironment testEnvironment) {
        testEnvironment.setProperty(EXPECTED_CHANGED_CONTENT, TestFilter.CHANGED_CONTENT);
        final Filter<TestMessage> filter = aContentChangingFilter();
        sutActions.addFilter(filter);
    }

    public static void addAFilterThatDropsWrongMessages(final ChannelMessageBusSutActions sutActions, final TestEnvironment testEnvironment) {
        final Filter<Object> filter = aMessageDroppingFilter();
        sutActions.addFilter(filter);
    }

    public static void addAFilterThatReplacesWrongMessages(final ChannelMessageBusSutActions sutActions, final TestEnvironment testEnvironment) {
        testEnvironment.setProperty(EXECUTE_MESSAGE_BUS_IN_OWN_THREAD, true);
        final Filter<Object> filter = aMessageReplacingFilter();
        sutActions.addFilter(filter);
    }

    public static void addAnInvalidFilterThatDoesNotUseAnyFilterMethods(final ChannelMessageBusSutActions sutActions, final TestEnvironment testEnvironment) {
        final Filter<Object> filter = aMessageFilterThatDoesNotCallAnyMethod();
        sutActions.addFilter(filter);
    }

    public static void addTwoFilterOnSpecificPositions(final ChannelMessageBusSutActions sutActions, final TestEnvironment testEnvironment) {
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

    public static void addAFilterAtAnInvalidPosition(final ChannelMessageBusSutActions sutActions, final TestEnvironment testEnvironment, final int position) {
        sutActions.addFilter(null, position);
    }

    public static void addASubscriberThatBlocksWhenAccepting(final ChannelMessageBusSutActions sutActions, final TestEnvironment testEnvironment) {
        final Semaphore semaphore = new Semaphore(0);
        sutActions.subscribe(TestMessageOfInterest.class, blockingTestSubscriber(semaphore));
        testEnvironment.setProperty(EXECUTION_END_SEMAPHORE, semaphore);
    }

    public static void addAnErrorAcceptingSubscriber(final ChannelMessageBusSutActions sutActions, final TestEnvironment testEnvironment) {
        @SuppressWarnings("rawtypes")
        final SimpleTestSubscriber<DeliveryFailedMessage> errorSubscriber = testSubscriber();
        sutActions.subscribe(DeliveryFailedMessage.class, errorSubscriber);
        testEnvironment.setProperty(ERROR_SUBSCRIBER, errorSubscriber);
    }

    public static void addAnErrorThrowingSubscriber(final ChannelMessageBusSutActions sutActions, final TestEnvironment testEnvironment) {
        final ErrorThrowingTestSubscriber<TestMessageOfInterest> subscriber = errorThrowingTestSubscriber();
        sutActions.subscribe(TestMessageOfInterest.class, subscriber);
    }

    public static void addSeveralDeliveryInterruptingSubscriber(final ChannelMessageBusSutActions sutActions, final TestEnvironment testEnvironment, final int numberOfReceivers) {
        for (int i = 0; i < numberOfReceivers; i++) {
            final SimpleTestSubscriber<TestMessageOfInterest> subscriber = interruptingSubscriber();
            sutActions.subscribe(TestMessageOfInterest.class, subscriber);
            testEnvironment.addToListProperty(POTENTIAL_RECEIVERS, subscriber);
        }
    }

}
