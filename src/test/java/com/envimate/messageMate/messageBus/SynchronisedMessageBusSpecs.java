package com.envimate.messageMate.messageBus;

import com.envimate.messageMate.messageBus.config.MessageBusTestConfig;
import com.envimate.messageMate.messageBus.config.SynchronisedMessageBusConfigurationResolver;
import com.envimate.messageMate.shared.subscriber.TestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.envimate.messageMate.messageBus.givenWhenThen.Given.given;
import static com.envimate.messageMate.messageBus.givenWhenThen.MessageBusActionBuilder.*;
import static com.envimate.messageMate.messageBus.givenWhenThen.MessageBusSetupBuilder.aConfiguredMessageBus;
import static com.envimate.messageMate.messageBus.givenWhenThen.MessageBusValidationBuilder.*;

@ExtendWith(SynchronisedMessageBusConfigurationResolver.class)
public class SynchronisedMessageBusSpecs implements MessageBusSpecs {


    //messageStatistics
    @Test
    public void testMessageBus_queryingNumberOfQueuedMessages_alwaysReturnsZero(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        final int messagesSendParallel = 3;
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withASubscriberThatBlocksWhenAccepting())
                .when(severalMessagesAreSendAsynchronouslyButWillBeBlocked(messagesSendParallel, 1)
                        .andThen(theNumberOfQueuedMessagesIsQueried()))
                .then(expectResultToBe(0));
    }

    //shutdown
    @Test
    public void testMessageBus_whenShutdownAllRemainingTasksAreFinished(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        final int numberOfParallelSendMessages = 10;
        final boolean finishRemainingTasks = true;
        given(aConfiguredMessageBus(messageBusTestConfig))
                .when(sendSeveralMessagesBeforeTheBusIsShutdown(numberOfParallelSendMessages, finishRemainingTasks))
                .then(expectXMessagesToBeDelivered(10));
    }

    @Test
    public void testMessageBus_whenShutdownWithoutFinishingRemainingTasks_allTasksAreStillFinished(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        final int numberOfParallelSendMessages = 10;
        final boolean finishRemainingTasks = false;
        given(aConfiguredMessageBus(messageBusTestConfig))
                .when(sendSeveralMessagesBeforeTheBusIsShutdown(numberOfParallelSendMessages, finishRemainingTasks))
                .then(expectXMessagesToBeDelivered(10));
    }

    //errors
    @Test
    public void testMessageBus_dynamicErrorHandlerIsCalledEvenIfMessageBusExceptionHandlerThrowsException(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withAnErrorThrowingSubscriber()
                .withADynamicErrorListenerAndAnErrorThrowingExceptionHandler())
                .when(aSingleMessageIsSend())
                .then(expectTheExceptionHandledAndTheErrorToBeThrown(TestException.class));
    }

}
