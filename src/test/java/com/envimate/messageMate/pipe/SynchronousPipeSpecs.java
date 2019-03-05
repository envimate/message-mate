package com.envimate.messageMate.pipe;

import com.envimate.messageMate.pipe.config.PipeTestConfig;
import com.envimate.messageMate.pipe.config.SynchronisedPipeConfigurationResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.envimate.messageMate.pipe.givenWhenThen.Given.given;
import static com.envimate.messageMate.pipe.givenWhenThen.PipeActionBuilder.*;
import static com.envimate.messageMate.pipe.givenWhenThen.PipeSetupBuilder.aConfiguredPipe;
import static com.envimate.messageMate.pipe.givenWhenThen.PipeValidationBuilder.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@ExtendWith(SynchronisedPipeConfigurationResolver.class)
public class SynchronousPipeSpecs implements PipeSpecs {


    //messageStatistics
    @Test
    public void testPipe_withBlockingSubscriber_whenNumberOfSuccessfulDeliveredMessagesIsQueried_returnsZero(final PipeTestConfig testConfig) throws Exception {
        given(aConfiguredPipe(testConfig)
                .withASubscriberThatBlocksWhenAccepting())
                .when(severalMessagesAreSendAsynchronouslyButWillBeBlocked(3, 5)
                        .andThen(theNumberOfSuccessfulMessagesIsQueried()))
                .then(expectResultToBe(0));
    }

    @Test
    public void testPipe_withBlockingSubscriber_whenNumberOfAcceptedMessagesIsQueried_returnsNumberOfThreads(final PipeTestConfig testConfig) throws Exception {
        final int numberOfParallelSends = 3;
        given(aConfiguredPipe(testConfig)
                .withASubscriberThatBlocksWhenAccepting())
                .when(severalMessagesAreSendAsynchronouslyButWillBeBlocked(numberOfParallelSends, 5)
                        .andThen(aShortWaitIsDone(10, MILLISECONDS))
                        .andThen(theNumberOfAcceptedMessagesIsQueriedAsynchronously()))
                .then(expectResultToBe(numberOfParallelSends));
    }

    @Test
    public void testPipe_withBlockingSubscriber_whenNumberOfQueuedMessagesIsQueried_returnsNumberOfThreads(final PipeTestConfig testConfig) throws Exception {
        given(aConfiguredPipe(testConfig)
                .withASubscriberThatBlocksWhenAccepting())
                .when(severalMessagesAreSendAsynchronouslyButWillBeBlocked(3, 5)
                        .andThen(theNumberOfQueuedMessagesIsQueried()))
                .then(expectResultToBe(0));
    }

    //shutdown
    @Test
    public void testPipe_whenShutdown_deliversRemainingMessagesButNoNewAdded(final PipeTestConfig testConfig) throws Exception {
        final int numberOfParallelSendMessagesBeforeShutdown = 5;
        given(aConfiguredPipe(testConfig))
                .when(thePipeIsShutdownAfterHalfOfTheMessagesWereDelivered(10))
                .then(expectXMessagesToBeDelivered_despiteTheChannelClosed(numberOfParallelSendMessagesBeforeShutdown));
        //because waiting senders wait on synchronised send -> they never entered the Pipe and do not count as remaining
    }

    @Test
    public void testPipe_whenShutdownWithoutFinishingRemainingTasksIsCalled_noTasksAreFinished(final PipeTestConfig testConfig) throws Exception {
        final int numberOfParallelSendMessagesBeforeShutdown = 5;
        given(aConfiguredPipe(testConfig))
                .when(thePipeIsShutdownAfterHalfOfTheMessagesWereDelivered_withoutFinishingRemainingTasks(10))
                .then(expectXMessagesToBeDelivered_despiteTheChannelClosed(numberOfParallelSendMessagesBeforeShutdown));
    }


    //await
    @Test
    public void testPipe_awaitsFailsWhenAllTasksCouldBeDone(final PipeTestConfig testConfig) throws Exception {
        final int numberOfMessagesSend = 5;
        given(aConfiguredPipe(testConfig))
                .when(awaitIsCalledWithoutExpectingTasksToFinish(numberOfMessagesSend))
                .then(expectTheAwaitToBeTerminatedSuccessful(numberOfMessagesSend));
        //await returns always true, as it's not feasible to check whether there are still Threads waiting in the pipe
    }

}
