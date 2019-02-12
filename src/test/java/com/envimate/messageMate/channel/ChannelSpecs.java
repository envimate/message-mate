package com.envimate.messageMate.channel;

import com.envimate.messageMate.channel.config.ChannelTestConfig;
import org.junit.jupiter.api.Test;

import static com.envimate.messageMate.channel.givenWhenThen.ChannelActionBuilder.*;
import static com.envimate.messageMate.channel.givenWhenThen.ChannelSetupBuilder.aConfiguredChannel;
import static com.envimate.messageMate.channel.givenWhenThen.ChannelValidationBuilder.*;
import static com.envimate.messageMate.shared.channelMessageBus.givenWhenThen.Given.given;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public interface ChannelSpecs {

    //sending and subscribe
    @Test
    default void testChannel_canSendSingleMessageToOneReceiver(final ChannelTestConfig testConfig) throws Exception {
        given(aConfiguredChannel(testConfig)
                .withASingleSubscriber())
                .when(aSingleMessageIsSend())
                .then(expectTheMessageToBeReceived());
    }

    @Test
    default void testChannel_canSendSeveralMessagesToSeveralSubscriber(final ChannelTestConfig testConfig) throws Exception {
        given(aConfiguredChannel(testConfig)
                .withSeveralSubscriber(1))
                .when(severalMessagesAreSend(10))
                .then(expectAllMessagesToBeReceivedByAllSubscribers());
    }

    @Test
    default void testChannel_canSendAsynchronouslyReceivedMessages(final ChannelTestConfig testConfig) throws Exception {
        given(aConfiguredChannel(testConfig)
                .withSeveralSubscriber(5))
                .when(severalMessagesAreSendAsynchronously(5, 10))
                .then(expectAllMessagesToBeReceivedByAllSubscribers());
    }

    //unsubscribe

    @Test
    default void testChannel_canUnsubscribe(final ChannelTestConfig testConfig) throws Exception {
        given(aConfiguredChannel(testConfig)
                .withSeveralSubscriber(5))
                .when(oneSubscriberUnsubscribes())
                .then(expectAllRemainingSubscribersToStillBeSubscribed());
    }

    @Test
    default void testChannel_canUnsubscribeTwoSubscribers(final ChannelTestConfig testConfig) throws Exception {
        given(aConfiguredChannel(testConfig)
                .withSeveralSubscriber(5))
                .when(oneSubscriberUnsubscribes()
                        .andThen(oneSubscriberUnsubscribes()))
                .then(expectAllRemainingSubscribersToStillBeSubscribed());
    }

    @Test
    default void testChannel_canUnsubscribeTheSameSubscriberSeveralTimes(final ChannelTestConfig testConfig) throws Exception {
        given(aConfiguredChannel(testConfig)
                .withSeveralSubscriber(5))
                .when(oneSubscriberUnsubscribesSeveralTimes(2))
                .then(expectAllRemainingSubscribersToStillBeSubscribed());
    }

    //filter
    @Test
    default void testChannel_allowsFiltersToChangeMessages(final ChannelTestConfig testConfig) throws Exception {
        given(aConfiguredChannel(testConfig)
                .withSeveralSubscriber(3)
                .withAFilterThatChangesTheContentOfEveryMessage())
                .when(severalMessagesAreSend(10)
                        .andThen(aShortWaitIsDone(5, MILLISECONDS)))
                .then(expectAllMessagesToHaveTheContentChanged());
    }

    @Test
    default void testChannel_allowsFiltersToDropMessages(final ChannelTestConfig testConfig) throws Exception {
        given(aConfiguredChannel(testConfig)
                .withSeveralSubscriber(3)
                .withAFilterThatDropsWrongMessages())
                .when(bothValidAndInvalidMessagesAreSendAsynchronously(3, 10))
                .then(expectAllMessagesToBeReceivedByAllSubscribers());
    }


    @Test
    default void testChannel_allowsFiltersToReplaceMessages(final ChannelTestConfig testConfig) throws Exception {
        given(aConfiguredChannel(testConfig)
                .withSeveralSubscriber(3)
                .withAFilterThatReplacesWrongMessages())
                .when(severalInvalidMessagesAreSendAsynchronously(3, 10))
                .then(expectOnlyValidMessageToBeReceived());
    }

    @Test
    default void testChannel_whenAFilterDoesNotUseAMethod_messageIsDropped(final ChannelTestConfig testConfig) throws Exception {
        given(aConfiguredChannel(testConfig)
                .withSeveralSubscriber(3)
                .withAnInvalidFilterThatDoesNotUseAnyFilterMethods())
                .when(severalInvalidMessagesAreSendAsynchronously(3, 10))
                .then(expectNoMessagesToBeDelivered());
    }

    @Test
    default void testChannel_canAddFilterAtASpecificPosition(final ChannelTestConfig testConfig) throws Exception {
        given(aConfiguredChannel(testConfig)
                .withSeveralSubscriber(3)
                .withTwoFilterOnSpecificPositions())
                .when(severalMessagesAreSend(3)
                        .andThen(aShortWaitIsDone(5, MILLISECONDS)))
                .then(expectAllMessagesToHaveTheContentChanged());
    }

    @Test
    default void testChannel_throwsExceptionForPositionBelowZero(final ChannelTestConfig testConfig) throws Exception {
        given(aConfiguredChannel(testConfig)
                .withAFilterAtAnInvalidPosition(-1))
                .when(aSingleMessageIsSend())
                .then(expectTheException(IndexOutOfBoundsException.class));
    }

    @Test
    default void testChannel_throwsExceptionForPositionGreaterThanAllowed(final ChannelTestConfig testConfig) throws Exception {
        given(aConfiguredChannel(testConfig)
                .withAFilterAtAnInvalidPosition(100))
                .when(aSingleMessageIsSend())
                .then(expectTheException(IndexOutOfBoundsException.class));
    }

    @Test
    default void testChannel_canQueryListOfFilter(final ChannelTestConfig testConfig) throws Exception {
        given(aConfiguredChannel(testConfig)
                .withTwoFilterOnSpecificPositions())
                .when(theListOfFiltersIsQueried())
                .then(expectAListWithAllFilters());
    }

    @Test
    default void testChannel_canRemoveFilter(final ChannelTestConfig testConfig) throws Exception {
        given(aConfiguredChannel(testConfig)
                .withTwoFilterOnSpecificPositions())
                .when(aFilterIsRemoved())
                .then(expectTheRemainingFilter());
    }

    //messageStatistics
    @Test
    default void testChannel_returnsCorrectNumberOfAcceptedMessages(final ChannelTestConfig testConfig) throws Exception {
        given(aConfiguredChannel(testConfig)
                .withASingleSubscriber())
                .when(severalMessagesAreSendAsynchronously(3, 5)
                        .andThen(theNumberOfAcceptedMessagesIsQueried()))
                .then(expectResultToBe(15));
    }

    @Test
    default void testChannel_returnsCorrectNumberOfSuccessfulMessages(final ChannelTestConfig testConfig) throws Exception {
        given(aConfiguredChannel(testConfig)
                .withASingleSubscriber())
                .when(severalMessagesAreSendAsynchronously(3, 5)
                        .andThen(aShortWaitIsDone(10, MILLISECONDS))
                        .andThen(theNumberOfSuccessfulMessagesIsQueried()))
                .then(expectResultToBe(15));
    }

    @Test
    default void testChannel_returnsCorrectNumberOfDeliveryFailedMessages(final ChannelTestConfig testConfig) throws Exception {
        given(aConfiguredChannel(testConfig)
                .withoutASubscriber())
                .when(severalMessagesAreSendAsynchronously(3, 5)
                        .andThen(theNumberOfFailedMessagesIsQueried()))
                .then(expectResultToBe(15));
    }

    @Test
    default void testChannel_returnsCorrectNumberOfDroppedMessages(final ChannelTestConfig testConfig) throws Exception {
        given(aConfiguredChannel(testConfig)
                .withASingleSubscriber()
                .withAFilterThatDropsWrongMessages())
                .when(severalInvalidMessagesAreSendAsynchronously(3, 5)
                        .andThen(theNumberOfDroppedMessagesIsQueried()))
                .then(expectResultToBe(15));
    }

    @Test
    default void testChannel_returnsCorrectNumberOfReplacedMessages(final ChannelTestConfig testConfig) throws Exception {
        given(aConfiguredChannel(testConfig)
                .withASingleSubscriber()
                .withAFilterThatReplacesWrongMessages())
                .when(severalInvalidMessagesAreSendAsynchronously(3, 5)
                        .andThen(theNumberOfReplacedMessagesIsQueried()))
                .then(expectResultToBe(15));
    }

    @Test
    default void testChannel_whenAFilterDoesNotUseAMethod_theMessageIsMarkedAsForgotten(final ChannelTestConfig testConfig) throws Exception {
        given(aConfiguredChannel(testConfig)
                .withSeveralSubscriber(3)
                .withAnInvalidFilterThatDoesNotUseAnyFilterMethods())
                .when(aSingleMessageIsSend()
                        .andThen(theNumberOfForgottenMessagesIsQueried()))
                .then(expectResultToBe(1));
    }

    @Test
    default void testChannel_returnsAValidTimestampForStatistics(final ChannelTestConfig testConfig) throws Exception {
        given(aConfiguredChannel(testConfig)
                .withoutASubscriber())
                .when(theTimestampOfTheStatisticsIsQueried())
                .then(expectTimestampToBeInTheLastXSeconds(3));
    }

    //shutdown
    @Test
    default void testChannel_canShutdown_evenIfIsBlocked(final ChannelTestConfig testConfig) throws Exception {
        given(aConfiguredChannel(testConfig)
                .withASubscriberThatBlocksWhenAccepting())
                .when(severalMessagesAreSendAsynchronouslyBeforeTheChannelIsShutdown(3, 5)
                        .andThen(theChannelShutdownIsExpectedForTimeoutInSeconds(1)))
                .then(expectTheChannelToBeShutdownInTime());
    }

    @Test
    default void testChannel_shutdownCallIsIdempotent(final ChannelTestConfig testConfig) throws Exception {
        given(aConfiguredChannel(testConfig)
                .withASubscriberThatBlocksWhenAccepting())
                .when(theChannelIsShutdownAsynchronouslyXTimes(6)
                        .andThen(theChannelIsShutdown()))
                .then(expectTheChannelToBeShutdown());
    }

}
