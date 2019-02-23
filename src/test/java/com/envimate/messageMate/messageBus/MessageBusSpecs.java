package com.envimate.messageMate.messageBus;


import com.envimate.messageMate.error.AlreadyClosedException;
import com.envimate.messageMate.messageBus.config.MessageBusTestConfig;
import com.envimate.messageMate.shared.testMessages.InvalidTestMessage;
import com.envimate.messageMate.shared.testMessages.SubClassingTestMessageOfInterest;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import com.envimate.messageMate.shared.testMessages.TestMessageOfInterest;
import org.junit.jupiter.api.Test;

import static com.envimate.messageMate.messageBus.givenWhenThen.Given.given;
import static com.envimate.messageMate.messageBus.givenWhenThen.MessageBusActionBuilder.*;
import static com.envimate.messageMate.messageBus.givenWhenThen.MessageBusSetupBuilder.aConfiguredMessageBus;
import static com.envimate.messageMate.messageBus.givenWhenThen.MessageBusValidationBuilder.*;
import static com.envimate.messageMate.shared.testMessages.SubClassingTestMessageOfInterest.subClassingTestMessageOfInterest;
import static com.envimate.messageMate.shared.testMessages.TestMessageOfInterest.messageOfInterest;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public interface MessageBusSpecs {

    //Send and subscribe
    @Test
    default void testMessageBus_canSendAndReceiveASingleMessage(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withASingleSubscriber())
                .when(aSingleMessageIsSend())
                .then(expectTheMessageToBeReceived());
    }

    @Test
    default void testMessageBus_canSendAndReceiveSeveralMessagesWithSeveralSubscriber(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withSeveralSubscriber(5))
                .when(severalMessagesAreSend(10)
                        .andThen(aShortWaitIsDone(5, MILLISECONDS)))
                .then(expectAllMessagesToBeReceivedByAllSubscribers());
    }

    @Test
    default void testMessageBus_canSendMessageTwice(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        final TestMessageOfInterest message = messageOfInterest();
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withASingleSubscriber(TestMessage.class))
                .when(theMessageIsSend(message)
                        .andThen(theMessageIsSend(message)))
                .then(expectTheMessagesToBeReceivedByAllSubscriber(message, message));
    }

    @Test
    default void testMessageBus_subClassesAreReceived_forInterfaces(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        final TestMessageOfInterest subClassingMessage = messageOfInterest();
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withASingleSubscriber(TestMessage.class))
                .when(theMessageIsSend(subClassingMessage))
                .then(expectTheMessagesToBeReceivedByAllSubscriber(subClassingMessage));
    }

    @Test
    default void testMessageBus_subClassesAreReceived_forExtendedClasses(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        final SubClassingTestMessageOfInterest subClassingMessage = subClassingTestMessageOfInterest();
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withASingleSubscriber(TestMessageOfInterest.class))
                .when(theMessageIsSend(subClassingMessage))
                .then(expectTheMessagesToBeReceivedByAllSubscriber(subClassingMessage));
    }

    @Test
    default void testMessageBus_subClassesAreReceived_evenIfSubscriberIsAddedLater(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        final TestMessageOfInterest subClassingMessage1 = messageOfInterest();
        final TestMessageOfInterest subClassingMessage2 = messageOfInterest();
        given(aConfiguredMessageBus(messageBusTestConfig))
                .when(theMessageIsSend(subClassingMessage1)
                        .andThen(aSubscriberIsAdded(TestMessage.class)
                                .andThen(theMessageIsSend(subClassingMessage2))))
                .then(expectTheMessagesToBeReceivedByAllSubscriber(subClassingMessage2));
    }

    @Test
    default void testMessageBus_subClassesAreReceivedByAllSubscriber(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        final TestMessageOfInterest subClassingMessage = messageOfInterest();
        given(aConfiguredMessageBus(messageBusTestConfig))
                .when(aSubscriberIsAdded(TestMessage.class)
                        .andThen(aSubscriberIsAdded(TestMessage.class)
                                .andThen(theMessageIsSend(subClassingMessage))))
                .then(expectTheMessagesToBeReceivedByAllSubscriber(subClassingMessage));
    }

    @Test
    default void testMessageBus_objectChannelReceivesAll(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        final TestMessageOfInterest message1 = messageOfInterest();
        final SubClassingTestMessageOfInterest message2 = subClassingTestMessageOfInterest();
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withASubscriberForTyp(Object.class))
                .when(theMessageIsSend(message1)
                        .andThen(theMessageIsSend(message2)))
                .then(expectTheMessagesToBeReceivedByAllSubscriber(message1, message2));
    }

    @Test
    default void testMessageBus_objectChannelReceivesAll_evenIfAddedLater(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        final TestMessageOfInterest ignoredMessage1 = messageOfInterest();
        final SubClassingTestMessageOfInterest ignoredMessage2 = subClassingTestMessageOfInterest();
        final TestMessageOfInterest receivedMessage1 = messageOfInterest();
        final SubClassingTestMessageOfInterest receivedMessage2 = subClassingTestMessageOfInterest();
        given(aConfiguredMessageBus(messageBusTestConfig))
                .when(theMessageIsSend(ignoredMessage1)
                        .andThen(theMessageIsSend(ignoredMessage2)
                                .andThen(aSubscriberIsAdded(Object.class)
                                        .andThen(theMessageIsSend(receivedMessage1)
                                                .andThen(theMessageIsSend(receivedMessage2))))))
                .then(expectTheMessagesToBeReceivedByAllSubscriber(receivedMessage1, receivedMessage2));
    }

    @Test
    default void testMessageBus_canSendAndReceiveMessagesAsynchronously(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withSeveralSubscriber(5))
                .when(severalMessagesAreSendAsynchronously(5, 10)
                        .andThen(aShortWaitIsDone(5, MILLISECONDS)))
                .then(expectAllMessagesToBeReceivedByAllSubscribers());
    }

    //unsubscribe
    @Test
    default void testMessageBus_canUnsubscribe(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withSeveralSubscriber(5))
                .when(oneSubscriberUnsubscribes())
                .then(expectAllRemainingSubscribersToStillBeSubscribed());
    }

    @Test
    default void testMessageBus_canUnsubscribeTwoSubscribers(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withSeveralSubscriber(5))
                .when(oneSubscriberUnsubscribes()
                        .andThen(oneSubscriberUnsubscribes()))
                .then(expectAllRemainingSubscribersToStillBeSubscribed());
    }

    @Test
    default void testMessageBus_canUnsubscribeTheSameSubscriberSeveralTimes(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withSeveralSubscriber(5))
                .when(oneSubscriberUnsubscribesSeveralTimes(2))
                .then(expectAllRemainingSubscribersToStillBeSubscribed());
    }

    //filter
    @Test
    default void testMessageBus_allowsFiltersToChangeMessages(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withSeveralSubscriber(1)
                .withAFilterThatChangesTheContentOfEveryMessage())
                .when(severalMessagesAreSend(1))
                .then(expectAllMessagesToHaveTheContentChanged());
    }

    @Test
    default void testMessageBus_allowsFiltersToDropMessages(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withSeveralSubscriber(3)
                .withAFilterThatDropsMessages())
                .when(halfValidAndInvalidMessagesAreSendAsynchronously(3, 10))
                .then(expectNoMessagesToBeDelivered());
    }

    @Test
    default void testMessageBus_allowsFiltersToReplaceMessages(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withSeveralSubscriber(3)
                .withAFilterThatReplacesWrongMessages())
                .when(severalInvalidMessagesAreSendAsynchronously(3, 10))
                .then(expectOnlyValidMessageToBeReceived());
    }

    @Test
    default void testMessageBus_whenAFilterDoesNotUseAMethod_messageIsDropped(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withSeveralSubscriber(3)
                .withAnInvalidFilterThatDoesNotUseAnyFilterMethods())
                .when(severalInvalidMessagesAreSendAsynchronously(3, 10))
                .then(expectNoMessagesToBeDelivered());
    }

    @Test
    default void testMessageBus_throwsExceptionForPositionBelowZero(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withAFilterAtAnInvalidPosition(-1))
                .when(aSingleMessageIsSend())
                .then(expectTheException(IndexOutOfBoundsException.class));
    }

    @Test
    default void testMessageBus_throwsExceptionForPositionGreaterThanAllowed(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withAFilterAtAnInvalidPosition(100))
                .when(aSingleMessageIsSend())
                .then(expectTheException(IndexOutOfBoundsException.class));
    }

    @Test
    default void testMessageBus_canQueryListOfFilter(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withTwoFilterOnSpecificPositions())
                .when(theListOfFiltersIsQueried())
                .then(expectAListWithAllFilters());
    }

    @Test
    default void testMessageBus_canRemoveFilter(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withTwoFilterOnSpecificPositions())
                .when(aFilterIsRemoved())
                .then(expectTheRemainingFilter());
    }

    //messageStatistics
    @Test
    default void testMessageBus_whenAFilterDoesNotUseAMethod_theMessageIsMarkedAsForgotten(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withSeveralSubscriber(3)
                .withAnInvalidFilterThatDoesNotUseAnyFilterMethods())
                .when(aSingleMessageIsSend()
                        .andThen(theNumberOfForgottenMessagesIsQueried()))
                .then(expectResultToBe(1));
    }

    @Test
    default void testMessageBus_returnsCorrectNumberOfAcceptedMessages(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withASingleSubscriber())
                .when(severalMessagesAreSendAsynchronously(3, 5)
                        .andThen(theNumberOfAcceptedMessagesIsQueried()))
                .then(expectResultToBe(15));
    }

    @Test
    default void testMessageBus_returnsCorrectNumberOfSuccessfulMessages(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withASingleSubscriber())
                .when(severalMessagesAreSendAsynchronously(3, 5)
                        .andThen(aShortWaitIsDone(10, MILLISECONDS))
                        .andThen(theNumberOfSuccessfulMessagesIsQueried()))
                .then(expectResultToBe(15));
    }

    @Test
    default void testMessageBus_returnsCorrectNumberOfDroppedMessages(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withASingleSubscriber()
                .withAFilterThatDropsMessages())
                .when(severalInvalidMessagesAreSendAsynchronously(3, 5)
                        .andThen(theNumberOfBlockedMessagesIsQueried()))
                .then(expectResultToBe(15));
    }

    @Test
    default void testMessageBus_returnsCorrectNumberOfReplacedMessages(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withASingleSubscriber()
                .withAFilterThatReplacesWrongMessages())
                .when(severalInvalidMessagesAreSendAsynchronously(3, 5)
                        .andThen(theNumberOfReplacedMessagesIsQueried()))
                .then(expectResultToBe(15));
    }

    @Test
    default void testMessageBus_returnsCorrectNumberOfDeliveryFailedMessages(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withoutASubscriber())
                .when(severalMessagesAreSendAsynchronously(3, 5)
                        .andThen(theNumberOfFailedMessagesIsQueried()))
                .then(expectResultToBe(0));
    }

    @Test
    default void testMessageBus_returnsAValidTimestampForStatistics(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withoutASubscriber())
                .when(theTimestampOfTheStatisticsIsQueried())
                .then(expectTimestampToBeInTheLastXSeconds(3));
    }


    //subscribers
    @Test
    default void testMessageBus_returnsCorrectSubscribersPerType(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withASubscriberForTyp(TestMessageOfInterest.class)
                .withASubscriberForTyp(TestMessageOfInterest.class)
                .withASubscriberForTyp(InvalidTestMessage.class))
                .when(theSubscriberAreQueriedPerType())
                .then(expectSubscriberOfType(2, TestMessageOfInterest.class)
                        .and(expectSubscriberOfType(1, InvalidTestMessage.class)));
    }

    @Test
    default void testMessageBus_returnsCorrectSubscribersInList(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withASubscriberForTyp(TestMessageOfInterest.class)
                .withASubscriberForTyp(InvalidTestMessage.class))
                .when(allSubscribersAreQueriedAsList())
                .then(expectAListOfSize(2));
    }


    //channel
    @Test
    default void testMessageBus_returnsCorrectChannel(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withACustomChannelFactory()
                .withASubscriberForTyp(TestMessageOfInterest.class))
                .when(theChannelForTheClassIsQueried(TestMessageOfInterest.class))
                .then(expectTheCorrectChannel());
    }

    //shutdown
    @Test
    default void testMessageBus_canShutdown_evenIfIsBlocked(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withASubscriberThatBlocksWhenAccepting())
                .when(severalMessagesAreSendAsynchronouslyBeforeTheMessageBusIsShutdown(3, 5)
                        .andThen(theMessageBusShutdownIsExpectedForTimeoutInSeconds(1)))
                .then(expectTheMessageBusToBeShutdownInTime());
    }

    @Test
    default void testMessageBus_shutdownCallIsIdempotent(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withASubscriberThatBlocksWhenAccepting())
                .when(theMessageBusIsShutdownAsynchronouslyXTimes(6)
                        .andThen(theMessageBusIsShutdown()))
                .then(expectTheMessageBusToBeShutdown());
    }

    //TODO: await in sub configs -> sync and async configs
    //TODO: error in acceptingPipe filter, error class specific channel filter, error in subscription.subscriber

    //error cases
    @Test
    default void testMessageBus_throwsErrorWhenSendOnAClosedMessageBusIsCalled(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withAnErrorAcceptingSubscriber())
                .when(theMessageBusIsShutdown()
                        .andThen(aSingleMessageIsSend()))
                .then(expectTheException(AlreadyClosedException.class));
    }
}