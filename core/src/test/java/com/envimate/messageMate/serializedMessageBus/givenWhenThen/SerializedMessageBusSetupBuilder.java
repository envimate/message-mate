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

package com.envimate.messageMate.serializedMessageBus.givenWhenThen;

import com.envimate.messageMate.identification.CorrelationId;
import com.envimate.messageMate.internal.collections.filtermap.FilterMapBuilder;
import com.envimate.messageMate.internal.collections.predicatemap.PredicateMapBuilder;
import com.envimate.messageMate.mapping.Demapifier;
import com.envimate.messageMate.mapping.Deserializer;
import com.envimate.messageMate.mapping.Mapifier;
import com.envimate.messageMate.mapping.Serializer;
import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.processingContext.EventType;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.serializedMessageBus.SerializedMessageBus;
import com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.SetupAction;
import com.envimate.messageMate.shared.subscriber.SimpleTestSubscriber;
import com.envimate.messageMate.shared.subscriber.TestException;
import com.envimate.messageMate.shared.testMessages.ErrorTestMessage;
import com.envimate.messageMate.shared.testMessages.TestMessageOfInterest;
import com.envimate.messageMate.subscribing.SubscriptionId;
import com.envimate.messageMate.useCases.payloadAndErrorPayload.PayloadAndErrorPayload;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.envimate.messageMate.internal.collections.predicatemap.PredicateMapBuilder.predicateMapBuilder;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.EXPECTED_RECEIVERS;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.*;
import static com.envimate.messageMate.serializedMessageBus.givenWhenThen.SerializedMessageBusTestProperties.*;
import static com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeChannelMessageBusSharedTestProperties.*;
import static com.envimate.messageMate.shared.subscriber.SimpleTestSubscriber.testSubscriber;
import static com.envimate.messageMate.subscribing.ConsumerSubscriber.consumerSubscriber;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class SerializedMessageBusSetupBuilder {
    public final static String PAYLOAD_SERIALIZATION_KEY = "content";
    public final static String ERROR_PAYLOAD_SERIALIZATION_KEY = "error";
    private final TestEnvironment testEnvironment = TestEnvironment.emptyTestEnvironment();
    private final List<SetupAction<SerializedMessageBus>> setupActions = new LinkedList<>();
    private final SerializedMessageBusTestConfig testConfig;

    public static SerializedMessageBusSetupBuilder aSerializedMessageBus(final SerializedMessageBusTestConfig testConfig) {
        return new SerializedMessageBusSetupBuilder(testConfig);
    }

    public SerializedMessageBusSetupBuilder withAMapSubscriber() {
        setupActions.add(this::addMapSubscriber);
        return this;
    }

    private void addMapSubscriber(final SerializedMessageBus serializedMessageBus, final TestEnvironment testEnvironment) {
        final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, DEFAULT_EVENT_TYPE);
        final SimpleTestSubscriber<PayloadAndErrorPayload<Map<String, Object>, Map<String, Object>>> subscriber =
                testSubscriber();
        testEnvironment.addToListProperty(EXPECTED_RECEIVERS, subscriber);
        testEnvironment.setProperty(SINGLE_RECEIVER, subscriber);
        final SubscriptionId subscriptionId = serializedMessageBus.subscribe(eventType, subscriber);
        testEnvironment.addToListProperty(USED_SUBSCRIPTION_ID, subscriptionId);
    }

    public SerializedMessageBusSetupBuilder withAMapSubscriberForACorrelationId() {
        setupActions.add((serializedMessageBus, testEnvironment) -> {
            final CorrelationId newUniqueCorrelationId = CorrelationId.newUniqueCorrelationId();
            final CorrelationId correlationId = getOrSetCorrelationId(testEnvironment, newUniqueCorrelationId);
            final SimpleTestSubscriber<PayloadAndErrorPayload<Map<String, Object>, Map<String, Object>>> subscriber =
                    testSubscriber();
            testEnvironment.addToListProperty(EXPECTED_RECEIVERS, subscriber);
            testEnvironment.setProperty(SINGLE_RECEIVER, subscriber);
            final SubscriptionId subscriptionId = serializedMessageBus.subscribe(correlationId, subscriber);
            testEnvironment.addToListProperty(USED_SUBSCRIPTION_ID, subscriptionId);
        });
        return this;
    }

    public SerializedMessageBusSetupBuilder withADeserializedSubscriber() {
        setupActions.add((serializedMessageBus, testEnvironment) -> {
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, DEFAULT_EVENT_TYPE);
            final SimpleTestSubscriber<PayloadAndErrorPayload<TestMessageOfInterest, ErrorTestMessage>> subscriber =
                    testSubscriber();
            testEnvironment.addToListProperty(EXPECTED_RECEIVERS, subscriber);
            testEnvironment.setProperty(SINGLE_RECEIVER, subscriber);
            final SubscriptionId subscriptionId = serializedMessageBus
                    .subscribeDeserialized(eventType, subscriber, TestMessageOfInterest.class, ErrorTestMessage.class);
            testEnvironment.addToListProperty(USED_SUBSCRIPTION_ID, subscriptionId);
        });
        return this;
    }

    public SerializedMessageBusSetupBuilder withADeserializedSubscriberForACorrelationId() {
        setupActions.add((serializedMessageBus, testEnvironment) -> {
            final CorrelationId newUniqueCorrelationId = CorrelationId.newUniqueCorrelationId();
            final CorrelationId correlationId = getOrSetCorrelationId(testEnvironment, newUniqueCorrelationId);
            final SimpleTestSubscriber<PayloadAndErrorPayload<TestMessageOfInterest, ErrorTestMessage>> subscriber =
                    testSubscriber();
            testEnvironment.addToListProperty(EXPECTED_RECEIVERS, subscriber);
            testEnvironment.setProperty(SINGLE_RECEIVER, subscriber);
            final SubscriptionId subscriptionId = serializedMessageBus
                    .subscribeDeserialized(correlationId, subscriber, TestMessageOfInterest.class, ErrorTestMessage.class);
            testEnvironment.addToListProperty(USED_SUBSCRIPTION_ID, subscriptionId);
        });
        return this;
    }

    public SerializedMessageBusSetupBuilder withASubscriberSendingCorrelatedResponse() {
        setupActions.add((serializedMessageBus, testEnvironment) -> {
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, DEFAULT_EVENT_TYPE);
            final SubscriptionId subscriptionId = serializedMessageBus.subscribeRaw(eventType,
                    consumerSubscriber(processingContext -> {
                        final CorrelationId correlationId = processingContext.generateCorrelationIdForAnswer();
                        final Map<String, Object> payload = processingContext.getPayload();
                        serializedMessageBus.send(EVENT_TYPE_WITH_NO_SUBSCRIBERS, payload, correlationId);
                    }));
            testEnvironment.addToListProperty(USED_SUBSCRIPTION_ID, subscriptionId);
        });
        return this;
    }

    public SerializedMessageBusSetupBuilder withASubscriberSendingDataBackAsErrorResponse() {
        setupActions.add((serializedMessageBus, testEnvironment) -> {
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, DEFAULT_EVENT_TYPE);
            final SubscriptionId subscriptionId = serializedMessageBus.subscribeRaw(eventType,
                    consumerSubscriber(processingContext -> {
                        final CorrelationId correlationId = processingContext.generateCorrelationIdForAnswer();
                        final Map<String, Object> payload = processingContext.getPayload();
                        serializedMessageBus.send(EVENT_TYPE_WITH_NO_SUBSCRIBERS, null, payload, correlationId);
                    }));
            testEnvironment.addToListProperty(USED_SUBSCRIPTION_ID, subscriptionId);
        });
        return this;
    }

    public SerializedMessageBusSetupBuilder withASubscriberThrowingError() {
        setupActions.add((serializedMessageBus, testEnvironment) -> {
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, DEFAULT_EVENT_TYPE);
            final SubscriptionId subscriptionId = serializedMessageBus.subscribe(eventType, consumerSubscriber(ignored -> {
                throw new TestException();
            }));
            testEnvironment.addToListProperty(USED_SUBSCRIPTION_ID, subscriptionId);
        });
        return this;
    }

    public SerializedMessageBusSetup build() {
        final MessageBus messageBus = testConfig.getMessageBus();
        final Deserializer deserializer = getDeserializer();
        final Serializer serializer = getSerializer();
        final SerializedMessageBus serializedMessageBus = SerializedMessageBus
                .aSerializedMessageBus(messageBus, deserializer, serializer);
        setupActions.forEach(a -> a.execute(serializedMessageBus, testEnvironment));
        testEnvironment.setPropertyIfNotSet(SUT, serializedMessageBus);
        testEnvironment.setPropertyIfNotSet(MOCK, messageBus);
        return new SerializedMessageBusSetup(serializedMessageBus, testEnvironment);
    }

    private CorrelationId getOrSetCorrelationId(final TestEnvironment testEnvironment,
                                                final CorrelationId newUniqueCorrelationId) {
        return testEnvironment.getPropertyOrSetDefault(EXPECTED_CORRELATION_ID, newUniqueCorrelationId);
    }

    private Deserializer getDeserializer() {
        final FilterMapBuilder<Class<?>, Map<String, Object>, Demapifier<?>> deserializingFilterMapBuilder = FilterMapBuilder.filterMapBuilder();
        deserializingFilterMapBuilder
                .put((o, o2) -> o.equals(TestMessageOfInterest.class), (Demapifier) (targetType, map) -> {
                    final String content = (String) map.get(PAYLOAD_SERIALIZATION_KEY);
                    return TestMessageOfInterest.messageOfInterest(content);
                })
                .put((o, o2) -> o.equals(ErrorTestMessage.class), (Demapifier) (targetType, map) -> {
                    final String content = (String) map.get(ERROR_PAYLOAD_SERIALIZATION_KEY);
                    return ErrorTestMessage.errorTestMessage(content);
                })
                .setDefaultValue((targetType, map) -> {
                    throw new TestMissingDeserializationException("No deserialization known for " + targetType);
                });

        return Deserializer.requestDeserializer(deserializingFilterMapBuilder.build());
    }

    private Serializer getSerializer() {
        final PredicateMapBuilder<Object, Mapifier<Object>> serializingMapBuilder = predicateMapBuilder();
        serializingMapBuilder
                .put((o) -> o.getClass().equals(TestMessageOfInterest.class), object -> {
                    final TestMessageOfInterest message = (TestMessageOfInterest) object;
                    final HashMap<String, Object> map = new HashMap<>();
                    map.put(PAYLOAD_SERIALIZATION_KEY, message.content);
                    return map;
                })
                .put((o) -> o.getClass().equals(ErrorTestMessage.class), object -> {
                    final ErrorTestMessage message = (ErrorTestMessage) object;
                    final HashMap<String, Object> map = new HashMap<>();
                    map.put(ERROR_PAYLOAD_SERIALIZATION_KEY, message.getContent());
                    return map;
                })
                .setDefaultValue((o) -> {
                    throw new TestMissingSerializationException("No serialization known for " + o.getClass());
                });
        return Serializer.responseSerializer(serializingMapBuilder.build());
    }

    @RequiredArgsConstructor(access = PRIVATE)
    final class SerializedMessageBusSetup {
        @Getter
        private final SerializedMessageBus serializedMessageBus;
        @Getter
        private final TestEnvironment testEnvironment;

    }
}
