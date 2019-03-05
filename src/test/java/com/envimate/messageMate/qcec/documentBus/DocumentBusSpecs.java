package com.envimate.messageMate.qcec.documentBus;

import org.junit.jupiter.api.Test;

import static com.envimate.messageMate.qcec.documentBus.givenWhenThen.DocumentBusActionBuilder.*;
import static com.envimate.messageMate.qcec.documentBus.givenWhenThen.DocumentBusValidationBuilder.*;
import static com.envimate.messageMate.qcec.documentBus.givenWhenThen.Given.given;
import static com.envimate.messageMate.qcec.documentBus.givenWhenThen.TestDocumentBusBuilder.aDocumentBus;

//Most of the Query/Constraint/Event specific stuff is tested in the respective Specs
public class DocumentBusSpecs {

    //queries
    @Test
    public void testDocumentBus_canUnsubscribeQueriesAfterASpecificEventIsReceived() {
        given(aDocumentBus()
                .withSeveralQuerySubscriberThatUnsubscribeAfterASpecificEventWasReceived())
                .when(aQueryTheEventAndASecondQueryAreSend())
                .then(expectNoQueryAfterTheEventToHaveAResult());
    }

    @Test
    public void testDocumentBus_subscriberCanFilterForQueries() {
        given(aDocumentBus()
                .withSeveralSubscriberThatOnlyTakeSpecificQueries())
                .when(oneQueryOfInterestAndSeveralOtherAreSend())
                .then(expectOnlyQueriesOfInterestToBeReceived());
    }

    //constraints
    @Test
    public void testDocumentBus_canUnsubscribeConstraintsAfterASpecificEventIsReceived() {
        given(aDocumentBus()
                .withSeveralConstraintSubscriberThatUnsubscribeAfterASpecificEventWasReceived())
                .when(aConstraintTheEventAndASecondConstraintAreSend())
                .then(expectOnlyTheFirstConstraintToBeReceived());
    }

    @Test
    public void testDocumentBus_subscriberCanFilterForConstraints() {
        given(aDocumentBus()
                .withSeveralSubscriberThatOnlyTakeSpecificConstraints())
                .when(oneConstraintOfInterestAndSeveralOtherAreSend())
                .then(expectOnlyConstraintsOfInterestToBeReceived());
    }

    //events
    @Test
    public void testDocumentBus_canUnsubscribeEventsAfterASpecificEventIsReceived() {
        given(aDocumentBus()
                .withSeveralEventSubscriberThatUnsubscribeAfterASpecificEventWasReceived())
                .when(anEventThenTheUnsubscribeEventAndAThirdEventAreSend())
                .then(expectOnlyTheFirstEventToBeReceived());
    }

    @Test
    public void testDocumentBus_subscriberCanFilterForEvents() {
        given(aDocumentBus()
                .withSeveralSubscriberThatOnlyTakeSpecificEvents())
                .when(oneEventOfInterestAndSeveralOtherAreSend())
                .then(expectOnlyEventsOfInterestToBeReceived());
    }


    @Test
    public void testDocumentBus_theConsumerIsCalledForTheUnsubscribingEventBeforeItIsRemoved() {
        given(aDocumentBus()
                .withASubscriberForTheUnscubscribeEvent())
                .when(anEventIsSend())
                .then(expectTheConsumerToBeStillExecuted());
    }
}
