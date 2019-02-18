package com.envimate.messageMate.pipe;

import com.envimate.messageMate.pipe.config.ChannelTestConfig;
import com.envimate.messageMate.pipe.config.QueuingAcceptingStrategyChannelConfigurationResolver;
import com.envimate.messageMate.pipe.givenWhenThen.ChannelActionBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.envimate.messageMate.pipe.givenWhenThen.ChannelActionBuilder.*;
import static com.envimate.messageMate.pipe.givenWhenThen.ChannelSetupBuilder.aConfiguredChannel;
import static com.envimate.messageMate.pipe.givenWhenThen.ChannelValidationBuilder.expectResultToBe;
import static com.envimate.messageMate.pipe.givenWhenThen.ChannelValidationBuilder.expectXMessagesToBeDelivered;
import static com.envimate.messageMate.shared.channelMessageBus.givenWhenThen.Given.given;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@ExtendWith(QueuingAcceptingStrategyChannelConfigurationResolver.class)
public class QueuingAcceptingPipeSpecs implements PipeSpecs {

    @Test
    public void testChannel_withBlockingSubscriber_whenNumberOfSuccessfulDeliveredMessagesIsQueried_returnsZero(final ChannelTestConfig testConfig) throws Exception {
        given(aConfiguredChannel(testConfig)
                .withASubscriberThatBlocksWhenAccepting())
                .when(severalMessagesAreSendAsynchronouslyButWillBeBlocked(3, 5)
                        .andThen(theNumberOfSuccessfulMessagesIsQueried()))
                .then(expectResultToBe(0));
    }

    @Test
    public void testChannel_withBlockingSubscriber_doesNotQueueAnyMessages(final ChannelTestConfig testConfig) throws Exception {
        //Transport is synchronous. So a transport will always be executed on the thread calling "send"
        given(aConfiguredChannel(testConfig)
                .withASubscriberThatBlocksWhenAccepting())
                .when(severalMessagesAreSendAsynchronouslyButWillBeBlocked(3, 5)
                        .andThen(ChannelActionBuilder.theNumberOfWaitingMessagesIsQueried()))
                .then(expectResultToBe(0));
    }

    @Test
    public void testChannel_withBlockingSubscriber_whenNumberOfAcceptedMessagesIsQueried_returnsAll(final ChannelTestConfig testConfig) throws Exception {
        final int numberOfParallelSender = 3;
        given(aConfiguredChannel(testConfig)
                .withASubscriberThatBlocksWhenAccepting())
                .when(severalMessagesAreSendAsynchronouslyButWillBeBlocked(numberOfParallelSender, 5)
                        .andThen(aShortWaitIsDone(10, MILLISECONDS))
                        .andThen(theNumberOfAcceptedMessagesIsQueriedAsynchronously()))
                .then(expectResultToBe(numberOfParallelSender));
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
        final int numberOfParallelSender = 3;
        given(aConfiguredChannel(testConfig)
                .withASubscriberThatBlocksWhenAccepting())
                .when(severalMessagesAreSendAsynchronouslyButWillBeBlocked(numberOfParallelSender, 5)
                        .andThen(aShortWaitIsDone(10, MILLISECONDS))
                        .andThen(theNumberOfCurrentlyDeliveredMessagesIsQueried()))
                .then(expectResultToBe(numberOfParallelSender));
    }

    //shutdown
    @Test
    public void testChannel_whenShutdown_deliversRemainingMessagesButNoNewAdded(final ChannelTestConfig testConfig) throws Exception {
        given(aConfiguredChannel(testConfig))
                .when(theChannelIsShutdownAfterHalfOfTheMessagesWereDelivered(10))
                .then(expectXMessagesToBeDelivered(5));
    }

    @Test
    public void testChannel_whenShutdownWithoutFinishingRemainingTasksIsCalled_noTasksAreFinished(final ChannelTestConfig testConfig) throws Exception {
        final int numberOfParallelSendMessage = 5;
        given(aConfiguredChannel(testConfig))
                .when(theChannelIsShutdownAfterHalfOfTheMessagesWereDelivered_withoutFinishingRemainingTasks(10))
                .then(expectXMessagesToBeDelivered(numberOfParallelSendMessage));
    }

}
