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

package com.envimate.messageMate.shared.utils;

import com.envimate.messageMate.processingContext.EventType;
import com.envimate.messageMate.processingContext.ProcessingContext;
import com.envimate.messageMate.shared.environment.TestEnvironment;
import com.envimate.messageMate.shared.pipeChannelMessageBus.testActions.RawSubscribeActions;
import com.envimate.messageMate.shared.pipeChannelMessageBus.testActions.SubscribeActions;
import com.envimate.messageMate.shared.subscriber.BlockingTestSubscriber;
import com.envimate.messageMate.shared.subscriber.ExceptionThrowingTestSubscriber;
import com.envimate.messageMate.shared.subscriber.SimpleTestSubscriber;
import com.envimate.messageMate.shared.subscriber.TestSubscriber;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import com.envimate.messageMate.subscribing.Subscriber;
import com.envimate.messageMate.subscribing.SubscriptionId;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.Semaphore;

import static com.envimate.messageMate.shared.environment.TestEnvironmentProperty.EXPECTED_RECEIVERS;
import static com.envimate.messageMate.shared.eventType.TestEventType.testEventType;
import static com.envimate.messageMate.shared.properties.SharedTestProperties.*;
import static com.envimate.messageMate.shared.subscriber.BlockingTestSubscriber.blockingTestSubscriber;
import static com.envimate.messageMate.shared.subscriber.ExceptionThrowingTestSubscriber.exceptionThrowingTestSubscriber;
import static com.envimate.messageMate.shared.subscriber.SimpleTestSubscriber.deliveryPreemptingSubscriber;
import static com.envimate.messageMate.shared.subscriber.SimpleTestSubscriber.testSubscriber;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class SubscriptionTestUtils {

    public static void addASingleSubscriber(final SubscribeActions subscribeActions, final TestEnvironment testEnvironment) {

        final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
        addASingleSubscriber(subscribeActions, testEnvironment, eventType);
    }

    public static void addASingleSubscriber(final SubscribeActions subscribeActions,
                                            final TestEnvironment testEnvironment,
                                            final EventType eventType) {
        final SimpleTestSubscriber<TestMessage> subscriber = testSubscriber();
        subscribeActions.subscribe(eventType, subscriber);
        testEnvironment.addToListProperty(EXPECTED_RECEIVERS, subscriber);
        testEnvironment.addToListProperty(INITIAL_SUBSCRIBER, subscriber);
    }

    public static void addASingleSubscriber(final SubscribeActions subscribeActions,
                                            final TestEnvironment testEnvironment,
                                            final Subscriber<TestMessage> subscriber,
                                            final EventType eventType) {
        subscribeActions.subscribe(eventType, subscriber);
        testEnvironment.addToListProperty(EXPECTED_RECEIVERS, subscriber);
        testEnvironment.addToListProperty(INITIAL_SUBSCRIBER, subscriber);
    }

    public static void addSeveralSubscriber(final SubscribeActions subscribeActions,
                                            final TestEnvironment testEnvironment,
                                            final int numberOfReceivers) {
        for (int i = 0; i < numberOfReceivers; i++) {
            addASingleSubscriber(subscribeActions, testEnvironment);
        }
    }

    public static void addASubscriberThatBlocksWhenAccepting(final SubscribeActions subscribeActions,
                                                             final TestEnvironment testEnvironment) {
        final Semaphore semaphore = new Semaphore(0);
        final BlockingTestSubscriber<TestMessage> subscriber = blockingTestSubscriber(semaphore);
        final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
        addASingleSubscriber(subscribeActions, testEnvironment, subscriber, eventType);
        testEnvironment.addToListProperty(EXPECTED_RECEIVERS, subscriber);
        testEnvironment.setPropertyIfNotSet(EXECUTION_END_SEMAPHORE, semaphore);
    }

    public static void addAnExceptionAcceptingSubscriber(final SubscribeActions subscribeActions, final TestEnvironment testEnvironment) {
        final SimpleTestSubscriber<TestMessage> errorSubscriber = testSubscriber();
        final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
        addASingleSubscriber(subscribeActions, testEnvironment, errorSubscriber, eventType);
        testEnvironment.setPropertyIfNotSet(ERROR_SUBSCRIBER, errorSubscriber);
    }

    public static TestSubscriber<TestMessage> addAnExceptionThrowingSubscriber(final SubscribeActions subscribeActions,
                                                                               final TestEnvironment testEnvironment) {

        final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
        return addAnExceptionThrowingSubscriber(subscribeActions, testEnvironment, eventType);
    }

    public static TestSubscriber<TestMessage> addAnExceptionThrowingSubscriber(final SubscribeActions subscribeActions,
                                                                               final TestEnvironment testEnvironment,
                                                                               final EventType eventType) {
        final ExceptionThrowingTestSubscriber<TestMessage> subscriber = exceptionThrowingTestSubscriber();
        addASingleSubscriber(subscribeActions, testEnvironment, subscriber, eventType);
        return subscriber;
    }

    public static void addSeveralDeliveryInterruptingSubscriber(final SubscribeActions subscribeActions,
                                                                final TestEnvironment testEnvironment,
                                                                final int numberOfReceivers) {
        final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
        for (int i = 0; i < numberOfReceivers; i++) {
            final SimpleTestSubscriber<TestMessage> subscriber = deliveryPreemptingSubscriber();
            addASingleSubscriber(subscribeActions, testEnvironment, subscriber, eventType);
            testEnvironment.addToListProperty(POTENTIAL_RECEIVERS, subscriber);
        }
    }

    public static void addASingleRawSubscriber(final RawSubscribeActions rawSubscribeActions,
                                               final TestEnvironment testEnvironment) {
        final EventType eventType = testEventType();
        testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, eventType);
        addASingleRawSubscriber(rawSubscribeActions, testEnvironment, eventType);
    }

    public static void addASingleRawSubscriber(final RawSubscribeActions rawSubscribeActions,
                                               final TestEnvironment testEnvironment,
                                               final EventType eventType) {
        final SimpleTestSubscriber<ProcessingContext<TestMessage>> subscriber = testSubscriber();
        addASingleRawSubscriber(rawSubscribeActions, testEnvironment, eventType, subscriber);
    }

    public static void addASingleRawSubscriber(final RawSubscribeActions rawSubscribeActions,
                                               final TestEnvironment testEnvironment,
                                               final EventType eventType,
                                               final SimpleTestSubscriber<ProcessingContext<TestMessage>> subscriber) {
        rawSubscribeActions.subscribeRaw(eventType, subscriber);
        testEnvironment.addToListProperty(EXPECTED_RECEIVERS, subscriber);
        testEnvironment.addToListProperty(INITIAL_SUBSCRIBER, subscriber);
    }

    public static void addSeveralRawSubscriber(final RawSubscribeActions subscribeActions,
                                               final TestEnvironment testEnvironment,
                                               final int numberOfReceivers) {
        for (int i = 0; i < numberOfReceivers; i++) {
            addASingleRawSubscriber(subscribeActions, testEnvironment);
        }
    }

    public static void unsubscribe(final SubscribeActions subscribeActions,
                                   final Subscriber<?> subscriber) {
        final SubscriptionId subscriptionId = subscriber.getSubscriptionId();
        subscribeActions.unsubscribe(subscriptionId);
    }

    public static void unsubscribe(final SubscribeActions subscribeActions,
                                   final SubscriptionId subscriptionId) {
        subscribeActions.unsubscribe(subscriptionId);
    }

    public static void unsubscribeASubscriberXTimes(final SubscribeActions subscribeActions,
                                                    final TestEnvironment testEnvironment,
                                                    final int numberOfUnsubscriptions) {
        final List<Subscriber<?>> currentSubscriber = getSubscriberList(testEnvironment);
        final Subscriber<?> firstSubscriber = currentSubscriber.get(0);
        final SubscriptionId subscriptionId = firstSubscriber.getSubscriptionId();
        for (int i = 0; i < numberOfUnsubscriptions; i++) {
            unsubscribe(subscribeActions, subscriptionId);
        }
        final List<Subscriber<?>> remainingSubscriber = currentSubscriber.subList(1, currentSubscriber.size());
        testEnvironment.setProperty(EXPECTED_SUBSCRIBER, remainingSubscriber);
    }

    @SuppressWarnings("unchecked")
    private static List<Subscriber<?>> getSubscriberList(final TestEnvironment testEnvironment) {
        final List<Subscriber<?>> currentSubscriber;
        if (testEnvironment.has(EXPECTED_SUBSCRIBER)) {
            currentSubscriber = (List<Subscriber<?>>) testEnvironment.getProperty(EXPECTED_SUBSCRIBER);
        } else {
            currentSubscriber = (List<Subscriber<?>>) testEnvironment.getProperty(INITIAL_SUBSCRIBER);
        }
        return currentSubscriber;
    }
}
