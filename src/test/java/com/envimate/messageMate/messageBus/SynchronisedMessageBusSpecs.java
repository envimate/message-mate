package com.envimate.messageMate.messageBus;

import com.envimate.messageMate.messageBus.config.MessageBusTestConfig;
import com.envimate.messageMate.messageBus.config.SynchronisedMessageBusConfigurationResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.envimate.messageMate.messageBus.givenWhenThen.MessageBusActionBuilder.*;
import static com.envimate.messageMate.messageBus.givenWhenThen.MessageBusSetupBuilder.aConfiguredMessageBus;
import static com.envimate.messageMate.messageBus.givenWhenThen.MessageBusValidationBuilder.*;
import static com.envimate.messageMate.messageBus.givenWhenThen.Given.given;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@ExtendWith(SynchronisedMessageBusConfigurationResolver.class)
public class SynchronisedMessageBusSpecs implements MessageBusSpecs {


    //messageStatistics
    //@Test
    public void testMessageBus_withBlockingSubscriber_whenNumberOfSuccessfulDeliveredMessagesIsQueried_returnsZero(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withASubscriberThatBlocksWhenAccepting())
                .when(severalMessagesAreSendAsynchronouslyButWillBeBlocked(3, 5)
                        .andThen(theNumberOfSuccessfulMessagesIsQueried()))
                .then(expectResultToBe(0));
    }

    //@Test
    public void testMessageBus_withBlockingSubscriber_whenNumberOfAcceptedMessagesIsQueried_returnsOnlyOne(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withASubscriberThatBlocksWhenAccepting())
                .when(severalMessagesAreSendAsynchronouslyButWillBeBlocked(3, 5)
                        .andThen(aShortWaitIsDone(10, MILLISECONDS))
                        .andThen(theNumberOfAcceptedMessagesIsQueriedAsynchronously()))
                .then(expectResultToBe(1));
    }

    //@Test
    public void testMessageBus_withBlockingSubscriber_whenNumberOfWaitingMessagesIsQueried_returnsZero(final MessageBusTestConfig messageBusTestConfig) throws Exception {
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
        given(aConfiguredMessageBus(messageBusTestConfig))
                .when(theBusIsShutdownAfterHalfOfTheMessagesWereDelivered(10))
                .then(expectXMessagesToBeDelivered(1));
        //because waiting senders wait on synchronised send -> they never entered the Bus and do not count as remaining
    }

    @Test
    public void testMessageBus_whenShutdownWithoutFinishingRemainingTasksIsCalled(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig))
                .when(theBusIsShutdownAfterHalfOfTheMessagesWereDelivered_withoutFinishingRemainingTasks(10))
                .then(expectXMessagesToBeDelivered(1));
    }

}
