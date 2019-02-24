package com.envimate.messageMate.channel;

import com.envimate.messageMate.channel.config.AsynchronousChannelConfigResolver;
import com.envimate.messageMate.channel.config.ChannelTestConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.envimate.messageMate.channel.config.ChannelTestConfig.ASYNCHRONOUS_CHANNEL_CONFIG_POOL_SIZE;
import static com.envimate.messageMate.channel.givenWhenThen.ChannelActionBuilder.*;
import static com.envimate.messageMate.channel.givenWhenThen.ChannelSetupBuilder.aConfiguredChannel;
import static com.envimate.messageMate.channel.givenWhenThen.ChannelValidationBuilder.*;
import static com.envimate.messageMate.channel.givenWhenThen.Given.given;

@ExtendWith(AsynchronousChannelConfigResolver.class)
public class AsynchronousChannelSpecs implements ChannelSpecs {

    //statistics
    @Test
    public void testChannel_canQueuedMessages(final ChannelTestConfig channelTestConfig) {
        final int expectedQueuedMessage = 3;
        final int numberOfSendMessages = ASYNCHRONOUS_CHANNEL_CONFIG_POOL_SIZE + expectedQueuedMessage;
        given(aConfiguredChannel(channelTestConfig)
                .withABlockingSubscriber())
                .when(severalMessagesAreSendAsynchronously(numberOfSendMessages)
                        .andThen(theNumberOfQueuedMessagesIsQueried()))
                .then(expectTheResult(expectedQueuedMessage));
    }

    //shutdown
    @Test
    public void testChannel_closeWithoutFinishingRemainingTasks_hasNoEffectForSynchronousConfig(final ChannelTestConfig channelTestConfig) {
        final int expectedQueuedMessage = 3;
        final int numberOfSendMessages = ASYNCHRONOUS_CHANNEL_CONFIG_POOL_SIZE + expectedQueuedMessage;
        given(aConfiguredChannel(channelTestConfig)
                .withABlockingSubscriber())
                .when(severalMessagesAreSendAsynchronouslyBeforeTheChannelIsClosedWithoutFinishingRemainingTasks(numberOfSendMessages)
                        .andThen(theSubscriberLockIsReleased()
                                .andThen(theNumberOfSuccessfulDeliveredMessagesIsQueried())))
                .then(expectTheResult(ASYNCHRONOUS_CHANNEL_CONFIG_POOL_SIZE)
                        .and(expectTheChannelToBeShutdown()));
    }

    //await
    @Test
    public void testChannel_awaitsWithoutFinishingTasks_succeedsDespiteNotFinished(final ChannelTestConfig channelTestConfig) {
        final int numberOfMessages = ASYNCHRONOUS_CHANNEL_CONFIG_POOL_SIZE + 5;
        given(aConfiguredChannel(channelTestConfig)
                .withABlockingSubscriber())
                .when(sendMessagesBeforeTheShutdownIsAwaitedWithoutFinishingTasks(numberOfMessages))
                .then(expectTheShutdownToBeFailed());
    }
}
