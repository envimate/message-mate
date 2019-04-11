/*
 * Copyright (c) 2018 envimate GmbH - https://envimate.com/.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.envimate.messageMate.messageBus;


import com.envimate.messageMate.exceptions.AlreadyClosedException;
import com.envimate.messageMate.internal.enforcing.MustNotBeNullException;
import com.envimate.messageMate.messageBus.config.MessageBusTestConfig;
import com.envimate.messageMate.processingContext.EventType;
import com.envimate.messageMate.shared.subscriber.TestException;
import com.envimate.messageMate.shared.testMessages.TestMessageOfInterest;
import org.junit.jupiter.api.Test;

import static com.envimate.messageMate.messageBus.givenWhenThen.Given.given;
import static com.envimate.messageMate.messageBus.givenWhenThen.MessageBusActionBuilder.*;
import static com.envimate.messageMate.messageBus.givenWhenThen.MessageBusSetupBuilder.aConfiguredMessageBus;
import static com.envimate.messageMate.messageBus.givenWhenThen.MessageBusValidationBuilder.*;
import static com.envimate.messageMate.shared.TestEventType.testEventType;
import static com.envimate.messageMate.shared.testMessages.TestMessageOfInterest.messageOfInterest;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public interface MessageBusSpecs {

    //Send and subscribe
    @Test
    default void testMessageBus_canSendAndReceiveASingleMessage(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withASingleSubscriber())
                .when(aSingleMessageIsSend())
                .then(expectTheMessageToBeReceived());
    }

    @Test
    default void testMessageBus_canSendAndReceiveSeveralMessagesWithSeveralSubscriber(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withSeveralSubscriber(5))
                .when(severalMessagesAreSend(10)
                        .andThen(aShortWaitIsDone(5, MILLISECONDS)))
                .then(expectAllMessagesToBeReceivedByAllSubscribers());
    }

    @Test
    default void testMessageBus_canSendMessageTwice(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        final TestMessageOfInterest message = messageOfInterest();
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withASingleSubscriber())
                .when(theMessageIsSend(message)
                        .andThen(theMessageIsSend(message)))
                .then(expectTheMessagesToBeReceivedByAllSubscriber(message, message));
    }


    @Test
    default void testMessageBus_canSendAndReceiveMessagesAsynchronously(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withSeveralSubscriber(5))
                .when(severalMessagesAreSendAsynchronously(5, 10)
                        .andThen(aShortWaitIsDone(5, MILLISECONDS)))
                .then(expectAllMessagesToBeReceivedByAllSubscribers());
    }

    @Test
    default void testMessageBus_canSendAMessageWithoutPayload(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withASingleRawSubscriber())
                .when(aMessageWithoutPayloadIsSend())
                .then(expectTheMessageToBeReceived());
    }

    @Test
    default void testMessageBus_throwsExceptionWhenEventTypeIsNotSet(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withASingleRawSubscriber())
                .when(aMessageWithoutEventType())
                .then(expectTheException(MustNotBeNullException.class));
    }
    //errorPayload
    @Test
    default void testMessageBus_canSendAndReceiveErrorPayload(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withASingleRawSubscriber())
                .when(aSingleMessageWithErrorPayloadIsSend())
                .then(expectTheErrorPayloadToBeReceived());
    }

    //unsubscribe
    @Test
    default void testMessageBus_canUnsubscribe(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withSeveralSubscriber(5))
                .when(oneSubscriberUnsubscribes())
                .then(expectAllRemainingSubscribersToStillBeSubscribed());
    }

    @Test
    default void testMessageBus_canUnsubscribeTwoSubscribers(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withSeveralSubscriber(5))
                .when(oneSubscriberUnsubscribes()
                        .andThen(oneSubscriberUnsubscribes()))
                .then(expectAllRemainingSubscribersToStillBeSubscribed());
    }

    @Test
    default void testMessageBus_canUnsubscribeTheSameSubscriberSeveralTimes(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withSeveralSubscriber(5))
                .when(oneSubscriberUnsubscribesSeveralTimes(2))
                .then(expectAllRemainingSubscribersToStillBeSubscribed());
    }

    //MessageId and CorrelationId
    @Test
    default void testMessageBus_sendMessageHasConstantMessageIdAndCanGenerateMatchingCorrelationId(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withASingleRawSubscriber())
                .when(aSingleMessageIsSend())
                .then(expectTheMessageToHaveTheSameMessageIdAndAMatchingGeneratedCorrelationId());
    }

    @Test
    default void testMessageBus_canSetCorrelationIdWhenSend(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withASingleRawSubscriber())
                .when(aMessageWithCorrelationIdIsSend())
                .then(expectTheCorrelationIdToBeSetWhenReceived());
    }

    @Test
    default void testMessageBus_canSendProcessingContextWithAMessageId(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withASingleRawSubscriber())
                .when(aMessageWithCorrelationIdIsSend())
                .then(expectTheMessageToHaveTheSameMessageIdAndAMatchingGeneratedCorrelationId());
    }

    @Test
    default void testMessageBus_canSubscriberForSpecificCorrelationIds(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withASubscriberForACorrelationId())
                .when(aMessageWithCorrelationIdIsSend())
                .then(expectTheMessageWrappedInProcessingContextToBeReceived());
    }

    @Test
    default void testMessageBus_canUnsubscribeForCorrelationId(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withASubscriberForACorrelationId())
                .when(theSubscriberForTheCorrelationIdUnsubscribes()
                        .andThen(aMessageWithCorrelationIdIsSend()))
                .then(expectNoMessagesToBeDelivered());
    }

    //filter
    @Test
    default void testMessageBus_allowsFiltersToChangeMessages(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withASingleSubscriber()
                .withAFilterThatChangesTheContentOfEveryMessage())
                .when(severalMessagesAreSend(5))
                .then(expectAllMessagesToHaveTheContentChanged());
    }

    @Test
    default void testMessageBus_allowsFiltersToDropMessages(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withSeveralSubscriber(3)
                .withAFilterThatDropsMessages())
                .when(halfValidAndInvalidMessagesAreSendAsynchronously(3, 10))
                .then(expectNoMessagesToBeDelivered());
    }

    @Test
    default void testMessageBus_whenAFilterDoesNotUseAMethod_messageIsDropped(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withSeveralSubscriber(3)
                .withAnInvalidFilterThatDoesNotUseAnyFilterMethods())
                .when(severalInvalidMessagesAreSendAsynchronously(3, 10))
                .then(expectNoMessagesToBeDelivered());
    }

    @Test
    default void testMessageBus_throwsExceptionForPositionBelowZero(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withAFilterAtAnInvalidPosition(-1))
                .when(aSingleMessageIsSend())
                .then(expectTheException(IndexOutOfBoundsException.class));
    }

    @Test
    default void testMessageBus_throwsExceptionForPositionGreaterThanAllowed(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withAFilterAtAnInvalidPosition(100))
                .when(aSingleMessageIsSend())
                .then(expectTheException(IndexOutOfBoundsException.class));
    }

    @Test
    default void testMessageBus_canQueryListOfFilter(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withTwoFilterOnSpecificPositions())
                .when(theListOfFiltersIsQueried())
                .then(expectAListWithAllFilters());
    }

    @Test
    default void testMessageBus_canRemoveFilter(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withTwoFilterOnSpecificPositions())
                .when(aFilterIsRemoved())
                .then(expectTheRemainingFilter());
    }

    @Test
    default void testMessageBus_allowsRawFiltersToCompleteProcessingContext(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withASingleRawSubscriber()
                .withARawFilterThatChangesCompleteProcessingContext())
                .when(severalMessagesAreSend(5))
                .then(expectAllProcessingContextsToBeReplaced());
    }

    //messageStatistics
    @Test
    default void testMessageBus_returnsCorrectNumberOfAcceptedMessages(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withASingleSubscriber())
                .when(severalMessagesAreSendAsynchronously(3, 5)
                        .andThen(theNumberOfAcceptedMessagesIsQueried()))
                .then(expectResultToBe(15));
    }

    // queued statistics config dependent

    @Test
    default void testMessageBus_whenAFilterDoesNotUseAMethod_theMessageIsMarkedAsForgotten(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withSeveralSubscriber(3)
                .withAnInvalidFilterThatDoesNotUseAnyFilterMethods())
                .when(aSingleMessageIsSend()
                        .andThen(theNumberOfForgottenMessagesIsQueried()))
                .then(expectResultToBe(1));
    }

    @Test
    default void testMessageBus_returnsCorrectNumberOfDroppedMessages(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withASingleSubscriber()
                .withAFilterThatDropsMessages())
                .when(severalInvalidMessagesAreSendAsynchronously(3, 5)
                        .andThen(theNumberOfBlockedMessagesIsQueried()))
                .then(expectResultToBe(15));
    }


    @Test
    default void testMessageBus_returnsCorrectNumberOfSuccessfulMessages(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withASingleSubscriber())
                .when(severalMessagesAreSendAsynchronously(3, 5)
                        .andThen(aShortWaitIsDone(10, MILLISECONDS))
                        .andThen(theNumberOfSuccessfulMessagesIsQueried()))
                .then(expectResultToBe(15));
    }

    @Test
    default void testMessageBus_returnsCorrectNumberOfDeliveryFailedMessages(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withAnExceptionThrowingSubscriber()
                .withACustomExceptionHandlerMarkingExceptionAsIgnored())
                .when(severalMessagesAreSendAsynchronously(3, 5)
                        .andThen(theNumberOfFailedMessagesIsQueried()))
                .then(expectResultToBe(0));
        //Is 0, because errors in event-type specific channels do not count for MB itself
    }

    @Test
    default void testMessageBus_returnsAValidTimestampForStatistics(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withoutASubscriber())
                .when(theTimestampOfTheStatisticsIsQueried())
                .then(expectTimestampToBeInTheLastXSeconds(3));
    }


    //subscribers
    @Test
    default void testMessageBus_returnsCorrectSubscribersPerType(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        final EventType eventTypeA = EventType.eventTypeFromString("A");
        final EventType eventTypeB = EventType.eventTypeFromString("B");
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withASubscriberForTyp(eventTypeA)
                .withASubscriberForTyp(eventTypeB)
                .withARawSubscriberForType(eventTypeA)
                .withASubscriberForTyp(eventTypeA))
                .when(theSubscriberAreQueriedPerType())
                .then(expectSubscriberOfType(3, eventTypeA)
                        .and(expectSubscriberOfType(1, eventTypeB)));
    }

    @Test
    default void testMessageBus_returnsCorrectSubscribersInList(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withASubscriberForTyp(EventType.eventTypeFromString("type1"))
                .withASubscriberForTyp(EventType.eventTypeFromString("type2")))
                .when(allSubscribersAreQueriedAsList())
                .then(expectAListOfSize(2));
    }

    //channel
    @Test
    default void testMessageBus_returnsCorrectChannel(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        final EventType eventType = testEventType();
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withACustomChannelFactory()
                .withASubscriberForTyp(eventType))
                .when(theChannelForTheTypeIsQueried(eventType))
                .then(expectTheCorrectChannel());
    }

    //shutdown
    @Test
    default void testMessageBus_canShutdown_evenIfIsBlocked(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withASubscriberThatBlocksWhenAccepting())
                .when(severalMessagesAreSendAsynchronouslyBeforeTheMessageBusIsShutdown(3, 5)
                        .andThen(theMessageBusShutdownIsExpectedForTimeoutInSeconds(1)))
                .then(expectTheMessageBusToBeShutdownInTime());
    }

    @Test
    default void testMessageBus_shutdownCallIsIdempotent(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withASubscriberThatBlocksWhenAccepting())
                .when(theMessageBusIsShutdownAsynchronouslyXTimes(6)
                        .andThen(theMessageBusIsShutdown()))
                .then(expectTheMessageBusToBeShutdown());
    }

    //error cases
    @Test
    default void testMessageBus_throwsExceptionWhenSendOnAClosedMessageBusIsCalled(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withAnExceptionAcceptingSubscriber())
                .when(theMessageBusIsShutdown()
                        .andThen(aSingleMessageIsSend()))
                .then(expectTheException(AlreadyClosedException.class));
    }

    @Test
    default void testMessageBus_customExceptionHandlerCanAccessExceptionsInsideFilterOfAcceptingPipe(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withAnExceptionThrowingFilter()
                .withACustomExceptionHandler())
                .when(aSingleMessageIsSend())
                .then(expectTheExceptionHandledAsFilterException(TestException.class));
    }

    @Test
    default void testMessageBus_customExceptionHandlerCanAccessExceptionsInsideFilterOfDeliveringPipes(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        final EventType eventType = testEventType();
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withACustomExceptionHandler())
                .when(aSubscriberIsAdded(eventType)
                        .andThen(anExceptionThrowingFilterIsAddedInChannelOf(eventType))
                        .andThen(aSingleMessageIsSend()))
                .then(expectTheExceptionHandledAsFilterException(TestException.class));
    }

    @Test
    default void testMessageBus_customExceptionHandlerCanAccessExceptionsDuringDelivery(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withACustomExceptionHandler())
                .when(anExceptionThrowingSubscriberIsAdded()
                        .andThen(aSingleMessageIsSend()))
                .then(expectTheExceptionHandledAsDeliverException(TestException.class));
    }

    @Test
    default void testMessageBus_customExceptionHandlerCanMarkExceptionAsNotDeliveryAborting(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withACustomExceptionHandlerMarkingExceptionAsIgnored())
                .when(anExceptionThrowingSubscriberIsAdded()
                        .andThen(aSingleMessageIsSend()
                                .andThen(theNumberOfSuccessfulMessagesIsQueried())))
                .then(expectResultToBe(1)
                        .and(expectNoException()));
    }

    //dynamic exception listener
    @Test
    default void testMessageBus_dynamicExceptionListenerCanBeAdded_forEventType_forExceptionInFilter(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withAnExceptionThrowingFilter()
                .withADynamicExceptionListenerForEventType())
                .when(aSingleMessageIsSend())
                .then(expectTheExceptionHandled(TestException.class));
    }

    @Test
    default void testMessageBus_dynamicExceptionListenerCanBeAdded_forEventType_forExceptionInSubscriber(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withAnExceptionThrowingSubscriber()
                .withADynamicExceptionListenerForEventType())
                .when(aSingleMessageIsSend())
                .then(expectTheExceptionHandled(TestException.class));
    }

    @Test
    default void testMessageBus_dynamicErrorListenerCanBeRemoved_forEventType(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withAnExceptionThrowingSubscriber()
                .withTwoDynamicExceptionListenerForEventType())
                .when(theDynamicExceptionHandlerToBeRemoved()
                        .andThen(aSingleMessageIsSend()))
                .then(expectNumberOfErrorListener(1)
                        .and(expectTheExceptionHandled(TestException.class)));
    }

    @Test
    default void testMessageBus_dynamicExceptionListenerCanBeAdded_forCorrelationId_forExceptionInFilter(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withAnExceptionThrowingFilter()
                .withADynamicCorrelationIdBasedExceptionListener())
                .when(aMessageWithCorrelationIdIsSend())
                .then(expectTheExceptionHandled(TestException.class));
    }

    @Test
    default void testMessageBus_dynamicExceptionListenerCanBeAdded_forCorrelationId_forExceptionInSubscriber(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withAnExceptionThrowingSubscriber()
                .withADynamicCorrelationIdBasedExceptionListener())
                .when(aMessageWithCorrelationIdIsSend())
                .then(expectTheExceptionHandled(TestException.class));
    }

    @Test
    default void testMessageBus_dynamicCorrelationIdBasedErrorListenerCanBeRemoved(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withAnExceptionThrowingSubscriber()
                .withTwoDynamicCorrelationBasedExceptionListener())
                .when(theDynamicExceptionHandlerToBeRemoved()
                        .andThen(aMessageWithCorrelationIdIsSend()))
                .then(expectNumberOfErrorListener(1)
                        .and(expectTheExceptionHandledOnlyBeTheRemaining(TestException.class)));
    }

    @Test
    default void testMessageBus_dynamicExceptionListenerGetsCorrectMessageTheErrorOccurredOn(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withAnExceptionThrowingFilter()
                .withADynamicExceptionListenerForEventType())
                .when(aSingleMessageIsSend())
                .then(expectTheExceptionHandled(TestException.class));
    }

    @Test
    default void testMessageBus_canQueryForAllExceptionListener(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withADynamicExceptionListenerForEventType()
                .withADynamicCorrelationIdBasedExceptionListener())
                .when(allDynamicExceptionListenerAreQueried())
                .then(expectAListOfSize(2));
    }

    //await
    @Test
    default void testMessageBus_awaitWithoutCloseReturnsAlwaysTrue(final MessageBusTestConfig messageBusTestConfig) throws Exception {
        given(aConfiguredMessageBus(messageBusTestConfig)
                .withAnExceptionAcceptingSubscriber())
                .when(theMessageBusShutdownIsExpectedForTimeoutInSeconds(1))
                .then(expectResultToBe(true));
    }
}
