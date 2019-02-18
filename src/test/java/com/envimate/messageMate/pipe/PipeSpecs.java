package com.envimate.messageMate.pipe;

import com.envimate.messageMate.pipe.config.PipeTestConfig;
import com.envimate.messageMate.pipe.givenWhenThen.PipeActionBuilder;
import org.junit.jupiter.api.Test;

import static com.envimate.messageMate.pipe.givenWhenThen.PipeActionBuilder.*;
import static com.envimate.messageMate.pipe.givenWhenThen.PipeSetupBuilder.aConfiguredPipe;
import static com.envimate.messageMate.pipe.givenWhenThen.PipeValidationBuilder.*;
import static com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.Given.given;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public interface PipeSpecs {

    //sending and subscribe
    @Test
    default void testPipe_canSendSingleMessageToOneReceiver(final PipeTestConfig testConfig) throws Exception {
        given(aConfiguredPipe(testConfig)
                .withASingleSubscriber())
                .when(aSingleMessageIsSend())
                .then(expectTheMessageToBeReceived());
    }

    @Test
    default void testPipe_canSendSeveralMessagesToSeveralSubscriber(final PipeTestConfig testConfig) throws Exception {
        given(aConfiguredPipe(testConfig)
                .withSeveralSubscriber(5))
                .when(severalMessagesAreSend(10))
                .then(expectAllMessagesToBeReceivedByAllSubscribers());
    }

    @Test
    default void testPipe_canSendMessagesAsynchronously(final PipeTestConfig testConfig) throws Exception {
        given(aConfiguredPipe(testConfig)
                .withSeveralSubscriber(5))
                .when(severalMessagesAreSendAsynchronously(5, 10))
                .then(expectAllMessagesToBeReceivedByAllSubscribers());
    }

    //unsubscribe
    @Test
    default void testPipe_canUnsubscribe(final PipeTestConfig testConfig) throws Exception {
        given(aConfiguredPipe(testConfig)
                .withSeveralSubscriber(5))
                .when(oneSubscriberUnsubscribes())
                .then(expectAllRemainingSubscribersToStillBeSubscribed());
    }

    @Test
    default void testPipe_canUnsubscribeTwoSubscribers(final PipeTestConfig testConfig) throws Exception {
        given(aConfiguredPipe(testConfig)
                .withSeveralSubscriber(5))
                .when(oneSubscriberUnsubscribes()
                        .andThen(oneSubscriberUnsubscribes()))
                .then(expectAllRemainingSubscribersToStillBeSubscribed());
    }

    @Test
    default void testPipe_canUnsubscribeTheSameSubscriberSeveralTimes(final PipeTestConfig testConfig) throws Exception {
        given(aConfiguredPipe(testConfig)
                .withSeveralSubscriber(5))
                .when(oneSubscriberUnsubscribesSeveralTimes(2))
                .then(expectAllRemainingSubscribersToStillBeSubscribed());
    }

    //messageStatistics
    @Test
    default void testPipe_returnsCorrectNumberOfAcceptedMessages(final PipeTestConfig testConfig) throws Exception {
        given(aConfiguredPipe(testConfig)
                .withASingleSubscriber())
                .when(severalMessagesAreSendAsynchronously(3, 5)
                        .andThen(theNumberOfAcceptedMessagesIsQueried()))
                .then(expectResultToBe(15));
    }

    @Test
    default void testPipe_returnsCorrectNumberOfSuccessfulMessages(final PipeTestConfig testConfig) throws Exception {
        given(aConfiguredPipe(testConfig)
                .withASingleSubscriber())
                .when(severalMessagesAreSendAsynchronously(3, 5)
                        .andThen(aShortWaitIsDone(10, MILLISECONDS))
                        .andThen(theNumberOfSuccessfulMessagesIsQueried()))
                .then(expectResultToBe(15));
    }

    @Test
    default void testPipe_returnsCorrectNumberOfDeliveryFailedMessages(final PipeTestConfig testConfig) throws Exception {
        given(aConfiguredPipe(testConfig)
                .withoutASubscriber())
                .when(severalMessagesAreSendAsynchronously(3, 5)
                        .andThen(theNumberOfFailedMessagesIsQueried()))
                .then(expectResultToBe(15));
    }

    @Test
    default void testPipe_returnsAValidTimestampForStatistics(final PipeTestConfig testConfig) throws Exception {
        given(aConfiguredPipe(testConfig)
                .withoutASubscriber())
                .when(theTimestampOfTheStatisticsIsQueried())
                .then(expectTimestampToBeInTheLastXSeconds(3));
    }

    //shutdown
    @Test
    default void testPipe_canShutdown_evenIfIsBlocked(final PipeTestConfig testConfig) throws Exception {
        given(aConfiguredPipe(testConfig)
                .withASubscriberThatBlocksWhenAccepting())
                .when(severalMessagesAreSendAsynchronouslyBeforeThePipeIsShutdown(3, 5)
                        .andThen(thePipeShutdownIsExpectedForTimeoutInSeconds(1)))
                .then(expectThePipeToBeShutdownInTime());
    }

    @Test
    default void testPipe_shutdownCallIsIdempotent(final PipeTestConfig testConfig) throws Exception {
        given(aConfiguredPipe(testConfig)
                .withASubscriberThatBlocksWhenAccepting())
                .when(thePipeIsShutdownAsynchronouslyXTimes(6)
                        .andThen(thePipeIsShutdown()))
                .then(expectThePipeToBeShutdown());
    }

    @Test
    default void testPipe_awaitReturnsAlwaysFalse_withoutACloseCall(final PipeTestConfig testConfig) throws Exception {
        given(aConfiguredPipe(testConfig))
                .when(PipeActionBuilder.awaitWithoutACloseIsCalled())
                .then(expectTheResultToAlwaysBeFalse());
    }

    //TODO: await explizit testen für alle

}
