package com.envimate.messageMate.messageBus;

import com.envimate.messageMate.messageBus.config.AsynchronousDeliveryMessageBusConfigurationResolver;
import com.envimate.messageMate.messageBus.config.MessageBusTestConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.envimate.messageMate.messageBus.config.MessageBusTestConfig.ASYNCHRONOUS_DELIVERY_POOL_SIZE;
import static com.envimate.messageMate.messageBus.givenWhenThen.MessageBusActionBuilder.*;
import static com.envimate.messageMate.messageBus.givenWhenThen.MessageBusSetupBuilder.aConfiguredMessageBus;
import static com.envimate.messageMate.messageBus.givenWhenThen.MessageBusValidationBuilder.expectResultToBe;
import static com.envimate.messageMate.messageBus.givenWhenThen.MessageBusValidationBuilder.expectXMessagesToBeDelivered;
import static com.envimate.messageMate.shared.channelMessageBus.givenWhenThen.Given.given;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@ExtendWith(AsynchronousDeliveryMessageBusConfigurationResolver.class)
public class AsynchronousDeliveryMessageBusSpecs implements MessageBusSpecs {

    //messageStatistics
    @Test
    public void testMessageBus_withBlockingSubscriber_whenNumberOfSuccessfulDeliveredMessagesIsQueried_returnsZero(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withASubscriberThatBlocksWhenAccepting())
                .when(severalMessagesAreSendAsynchronously(3, 5)
                        .andThen(theNumberOfSuccessfulMessagesIsQueried()))
                .then(expectResultToBe(0));
    }

    @Test
    public void testMessageBus_withBlockingSubscriber_whenNumberOfWaitingMessagesIsQueried_returnsZero(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withASubscriberThatBlocksWhenAccepting())
                .when(severalMessagesAreSendAsynchronously(3, 5)
                        .andThen(theNumberOfWaitingMessagesIsQueried()))
                .then(expectResultToBe(0));
    }

    @Test
    public void testMessageBus_withBlockingSubscriber_whenNumberOfAcceptedMessagesIsQueried_returnsOnlyOne(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withASubscriberThatBlocksWhenAccepting())
                .when(severalMessagesAreSendAsynchronously(3, 5)
                        .andThen(theNumberOfAcceptedMessagesIsQueried()))
                .then(expectResultToBe(15));
    }

    @Test
    public void testMessageBus_withBlockingSubscriber_whenNumberOfCurrentlyTransportedMessagesIsQueried_returnsZero(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withASubscriberThatBlocksWhenAccepting())
                .when(severalMessagesAreSendAsynchronously(3, 5)
                        .andThen(theNumberOfCurrentlyTransportedMessagesIsQueried()))
                .then(expectResultToBe(0));
    }

    @Test
    public void testMessageBus_withBlockingSubscriber_whenNumberOfCurrentlyDeliveredMessagesIsQueried_deliversTooMuchForUnboundedQueue(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withASubscriberThatBlocksWhenAccepting())
                .when(severalMessagesAreSendAsynchronouslyButWillBeBlocked(3, 5)
                        .andThen(aShortWaitIsDone(100, MILLISECONDS))
                        .andThen(theNumberOfCurrentlyDeliveredMessagesIsQueried()))
                .then(expectResultToBe(3 * 5));
    }

    //shutdown
    @Test
    public void testMessageBus_whenShutdown_deliversRemainingMessagesButNoNewAdded(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig))
                .when(theBusIsShutdownAfterHalfOfTheMessagesWereDelivered(10)
                        .andThen(aShortWaitIsDone(10, MILLISECONDS)))
                .then(expectXMessagesToBeDelivered(5));
    }

    @Test
    public void testMessageBus_whenShutdownWithoutFinishingRemainingTasksIsCalled_noNewTasksAreFinished(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig))
                .when(theBusIsShutdownAfterHalfOfTheMessagesWereDelivered_withoutFinishingRemainingTasks(10)
                        .andThen(aShortWaitIsDone(10, MILLISECONDS)))
                .then(expectXMessagesToBeDelivered(ASYNCHRONOUS_DELIVERY_POOL_SIZE));
    }

}
