package com.envimate.messageMate.pipe;

import com.envimate.messageMate.pipe.config.AsynchronousDeliveryPipeConfigurationResolver;
import com.envimate.messageMate.pipe.config.PipeTestConfig;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.envimate.messageMate.pipe.config.PipeTestConfig.ASYNCHRONOUS_DELIVERY_POOL_SIZE;
import static com.envimate.messageMate.pipe.givenWhenThen.PipeActionBuilder.*;
import static com.envimate.messageMate.pipe.givenWhenThen.PipeSetupBuilder.aConfiguredPipe;
import static com.envimate.messageMate.pipe.givenWhenThen.PipeValidationBuilder.expectResultToBe;
import static com.envimate.messageMate.pipe.givenWhenThen.PipeValidationBuilder.expectXMessagesToBeDelivered;
import static com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.Given.given;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@ExtendWith(AsynchronousDeliveryPipeConfigurationResolver.class)
public class AsynchronousDeliveryPipeSpecs implements PipeSpecs {

    //messageStatistics
    @Test
    public void testPipe_withBlockingSubscriber_whenNumberOfSuccessfulDeliveredMessagesIsQueried_returnsZero(final PipeTestConfig testConfig) throws Exception {
        given(aConfiguredPipe(testConfig)
                .withASubscriberThatBlocksWhenAccepting())
                .when(severalMessagesAreSendAsynchronously(3, 5)
                        .andThen(theNumberOfSuccessfulMessagesIsQueried()))
                .then(expectResultToBe(0));
    }

    @Test
    public void testPipe_withBlockingSubscriber_whenNumberOfWaitingMessagesIsQueried_returnsZero(final PipeTestConfig testConfig) throws Exception {
        given(aConfiguredPipe(testConfig)
                .withASubscriberThatBlocksWhenAccepting())
                .when(severalMessagesAreSendAsynchronously(3, 5)
                        .andThen(theNumberOfWaitingMessagesIsQueried()))
                .then(expectResultToBe(0));
    }

    @Test
    public void testPipe_withBlockingSubscriber_whenNumberOfAcceptedMessagesIsQueried_returnsOnlyOne(final PipeTestConfig testConfig) throws Exception {
        given(aConfiguredPipe(testConfig)
                .withASubscriberThatBlocksWhenAccepting())
                .when(severalMessagesAreSendAsynchronously(3, 5)
                        .andThen(theNumberOfAcceptedMessagesIsQueried()))
                .then(expectResultToBe(15));
    }

    @Test
    public void testPipe_withBlockingSubscriber_whenNumberOfCurrentlyTransportedMessagesIsQueried_returnsZero(final PipeTestConfig testConfig) throws Exception {
        given(aConfiguredPipe(testConfig)
                .withASubscriberThatBlocksWhenAccepting())
                .when(severalMessagesAreSendAsynchronously(3, 5)
                        .andThen(theNumberOfCurrentlyTransportedMessagesIsQueried()))
                .then(expectResultToBe(0));
    }

    @Test
    public void testPipe_withBlockingSubscriber_whenNumberOfCurrentlyDeliveredMessagesIsQueried_deliversTooMuchForUnboundedQueue(final PipeTestConfig testConfig) throws Exception {
        given(aConfiguredPipe(testConfig)
                .withASubscriberThatBlocksWhenAccepting())
                .when(severalMessagesAreSendAsynchronouslyButWillBeBlocked(3, 5)
                        .andThen(aShortWaitIsDone(100, MILLISECONDS))
                        .andThen(theNumberOfCurrentlyDeliveredMessagesIsQueried()))
                .then(expectResultToBe(3 * 5));
    }

    //shutdown
    @Test
    public void testPipe_whenShutdown_deliversRemainingMessagesButNoNewAdded(final PipeTestConfig testConfig) throws Exception {
        given(aConfiguredPipe(testConfig))
                .when(thePipeIsShutdownAfterHalfOfTheMessagesWereDelivered(10)
                        .andThen(aShortWaitIsDone(10, MILLISECONDS)))
                .then(expectXMessagesToBeDelivered(5));
    }

    @Test
    public void testPipe_whenShutdownWithoutFinishingRemainingTasksIsCalled_noNewTasksAreFinished(final PipeTestConfig testConfig) throws Exception {
        given(aConfiguredPipe(testConfig))
                .when(thePipeIsShutdownAfterHalfOfTheMessagesWereDelivered_withoutFinishingRemainingTasks(10)
                        .andThen(aShortWaitIsDone(10, MILLISECONDS)))
                .then(expectXMessagesToBeDelivered(ASYNCHRONOUS_DELIVERY_POOL_SIZE));
    }

}
