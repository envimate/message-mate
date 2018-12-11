package com.envimate.messageMate.messageBus;

import com.envimate.messageMate.messageBus.config.MessageBusTestConfig;
import com.envimate.messageMate.messageBus.config.SynchronisedMessageBusConfigurationResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.envimate.messageMate.messageBus.givenWhenThen.MessageBusActionBuilder.*;
import static com.envimate.messageMate.messageBus.givenWhenThen.MessageBusSetupBuilder.aMessageBus;
import static com.envimate.messageMate.messageBus.givenWhenThen.MessageBusValidationBuilder.*;
import static com.envimate.messageMate.shared.givenWhenThen.Given.given;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@ExtendWith(SynchronisedMessageBusConfigurationResolver.class)
public class SynchronisedMessageBusSpecs implements MessageBusSpecs {


    //subscribing
    @Test
    public void testMessageBus_subscriberCanInterruptDeliveringMessage_whenDeliveryIsSynchronous(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aMessageBus()
                .configuredWith(messageBusTestConfig)
                .withSeveralDeliveryInterruptingSubscriber(5))
                .when(severalMessagesAreSend(10))
                .then(expectEachMessagesToBeReceivedByOnlyOneSubscriber());
    }

    //messageStatistics
    @Test
    public void testMessageBus_withBlockingSubscriber_whenNumberOfSuccessfulDeliveredMessagesIsQueried_returnsZero(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aMessageBus()
                .configuredWith(messageBusTestConfig)
                .withASubscriberThatBlocksWhenAccepting())
                .when(severalMessagesAreSendAsynchronouslyButWillBeBlocked(3, 5)
                        .andThen(theNumberOfSuccessfulMessagesIsQueried()))
                .then(expectResultToBe(0));
    }

    @Test
    public void testMessageBus_withBlockingSubscriber_whenNumberOfAcceptedMessagesIsQueried_returnsOnlyOne(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aMessageBus()
                .configuredWith(messageBusTestConfig)
                .withASubscriberThatBlocksWhenAccepting())
                .when(severalMessagesAreSendAsynchronouslyButWillBeBlocked(3, 5)
                        .andThen(aShortWaitIsDone(10, MILLISECONDS))
                        .andThen(theNumberOfAcceptedMessagesIsQueriedAsynchronously()))
                .then(expectResultToBe(1));
    }

    @Test
    public void testMessageBus_withBlockingSubscriber_whenNumberOfWaitingMessagesIsQueried_returnsZero(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aMessageBus()
                .configuredWith(messageBusTestConfig)
                .withASubscriberThatBlocksWhenAccepting())
                .when(severalMessagesAreSendAsynchronouslyButWillBeBlocked(3, 5)
                        .andThen(aShortWaitIsDone(10, MILLISECONDS))
                        .andThen(theNumberOfWaitingMessagesIsQueried()))
                .then(expectResultToBe(0));
    }

    @Test
    public void testMessageBus_withBlockingSubscriber_whenNumberOfCurrentlyTransportedMessagesIsQueried_returnsZero(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aMessageBus()
                .configuredWith(messageBusTestConfig)
                .withASubscriberThatBlocksWhenAccepting())
                .when(severalMessagesAreSendAsynchronouslyButWillBeBlocked(3, 5)
                        .andThen(aShortWaitIsDone(10, MILLISECONDS))
                        .andThen(theNumberOfCurrentlyTransportedMessagesIsQueried()))
                .then(expectResultToBe(0));
    }

    @Test
    public void testMessageBus_withBlockingSubscriber_whenNumberOfCurrentlyDeliveredMessagesIsQueried_returnsZero(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aMessageBus()
                .configuredWith(messageBusTestConfig)
                .withASubscriberThatBlocksWhenAccepting())
                .when(severalMessagesAreSendAsynchronouslyButWillBeBlocked(3, 5)
                        .andThen(aShortWaitIsDone(10, MILLISECONDS))
                        .andThen(theNumberOfCurrentlyDeliveredMessagesIsQueried()))
                .then(expectResultToBe(1));
    }

    //shutdown
    @Test
    public void testMessageBus_whenShutdown_deliversRemainingMessagesButNoNewAdded(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aMessageBus()
                .configuredWith(messageBusTestConfig))
                .when(theBusIsShutdownAfterHalfOfTheMessagesWereDelivered(10))
                .then(expectXMessagesToBeDelivered(1));
        //because waiting senders wait on synchronised send -> they never entered the Bus and do not count as remaining
    }

    @Test
    public void testMessageBus_whenShutdownWithoutFinishingRemainingTasksIsCalled(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aMessageBus()
                .configuredWith(messageBusTestConfig))
                .when(theBusIsShutdownAfterHalfOfTheMessagesWereDelivered_withoutFinishingRemainingTasks(10))
                .then(expectXMessagesToBeDelivered(1));
    }

}
