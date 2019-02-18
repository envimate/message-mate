package com.envimate.messageMate.pipe;

import com.envimate.messageMate.pipe.config.ChannelTestConfig;
import com.envimate.messageMate.pipe.config.SynchronisedChannelConfigurationResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.envimate.messageMate.pipe.givenWhenThen.ChannelActionBuilder.*;
import static com.envimate.messageMate.pipe.givenWhenThen.ChannelSetupBuilder.aConfiguredChannel;
import static com.envimate.messageMate.pipe.givenWhenThen.ChannelValidationBuilder.*;
import static com.envimate.messageMate.shared.channelMessageBus.givenWhenThen.Given.given;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@ExtendWith(SynchronisedChannelConfigurationResolver.class)
public class SynchronisedPipeSpecs implements PipeSpecs {

    //subscribing
    @Test
    public void testChannel_subscriberCanInterruptDeliveringMessage_whenDeliveryIsSynchronous(final ChannelTestConfig testConfig) throws Exception {
        given(aConfiguredChannel(testConfig)
                .withSeveralDeliveryInterruptingSubscriber(5))
                .when(severalMessagesAreSend(10))
                .then(expectEachMessagesToBeReceivedByOnlyOneSubscriber());
    }

    //messageStatistics
    @Test
    public void testChannel_withBlockingSubscriber_whenNumberOfSuccessfulDeliveredMessagesIsQueried_returnsZero(final ChannelTestConfig testConfig) throws Exception {
        given(aConfiguredChannel(testConfig)
                .withASubscriberThatBlocksWhenAccepting())
                .when(severalMessagesAreSendAsynchronouslyButWillBeBlocked(3, 5)
                        .andThen(theNumberOfSuccessfulMessagesIsQueried()))
                .then(expectResultToBe(0));
    }

    @Test
    public void testChannel_withBlockingSubscriber_whenNumberOfAcceptedMessagesIsQueried_returnsOnlyOne(final ChannelTestConfig testConfig) throws Exception {
        given(aConfiguredChannel(testConfig)
                .withASubscriberThatBlocksWhenAccepting())
                .when(severalMessagesAreSendAsynchronouslyButWillBeBlocked(3, 5)
                        .andThen(aShortWaitIsDone(10, MILLISECONDS))
                        .andThen(theNumberOfAcceptedMessagesIsQueriedAsynchronously()))
                .then(expectResultToBe(1));
    }

    @Test
    public void testChannel_withBlockingSubscriber_whenNumberOfWaitingMessagesIsQueried_returnsZero(final ChannelTestConfig testConfig) throws Exception {
        given(aConfiguredChannel(testConfig)
                .withASubscriberThatBlocksWhenAccepting())
                .when(severalMessagesAreSendAsynchronouslyButWillBeBlocked(3, 5)
                        .andThen(aShortWaitIsDone(10, MILLISECONDS))
                        .andThen(theNumberOfWaitingMessagesIsQueried()))
                .then(expectResultToBe(0));
    }

    @Test
    public void testChannel_withBlockingSubscriber_whenNumberOfCurrentlyTransportedMessagesIsQueried_returnsZero(final ChannelTestConfig testConfig) throws Exception {
        given(aConfiguredChannel(testConfig)
                .withASubscriberThatBlocksWhenAccepting())
                .when(severalMessagesAreSendAsynchronouslyButWillBeBlocked(3, 5)
                        .andThen(aShortWaitIsDone(10, MILLISECONDS))
                        .andThen(theNumberOfCurrentlyTransportedMessagesIsQueried()))
                .then(expectResultToBe(0));
    }

    @Test
    public void testChannel_withBlockingSubscriber_whenNumberOfCurrentlyDeliveredMessagesIsQueried_returnsZero(final ChannelTestConfig testConfig) throws Exception {
        given(aConfiguredChannel(testConfig)
                .withASubscriberThatBlocksWhenAccepting())
                .when(severalMessagesAreSendAsynchronouslyButWillBeBlocked(3, 5)
                        .andThen(aShortWaitIsDone(10, MILLISECONDS))
                        .andThen(theNumberOfCurrentlyDeliveredMessagesIsQueried()))
                .then(expectResultToBe(1));
    }

    //shutdown
    @Test
    public void testChannel_whenShutdown_deliversRemainingMessagesButNoNewAdded(final ChannelTestConfig testConfig) throws Exception {
        given(aConfiguredChannel(testConfig))
                .when(theChannelIsShutdownAfterHalfOfTheMessagesWereDelivered(10))
                .then(expectXMessagesToBeDelivered(1));
        //because waiting senders wait on synchronised send -> they never entered the Pipe and do not count as remaining
    }

    @Test
    public void testChannel_whenShutdownWithoutFinishingRemainingTasksIsCalled_noTasksAreFinished(final ChannelTestConfig testConfig) throws Exception {
        given(aConfiguredChannel(testConfig))
                .when(theChannelIsShutdownAfterHalfOfTheMessagesWereDelivered_withoutFinishingRemainingTasks(10))
                .then(expectXMessagesToBeDelivered(1));
    }

}
