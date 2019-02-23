package com.envimate.messageMate.messageBus;

import com.envimate.messageMate.messageBus.config.MessageBusTestConfig;
import com.envimate.messageMate.messageBus.config.QueuedAcceptingStrategyMessageBusConfigurationResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.envimate.messageMate.messageBus.givenWhenThen.MessageBusActionBuilder.*;
import static com.envimate.messageMate.messageBus.givenWhenThen.MessageBusSetupBuilder.aConfiguredMessageBus;
import static com.envimate.messageMate.messageBus.givenWhenThen.MessageBusValidationBuilder.expectResultToBe;
import static com.envimate.messageMate.messageBus.givenWhenThen.MessageBusValidationBuilder.expectXMessagesToBeDelivered;
import static com.envimate.messageMate.messageBus.givenWhenThen.Given.given;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@ExtendWith(QueuedAcceptingStrategyMessageBusConfigurationResolver.class)
public class QueuingMessagesAcceptingMessageBusSpecs /*implements MessageBusSpecs*/ {

    //TODO: check if correct or move again
    /*@Test
    public void testMessageBus_returnsCorrectNumberOfWaitingMessages(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        Given.given(MessageBusSetupBuilder.aMessageBus()
                .configuredWith(messageBusTestConfig)
                .withASubscriberThatBlocksWhenAccepting())
                .when(severalMessagesAreSendAsynchronously(3, 5)
                        .andThen(MessageBusActionBuilder.theNumberOfQueuedMessagesIsQueried()))
                .then(MessageBusValidationBuilder.expectResultToBe(14));
    }
*/


    //messageStatistics
    @Test
    public void testMessageBus_withBlockingSubscriber_whenNumberOfSuccessfulDeliveredMessagesIsQueried(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withASubscriberThatBlocksWhenAccepting())
                .when(severalMessagesAreSendAsynchronouslyButWillBeBlocked(3, 5)
                        .andThen(theNumberOfSuccessfulMessagesIsQueried()))
                .then(expectResultToBe(0));
    }

    @Test
    public void testMessageBus_withBlockingSubscriber_whenNumberOfAcceptedMessagesIsQueried(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        final int numberOfParallelSender = 3;
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withASubscriberThatBlocksWhenAccepting())
                .when(severalMessagesAreSendAsynchronouslyButWillBeBlocked(3, 5)
                        .andThen(aShortWaitIsDone(10, MILLISECONDS))
                        .andThen(theNumberOfAcceptedMessagesIsQueriedAsynchronously()))
                .then(expectResultToBe(numberOfParallelSender));
    }

    @Test
    public void testMessageBus_withBlockingSubscriber_whenNumberOfWaitingMessagesIsQueried(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        //message transport is always started -> never queued
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withASubscriberThatBlocksWhenAccepting())
                .when(severalMessagesAreSendAsynchronouslyButWillBeBlocked(3, 5)
                        .andThen(aShortWaitIsDone(10, MILLISECONDS))
                        .andThen(theNumberOfQueuedMessagesIsQueried()))
                .then(expectResultToBe(0));
    }

    //shutdown
    @Test
    public void testMessageBus_whenShutdown_deliversRemainingMessagesButNoNewAdded(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        final int numberOfParallelSendMessages = 5;
        given(aConfiguredMessageBus(messageBusTestConfig))
                .when(theBusIsShutdownAfterHalfOfTheMessagesWereDelivered(10))
                .then(expectXMessagesToBeDelivered(numberOfParallelSendMessages));
    }

    @Test
    public void testMessageBus_whenShutdownWithoutFinishingRemainingTasksIsCalled(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        final int numberOfParallelSendMessages = 5;
        given(aConfiguredMessageBus(messageBusTestConfig))
                .when(theBusIsShutdownAfterHalfOfTheMessagesWereDelivered_withoutFinishingRemainingTasks(10))
                .then(expectXMessagesToBeDelivered(numberOfParallelSendMessages));
    }
}
