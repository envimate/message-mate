/*
 * Copyright (c) 2019 envimate GmbH - https://envimate.com/.
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

package com.envimate.messageMate.messageBus.givenWhenThen;

import com.envimate.messageMate.identification.CorrelationId;
import com.envimate.messageMate.identification.MessageId;
import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.messageBus.exception.MessageBusExceptionListener;
import com.envimate.messageMate.processingContext.EventType;
import com.envimate.messageMate.processingContext.ProcessingContext;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.shared.subscriber.BlockingTestSubscriber;
import com.envimate.messageMate.shared.subscriber.ExceptionThrowingTestSubscriber;
import com.envimate.messageMate.shared.subscriber.SimpleTestSubscriber;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import com.envimate.messageMate.shared.testMessages.TestMessageOfInterest;
import com.envimate.messageMate.subscribing.Subscriber;
import com.envimate.messageMate.subscribing.SubscriptionId;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.envimate.messageMate.identification.CorrelationId.newUniqueCorrelationId;
import static com.envimate.messageMate.messageBus.givenWhenThen.MessageBusTestProperties.EVENT_TYPE;
import static com.envimate.messageMate.messageBus.givenWhenThen.MessageBusTestProperties.MESSAGE_RECEIVED_BY_ERROR_LISTENER;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.EXPECTED_RECEIVERS;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.*;
import static com.envimate.messageMate.shared.eventType.TestEventType.testEventType;
import static com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.AsynchronousSendingTestUtils.*;
import static com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeChannelMessageBusSharedTestProperties.*;
import static com.envimate.messageMate.shared.subscriber.BlockingTestSubscriber.blockingTestSubscriber;
import static com.envimate.messageMate.shared.subscriber.ExceptionThrowingTestSubscriber.exceptionThrowingTestSubscriber;
import static com.envimate.messageMate.shared.subscriber.SimpleTestSubscriber.testSubscriber;
import static com.envimate.messageMate.shared.testMessages.TestMessageOfInterest.messageOfInterest;
import static com.envimate.messageMate.shared.testMessages.TestMessageOfInterest.messageWithErrorContent;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
final class MessageBusTestActions {

    static void sendASingleMessage(final MessageBus messageBus, final TestEnvironment testEnvironment) {
        final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
        sendASingleMessage(messageBus, testEnvironment, eventType);
    }

    private static void sendASingleMessage(final MessageBus messageBus,
                                           final TestEnvironment testEnvironment,
                                           final EventType eventType) {
        final TestMessageOfInterest message = messageOfInterest();
        testEnvironment.setProperty(SINGLE_SEND_MESSAGE, message);
        final MessageId messageId = messageBus.send(eventType, message);
        testEnvironment.setProperty(SEND_MESSAGE_ID, messageId);
    }

    static void sendTheMessage(final MessageBus messageBus, final TestEnvironment testEnvironment, final Object message) {
        final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
        testEnvironment.setProperty(SINGLE_SEND_MESSAGE, message);
        final MessageId messageId = messageBus.send(eventType, message);
        testEnvironment.setProperty(SEND_MESSAGE_ID, messageId);
    }

    static void sendAMessageWithCorrelationId(final MessageBus messageBus, final TestEnvironment testEnvironment) {
        final CorrelationId corId = newUniqueCorrelationId();
        final CorrelationId correlationId = testEnvironment.getPropertyOrSetDefault(EXPECTED_CORRELATION_ID, corId);
        testEnvironment.setProperty(EXPECTED_RESULT, correlationId);

        final TestMessageOfInterest message = messageOfInterest();
        testEnvironment.setProperty(SINGLE_SEND_MESSAGE, message);

        final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
        final MessageId messageId = messageBus.send(eventType, message, correlationId);
        testEnvironment.setProperty(SEND_MESSAGE_ID, messageId);
    }

    static void sendSeveralMessages(final MessageBus messageBus,
                                    final TestEnvironment testEnvironment,
                                    final int numberOfMessages) {
        final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
        final List<TestMessageOfInterest> messages = new LinkedList<>();
        for (int i = 0; i < numberOfMessages; i++) {
            final TestMessageOfInterest message = TestMessageOfInterest.messageOfInterest();
            messageBus.send(eventType, message);
            messages.add(message);
        }
        testEnvironment.setProperty(MESSAGES_SEND_OF_INTEREST, messages);
    }

    static void sendMessagesAsynchronously(final MessageBus messageBus,
                                           final TestEnvironment testEnvironment,
                                           final int numberOfSenders,
                                           final int numberOfMessages,
                                           final boolean expectCleanShutdown) {
        final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
        final Consumer<TestMessage> sendConsumer = testMessage -> messageBus.send(eventType, testMessage);
        sendValidMessagesAsynchronously(sendConsumer, testEnvironment, numberOfSenders, numberOfMessages, expectCleanShutdown);
    }

    static void sendOnlyInvalidMessagesAsynchronously(final MessageBus messageBus,
                                                      final TestEnvironment testEnvironment,
                                                      final int numberOfSenders,
                                                      final int numberOfMessages) {
        final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
        final Consumer<TestMessage> sendConsumer = testMessage -> messageBus.send(eventType, testMessage);
        sendInvalidMessagesAsynchronously(sendConsumer, testEnvironment, numberOfSenders, numberOfMessages);
    }

    static void sendInvalidAndInvalidMessagesAsynchronously(final MessageBus messageBus,
                                                            final TestEnvironment testEnvironment,
                                                            final int numberOfSenders,
                                                            final int numberOfMessages) {
        final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
        final Consumer<TestMessage> sendConsumer = testMessage -> messageBus.send(eventType, testMessage);
        sendMixtureOfValidAndInvalidMessagesAsynchronously(sendConsumer, testEnvironment, numberOfSenders, numberOfMessages);
    }

    static void sendTheMessageAsProcessingContext(final MessageBus messageBus,
                                                  final TestEnvironment testEnvironment,
                                                  final Object message) {
        final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
        sendTheMessageAsProcessingContext(messageBus, testEnvironment, message, eventType);
    }

    static void sendTheMessageAsProcessingContext(final MessageBus messageBus,
                                                  final TestEnvironment testEnvironment,
                                                  final Object message,
                                                  final EventType eventType) {
        final ProcessingContext<Object> processingContext = ProcessingContext.processingContext(eventType, message);
        testEnvironment.setProperty(SINGLE_SEND_MESSAGE, processingContext);
        final MessageId messageId = messageBus.send(processingContext);
        testEnvironment.setProperty(SEND_MESSAGE_ID, messageId);
    }

    static void sendMessageWithErrorPayloadIsSend(final MessageBus messageBus, final TestEnvironment testEnvironment) {
        final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
        final TestMessageOfInterest errorPayload = messageWithErrorContent();
        testEnvironment.setProperty(SINGLE_SEND_MESSAGE, errorPayload);
        final ProcessingContext<Object> processingContext = ProcessingContext.processingContextForError(eventType, errorPayload);
        final MessageId messageId = messageBus.send(processingContext);
        testEnvironment.setProperty(SEND_MESSAGE_ID, messageId);
    }

    static void addASingleSubscriber(final MessageBus messageBus, final TestEnvironment testEnvironment) {
        final EventType eventType = testEventType();
        testEnvironment.setPropertyIfNotSet(EVENT_TYPE, eventType);
        addASingleSubscriber(messageBus, testEnvironment, eventType);
    }

    static void addASingleSubscriber(final MessageBus messageBus,
                                     final TestEnvironment testEnvironment,
                                     final EventType eventType) {
        final SimpleTestSubscriber<Object> subscriber = testSubscriber();
        addASingleSubscriber(messageBus, testEnvironment, eventType, subscriber);
    }

    static void addASingleSubscriber(final MessageBus messageBus,
                                     final TestEnvironment testEnvironment,
                                     final EventType eventType,
                                     final Subscriber<Object> subscriber) {
        messageBus.subscribe(eventType, subscriber);
        testEnvironment.addToListProperty(EXPECTED_RECEIVERS, subscriber);
        testEnvironment.addToListProperty(INITIAL_SUBSCRIBER, subscriber);
    }

    static void withSeveralSubscriber(final MessageBus messageBus,
                                      final TestEnvironment testEnvironment,
                                      final int numberOfSubscribers) {
        final EventType eventType = testEventType();
        for (int i = 0; i < numberOfSubscribers; i++) {
            addASingleSubscriber(messageBus, testEnvironment, eventType);
        }
        testEnvironment.setPropertyIfNotSet(EVENT_TYPE, eventType);
    }

    static void addASingleRawSubscriber(final MessageBus messageBus,
                                        final TestEnvironment testEnvironment) {
        final EventType eventType = testEventType();
        testEnvironment.setPropertyIfNotSet(EVENT_TYPE, eventType);
        addASingleRawSubscriber(messageBus, testEnvironment, eventType);
    }

    static void addASingleRawSubscriber(final MessageBus messageBus,
                                        final TestEnvironment testEnvironment,
                                        final EventType eventType) {
        final SimpleTestSubscriber<ProcessingContext<Object>> subscriber = testSubscriber();
        messageBus.subscribeRaw(eventType, subscriber);
        testEnvironment.addToListProperty(EXPECTED_RECEIVERS, subscriber);
        testEnvironment.addToListProperty(INITIAL_SUBSCRIBER, subscriber);
    }

    static void addASubscriberThatBlocksWhenAccepting(final MessageBus messageBus, final TestEnvironment testEnvironment) {
        final EventType eventType = testEventType();
        testEnvironment.setPropertyIfNotSet(EVENT_TYPE, eventType);
        final Semaphore semaphore = new Semaphore(0);
        final BlockingTestSubscriber<Object> subscriber = blockingTestSubscriber(semaphore);
        addASingleSubscriber(messageBus, testEnvironment, eventType, subscriber);
        testEnvironment.setProperty(EXECUTION_END_SEMAPHORE, semaphore);
    }

    static void addAErrorThrowingSubscriber(final MessageBus messageBus, final TestEnvironment testEnvironment) {
        final EventType eventType = testEventType();
        testEnvironment.setPropertyIfNotSet(EVENT_TYPE, eventType);
        final ExceptionThrowingTestSubscriber<Object> subscriber = exceptionThrowingTestSubscriber();
        messageBus.subscribe(eventType, subscriber);
    }

    static void sendMessagesBeforeAShutdownAsynchronously(final MessageBus messageBus, final TestEnvironment testEnvironment,
                                                          final int numberOfSenders, final int numberOfMessages) {
        final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
        final BiConsumer<EventType, Subscriber<Object>> subscriberConsumer = (e, subscriber) -> {
            messageBus.subscribe(eventType, subscriber);
        };
        final BiConsumer<EventType, TestMessage> sendConsumer = (e, testMessage) -> messageBus.send(eventType, testMessage);
        final Consumer<Boolean> closeConsumer = finishRemainingTasks -> messageBus.close(false);
        sendMessagesBeforeShutdownAsynchronously(subscriberConsumer, sendConsumer, closeConsumer, testEnvironment,
                numberOfSenders, numberOfMessages);
    }

    static void sendMessagesBeforeAndAfterTheShutdownAsynchronously(final MessageBus messageBus,
                                                                    final TestEnvironment testEnvironment,
                                                                    final int numberOfSender,
                                                                    final boolean finishRemainingTasks) {
        final BiConsumer<EventType, Subscriber<Object>> subscriberConsumer = messageBus::subscribe;
        final BiConsumer<EventType, Object> sendConsumer = messageBus::send;
        final Consumer<Boolean> closeConsumer = messageBus::close;
        sendMessagesBeforeAndAfterShutdownAsynchronously(subscriberConsumer, sendConsumer, closeConsumer,
                testEnvironment, numberOfSender, 0, finishRemainingTasks);
    }

    static void addDynamicErrorListenerForEventType(final MessageBus messageBus, final TestEnvironment testEnvironment) {
        final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
        final SubscriptionId subscriptionId = messageBus.onException(eventType, (m, e) -> {
            testEnvironment.setPropertyIfNotSet(RESULT, e);
            testEnvironment.setPropertyIfNotSet(MESSAGE_RECEIVED_BY_ERROR_LISTENER, m);
        });
        testEnvironment.setProperty(USED_SUBSCRIPTION_ID, subscriptionId);
    }

    static void addTwoDynamicErrorListenerForEventType_whereTheFirstWillBeRemoved(final MessageBus messageBus,
                                                                                  final TestEnvironment testEnvironment) {
        final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
        final SubscriptionId subscriptionId = messageBus.onException(eventType, (m, e) -> {
            throw new RuntimeException("Should not be called");
        });
        testEnvironment.setProperty(USED_SUBSCRIPTION_ID, subscriptionId);
        messageBus.onException(eventType, (m, e) -> {
            testEnvironment.setPropertyIfNotSet(RESULT, e);
            testEnvironment.setPropertyIfNotSet(MESSAGE_RECEIVED_BY_ERROR_LISTENER, m);
        });
    }

    static List<MessageBusExceptionListener> queryListOfDynamicExceptionListener(final MessageBus messageBus) {
        return messageBus.getStatusInformation().getAllExceptionListener();
    }
}
