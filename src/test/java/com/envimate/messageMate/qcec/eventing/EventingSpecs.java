package com.envimate.messageMate.qcec.eventing;

import com.envimate.messageMate.qcec.eventing.givenWhenThen.TestEventBus;
import org.junit.jupiter.api.Test;

import static com.envimate.messageMate.qcec.eventing.givenWhenThen.EventBusActionBuilder.*;
import static com.envimate.messageMate.qcec.eventing.givenWhenThen.EventBusValidationBuilder.*;
import static com.envimate.messageMate.qcec.eventing.givenWhenThen.Given.given;


public interface EventingSpecs {

    @Test
    default void testEventBus_eventsAreReceivedByAllReceivers(final TestEventBus anEventBus) {
        given(anEventBus)
                .when(anEventIsPublishedToSeveralReceiver())
                .then(expectItToReceivedByAll());
    }

    @Test
    default void testEventBus_eventsDoNotHaveToBeReceived(final TestEventBus anEventBus) {
        given(anEventBus)
                .when(anEventIsPublishedToNoOne())
                .then(expectNoException());
    }

    @Test
    default void testEventBus_canUnsubscribe(final TestEventBus anEventBus) {
        given(anEventBus)
                .when(anReceiverUnsubscribes())
                .then(expectTheEventToBeReceivedByAllRemainingSubscribers());
    }

    @Test
    default void testEventBus_informsAboutThrownExceptions(final TestEventBus anEventBus) {
        given(anEventBus)
                .when(anEventIsDeliveredToAnErrorThrowingReceiver())
                .then(expectTheExceptionToBeDeliveredInADeliveryFailedMessage());
    }

}
