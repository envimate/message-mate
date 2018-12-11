package com.envimate.messageMate.messageBus;


import com.envimate.messageMate.messageBus.config.MessageBusTestConfig;
import com.envimate.messageMate.messages.ExceptionInSubscriberException;
import com.envimate.messageMate.messages.NoSuitableSubscriberException;
import com.envimate.messageMate.shared.testMessages.InvalidTestMessage;
import com.envimate.messageMate.shared.testMessages.TestMessageOfInterest;
import org.junit.jupiter.api.Test;

import static com.envimate.messageMate.messageBus.givenWhenThen.MessageBusActionBuilder.*;
import static com.envimate.messageMate.messageBus.givenWhenThen.MessageBusSetupBuilder.aMessageBus;
import static com.envimate.messageMate.messageBus.givenWhenThen.MessageBusValidationBuilder.*;
import static com.envimate.messageMate.shared.givenWhenThen.Given.given;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/*
TODO:
- TestMessageBus injected instead of MessageBusTestConfig + configuredWith Combo
- instead of waits: TestExecutionContext contains information, if there are asynchronous parts and adapt the then validations
 */

public interface MessageBusSpecs {

    //Send and subscribe
    @Test
    default void testMessageBus_canSendAndReceiveASingleMessage(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aMessageBus()
                .configuredWith(messageBusTestConfig)
                .withASingleSubscriber())
                .when(aSingleMessageIsSend())
                .then(expectTheMessageToBeReceived());
    }

    @Test
    default void testMessageBus_canSendAndReceiveSeveralMessagesWithSeveralSubscriber(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aMessageBus()
                .configuredWith(messageBusTestConfig)
                .withSeveralSubscriber(5))
                .when(severalMessagesAreSend(10)
                        .andThen(aShortWaitIsDone(5, MILLISECONDS)))
                .then(expectAllMessagesToBeReceivedByAllSubscribers());
    }

    @Test
    default void testMessageBus_canSendAndReceiveMessagesAsynchronously(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aMessageBus()
                .configuredWith(messageBusTestConfig)
                .withSeveralSubscriber(5))
                .when(severalMessagesAreSendAsynchronously(5, 10)
                        .andThen(aShortWaitIsDone(5, MILLISECONDS)))
                .then(expectAllMessagesToBeReceivedByAllSubscribers());
    }

    /*
    Better;
    @Test
    default void testMessageBus_canSendAndReceiveMessagesAsynchronously(final TestMessageBus aMessageBus) throws Exception {
        given(aMessageBus
                .withSeveralSubscriber(5))
                .when(severalMessagesAreSendAsynchronously(5, 10)
                        .andThen(aShortWaitIsDone(5, MILLISECONDS)))
                .then(expectAllMessagesToBeReceivedByAllSubscribers());
    }
     */

    //unsubscribe
    @Test
    default void testMessageBus_canUnsubscribe(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aMessageBus()
                .configuredWith(messageBusTestConfig)
                .withSeveralSubscriber(5))
                .when(oneSubscriberUnsubscribes())
                .then(expectAllRemainingSubscribersToStillBeSubscribed());
    }

    @Test
    default void testMessageBus_canUnsubscribeTwoSubscribers(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aMessageBus()
                .configuredWith(messageBusTestConfig)
                .withSeveralSubscriber(5))
                .when(oneSubscriberUnsubscribes()
                        .andThen(oneSubscriberUnsubscribes()))
                .then(expectAllRemainingSubscribersToStillBeSubscribed());
    }

    @Test
    default void testMessageBus_canUnsubscribeTheSameSubscriberSeveralTimes(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aMessageBus()
                .configuredWith(messageBusTestConfig)
                .withSeveralSubscriber(5))
                .when(oneSubscriberUnsubscribesSeveralTimes(2))
                .then(expectAllRemainingSubscribersToStillBeSubscribed());
    }

    //filter
    @Test
    default void testMessageBus_allowsFiltersToChangeMessages(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aMessageBus()
                .configuredWith(messageBusTestConfig)
                .withSeveralSubscriber(3)
                .withAFilterThatChangesTheContentOfEveryMessage())
                .when(severalMessagesAreSend(10))
                .then(expectAllMessagesToHaveTheContentChanged());
    }


    @Test
    default void testMessageBus_allowsFiltersToDropMessages(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aMessageBus()
                .configuredWith(messageBusTestConfig)
                .withSeveralSubscriber(3)
                .withAFilterThatDropsWrongMessages())
                .when(halfValidAndInvalidMessagesAreSendAsynchronously(3, 10))
                .then(expectAllMessagesToBeReceivedByAllSubscribers());
    }


    @Test
    default void testMessageBus_allowsFiltersToReplaceMessages(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aMessageBus()
                .configuredWith(messageBusTestConfig)
                .withSeveralSubscriber(3)
                .withAFilterThatReplacesWrongMessages())
                .when(severalInvalidMessagesAreSendAsynchronously(3, 10))
                .then(expectOnlyValidMessageToBeReceived());
    }

    @Test
    default void testMessageBus_whenAFilterDoesNotUseAMethod_messageIsDropped(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aMessageBus()
                .configuredWith(messageBusTestConfig)
                .withSeveralSubscriber(3)
                .withAnInvalidFilterThatDoesNotUseAnyFilterMethods())
                .when(severalInvalidMessagesAreSendAsynchronously(3, 10))
                .then(expectNoMessagesToBeDelivered());
    }

    //messageStatistics
    @Test
    default void testMessageBus_whenAFilterDoesNotUseAMethod_theMessageIsMarkedAsForgotten(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aMessageBus()
                .configuredWith(messageBusTestConfig)
                .withSeveralSubscriber(3)
                .withAnInvalidFilterThatDoesNotUseAnyFilterMethods())
                .when(aSingleMessageIsSend()
                        .andThen(theNumberOfForgottenMessagesIsQueried()))
                .then(expectResultToBe(1));
    }

    @Test
    default void testMessageBus_returnsCorrectNumberOfAcceptedMessages(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aMessageBus()
                .configuredWith(messageBusTestConfig)
                .withASingleSubscriber())
                .when(severalMessagesAreSendAsynchronously(3, 5)
                        .andThen(theNumberOfAcceptedMessagesIsQueried()))
                .then(expectResultToBe(15));
    }

    @Test
    default void testMessageBus_returnsCorrectNumberOfSuccessfulMessages(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aMessageBus()
                .configuredWith(messageBusTestConfig)
                .withASingleSubscriber())
                .when(severalMessagesAreSendAsynchronously(3, 5)
                        .andThen(aShortWaitIsDone(10, MILLISECONDS))
                        .andThen(theNumberOfSuccessfulMessagesIsQueried()))
                .then(expectResultToBe(15));
    }

    @Test
    default void testMessageBus_returnsCorrectNumberOfDroppedMessages(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aMessageBus()
                .configuredWith(messageBusTestConfig)
                .withASingleSubscriber()
                .withAFilterThatDropsWrongMessages())
                .when(severalInvalidMessagesAreSendAsynchronously(3, 5)
                        .andThen(theNumberOfDroppedMessagesIsQueried()))
                .then(expectResultToBe(15));
    }

    @Test
    default void testMessageBus_returnsCorrectNumberOfReplacedMessages(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aMessageBus()
                .configuredWith(messageBusTestConfig)
                .withASingleSubscriber()
                .withAFilterThatReplacesWrongMessages())
                .when(severalInvalidMessagesAreSendAsynchronously(3, 5)
                        .andThen(theNumberOfReplacedMessagesIsQueried()))
                .then(expectResultToBe(15));
    }

    @Test
    default void testMessageBus_returnsCorrectNumberOfDeliveryFailedMessages(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aMessageBus()
                .configuredWith(messageBusTestConfig)
                .withoutASubscriber())
                .when(severalMessagesAreSendAsynchronously(3, 5)
                        .andThen(theNumberOfFailedMessagesIsQueried()))
                .then(expectResultToBe(15));
    }

    @Test
    default void testMessageBus_returnsAValidTimestampForStatistics(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aMessageBus()
                .configuredWith(messageBusTestConfig)
                .withoutASubscriber())
                .when(theTimestampOfTheStatisticsIsQueried())
                .then(expectTimestampToBeInTheLastXSeconds(3));
    }


    //subscribers
    @Test
    default void testMessageBus_returnsCorrectSubscribersPerType(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aMessageBus()
                .configuredWith(messageBusTestConfig)
                .withASubscriberForTyp(TestMessageOfInterest.class)
                .withASubscriberForTyp(TestMessageOfInterest.class)
                .withASubscriberForTyp(InvalidTestMessage.class))
                .when(theSubscriberAreQueriedPerType())
                .then(expectSubscriberOfType(2, TestMessageOfInterest.class)
                        .and(expectSubscriberOfType(1, InvalidTestMessage.class)));
    }

    @Test
    default void testMessageBus_returnsCorrectSubscribersInList(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aMessageBus()
                .configuredWith(messageBusTestConfig)
                .withASubscriberForTyp(TestMessageOfInterest.class)
                .withASubscriberForTyp(InvalidTestMessage.class))
                .when(allSubscribersAreQueriedAsList())
                .then(expectAListOfSize(2));
    }

    //shutdown
    @Test
    default void testMessageBus_canShutdown_evenIfIsBlocked(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aMessageBus()
                .configuredWith(messageBusTestConfig)
                .withASubscriberThatBlocksWhenAccepting())
                .when(severalMessagesAreSendAsynchronouslyBeforeTheMessageBusIsShutdown(3, 5)
                        .andThen(theMessageBusShutdownIsExpectedForTimeoutInSeconds(1)))
                .then(expectTheMessageBusToBeShutdownInTime());
    }

    @Test
    default void testMessageBus_shutdownCallIsIdempotent(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aMessageBus()
                .configuredWith(messageBusTestConfig)
                .withASubscriberThatBlocksWhenAccepting())
                .when(theMessageBusIsShutdownAsynchronouslyXTimes(6)
                        .andThen(theMessageBusIsShutdown()))
                .then(expectTheMessageBusToBeShutdown());
    }

    //error cases
    @Test
    default void testMessageBus_errorMessageIsSend_whenNoSubscriberExists(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aMessageBus()
                .configuredWith(messageBusTestConfig)
                .withAnErrorAcceptingSubscriber())
                .when(aSingleMessageIsSend())
                .then(expectErrorMessageWithCause(NoSuitableSubscriberException.class));
    }

    @Test
    default void testMessageBus_errorMessageIsSend_whenNoSubscriberThrowsError(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aMessageBus()
                .configuredWith(messageBusTestConfig)
                .withAnErrorThrowingSubscriber()
                .withAnErrorAcceptingSubscriber())
                .when(aSingleMessageIsSend()
                        .andThen(aShortWaitIsDone(10, MILLISECONDS)))
                .then(expectErrorMessageWithCause(ExceptionInSubscriberException.class));
    }
}