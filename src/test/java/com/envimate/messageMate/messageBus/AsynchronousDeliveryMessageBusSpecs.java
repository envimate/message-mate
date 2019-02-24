package com.envimate.messageMate.messageBus;

import com.envimate.messageMate.messageBus.config.AsynchronousDeliveryMessageBusConfigurationResolver;
import com.envimate.messageMate.messageBus.config.MessageBusTestConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.envimate.messageMate.messageBus.config.MessageBusTestConfig.ASYNCHRONOUS_DELIVERY_POOL_SIZE;
import static com.envimate.messageMate.messageBus.givenWhenThen.Given.given;
import static com.envimate.messageMate.messageBus.givenWhenThen.MessageBusActionBuilder.*;
import static com.envimate.messageMate.messageBus.givenWhenThen.MessageBusSetupBuilder.aConfiguredMessageBus;
import static com.envimate.messageMate.messageBus.givenWhenThen.MessageBusValidationBuilder.expectResultToBe;
import static com.envimate.messageMate.messageBus.givenWhenThen.MessageBusValidationBuilder.expectXMessagesToBeDelivered;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@ExtendWith(AsynchronousDeliveryMessageBusConfigurationResolver.class)
public class AsynchronousDeliveryMessageBusSpecs implements MessageBusSpecs {


    @Test
    public void testMessageBus_queryingNumberOfQueuedMessages_alwaysReturnsZero(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        final int expectedQueuedMessages = 5;
        final int messagesSendParallel = MessageBusTestConfig.ASYNCHRONOUS_DELIVERY_POOL_SIZE + expectedQueuedMessages;
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withASubscriberThatBlocksWhenAccepting())
                .when(severalMessagesAreSendAsynchronouslyButWillBeBlocked(messagesSendParallel, 1)
                        .andThen(theNumberOfQueuedMessagesIsQueried()))
                .then(expectResultToBe(expectedQueuedMessages));
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
        final int numberOfParallelSendMessages = ASYNCHRONOUS_DELIVERY_POOL_SIZE + 3;
        final boolean finishRemainingTasks = false;
        given(aConfiguredMessageBus(messageBusTestConfig))
                .when(sendSeveralMessagesBeforeTheBusIsShutdown(numberOfParallelSendMessages, finishRemainingTasks))
                .then(expectXMessagesToBeDelivered(ASYNCHRONOUS_DELIVERY_POOL_SIZE));
    }
}
