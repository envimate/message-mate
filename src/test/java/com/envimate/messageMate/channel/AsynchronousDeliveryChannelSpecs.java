package com.envimate.messageMate.channel;

import com.envimate.messageMate.channel.config.AsynchronousDeliveryChannelConfigurationResolver;
import com.envimate.messageMate.channel.config.ChannelTestConfig;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.envimate.messageMate.channel.config.ChannelTestConfig.ASYNCHRONOUS_DELIVERY_POOL_SIZE;
import static com.envimate.messageMate.channel.givenWhenThen.ChannelActionBuilder.*;
import static com.envimate.messageMate.channel.givenWhenThen.ChannelSetupBuilder.aChannel;
import static com.envimate.messageMate.channel.givenWhenThen.ChannelValidationBuilder.expectResultToBe;
import static com.envimate.messageMate.channel.givenWhenThen.ChannelValidationBuilder.expectXMessagesToBeDelivered;
import static com.envimate.messageMate.shared.givenWhenThen.Given.given;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@ExtendWith(AsynchronousDeliveryChannelConfigurationResolver.class)
public class AsynchronousDeliveryChannelSpecs implements ChannelSpecs {

    //messageStatistics
    @Test
    public void testChannel_withBlockingSubscriber_whenNumberOfSuccessfulDeliveredMessagesIsQueried_returnsZero(final ChannelTestConfig testConfig) throws Exception {
        given(aChannel()
                .configuredWith(testConfig)
                .withASubscriberThatBlocksWhenAccepting())
                .when(severalMessagesAreSendAsynchronously(3, 5)
                        .andThen(theNumberOfSuccessfulMessagesIsQueried()))
                .then(expectResultToBe(0));
    }

    @Test
    public void testChannel_withBlockingSubscriber_whenNumberOfWaitingMessagesIsQueried_returnsZero(final ChannelTestConfig testConfig) throws Exception {
        given(aChannel()
                .configuredWith(testConfig)
                .withASubscriberThatBlocksWhenAccepting())
                .when(severalMessagesAreSendAsynchronously(3, 5)
                        .andThen(theNumberOfWaitingMessagesIsQueried()))
                .then(expectResultToBe(0));
    }


    @Test
    public void testChannel_withBlockingSubscriber_whenNumberOfAcceptedMessagesIsQueried_returnsOnlyOne(final ChannelTestConfig testConfig) throws Exception {
        given(aChannel()
                .configuredWith(testConfig)
                .withASubscriberThatBlocksWhenAccepting())
                .when(severalMessagesAreSendAsynchronously(3, 5)
                        .andThen(theNumberOfAcceptedMessagesIsQueried()))
                .then(expectResultToBe(15));
    }

    @Test
    public void testChannel_withBlockingSubscriber_whenNumberOfCurrentlyTransportedMessagesIsQueried_returnsZero(final ChannelTestConfig testConfig) throws Exception {
        given(aChannel()
                .configuredWith(testConfig)
                .withASubscriberThatBlocksWhenAccepting())
                .when(severalMessagesAreSendAsynchronously(3, 5)
                        .andThen(theNumberOfCurrentlyTransportedMessagesIsQueried()))
                .then(expectResultToBe(0));
    }

    @Test
    public void testChannel_withBlockingSubscriber_whenNumberOfCurrentlyDeliveredMessagesIsQueried_deliversTooMuchForUnboundedQueue(final ChannelTestConfig testConfig) throws Exception {
        given(aChannel()
                .configuredWith(testConfig)
                .withASubscriberThatBlocksWhenAccepting())
                .when(severalMessagesAreSendAsynchronouslyButWillBeBlocked(3, 5)
                        .andThen(aShortWaitIsDone(100, MILLISECONDS))
                        .andThen(theNumberOfCurrentlyDeliveredMessagesIsQueried()))
                .then(expectResultToBe(3 * 5));
    }

    //shutdown
    @Test
    public void testChannel_whenShutdown_deliversRemainingMessagesButNoNewAdded(final ChannelTestConfig testConfig) throws Exception {
        given(aChannel()
                .configuredWith(testConfig))
                .when(theChannelIsShutdownAfterHalfOfTheMessagesWereDelivered(10)
                        .andThen(aShortWaitIsDone(10, MILLISECONDS)))
                .then(expectXMessagesToBeDelivered(5));
    }

    @Test
    public void testChannel_whenShutdownWithoutFinishingRemainingTasksIsCalled_noNewTasksAreFinished(final ChannelTestConfig testConfig) throws Exception {
        given(aChannel()
                .configuredWith(testConfig))
                .when(theChannelIsShutdownAfterHalfOfTheMessagesWereDelivered_withoutFinishingRemainingTasks(10)
                        .andThen(aShortWaitIsDone(10, MILLISECONDS)))
                .then(expectXMessagesToBeDelivered(ASYNCHRONOUS_DELIVERY_POOL_SIZE));
    }

}
