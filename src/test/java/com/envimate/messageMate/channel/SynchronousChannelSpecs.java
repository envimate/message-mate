package com.envimate.messageMate.channel;

import com.envimate.messageMate.channel.config.ChannelTestConfig;
import com.envimate.messageMate.channel.config.SynchronousChannelConfigResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.envimate.messageMate.channel.givenWhenThen.ChannelActionBuilder.*;
import static com.envimate.messageMate.channel.givenWhenThen.ChannelSetupBuilder.aConfiguredChannel;
import static com.envimate.messageMate.channel.givenWhenThen.ChannelValidationBuilder.*;
import static com.envimate.messageMate.channel.givenWhenThen.Given.given;

@ExtendWith(SynchronousChannelConfigResolver.class)
public class SynchronousChannelSpecs implements ChannelSpecs {

    //statistics
    @Test
    public void testChannel_canQueuedMessages(final ChannelTestConfig channelTestConfig) {
        final int numberOfSendMessages = 5;
        given(aConfiguredChannel(channelTestConfig)
                .withABlockingSubscriber())
                .when(severalMessagesAreSendAsynchronously(numberOfSendMessages)
                        .andThen(theNumberOfQueuedMessagesIsQueried()))
                .then(expectTheResult(0));
    }

    //shutdown
    @Test
    public void testChannel_closeWithoutFinishingRemainingTasks_hasNoEffectForSynchronousConfig(final ChannelTestConfig channelTestConfig) {
        final int numberOfSendMessages = 5;
        given(aConfiguredChannel(channelTestConfig)
                .withABlockingSubscriber())
                .when(severalMessagesAreSendAsynchronouslyBeforeTheChannelIsClosedWithoutFinishingRemainingTasks(numberOfSendMessages)
                        .andThen(theSubscriberLockIsReleased()
                                .andThen(theNumberOfSuccessfulDeliveredMessagesIsQueried())))
                .then(expectTheResult(numberOfSendMessages)
                        .and(expectTheChannelToBeShutdown()));
    }

    //await
    @Test
    public void testChannel_awaitsWithoutFinishingTasks_succeedsDespiteNotFinished(final ChannelTestConfig channelTestConfig) {
        final int numberOfMessages = 5;
        given(aConfiguredChannel(channelTestConfig)
                .withABlockingSubscriber())
                .when(severalMessagesAreSendAsynchronouslyBeforeTheChannelIsClosedWithoutFinishingRemainingTasks(numberOfMessages)
                        .andThen(theShutdownIsAwaited()))
                .then(expectTheShutdownToBeSucceededInTime());
    }
}
