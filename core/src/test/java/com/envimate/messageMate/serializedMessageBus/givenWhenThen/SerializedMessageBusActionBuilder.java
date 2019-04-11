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

package com.envimate.messageMate.serializedMessageBus.givenWhenThen;

import com.envimate.messageMate.identification.CorrelationId;
import com.envimate.messageMate.processingContext.EventType;
import com.envimate.messageMate.qcec.shared.TestAction;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.serializedMessageBus.SerializedMessageBus;
import com.envimate.messageMate.shared.testMessages.ErrorTestMessage;
import com.envimate.messageMate.shared.testMessages.InvalidTestMessage;
import com.envimate.messageMate.shared.testMessages.TestMessageOfInterest;
import com.envimate.messageMate.subscribing.SubscriptionId;
import com.envimate.messageMate.useCases.payloadAndErrorPayload.PayloadAndErrorPayload;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.EXCEPTION;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.RESULT;
import static com.envimate.messageMate.serializedMessageBus.givenWhenThen.SerializedMessageBusTestProperties.*;
import static com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeChannelMessageBusSharedTestProperties.EXPECTED_CORRELATION_ID;
import static com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeChannelMessageBusSharedTestProperties.USED_SUBSCRIPTION_ID;
import static com.envimate.messageMate.shared.testMessages.ErrorTestMessage.errorTestMessage;
import static com.envimate.messageMate.shared.testMessages.TestMessageOfInterest.messageOfInterest;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class SerializedMessageBusActionBuilder {
    private final TestAction<SerializedMessageBus> testAction;

    public static SerializedMessageBusActionBuilder aMapDataIsSend() {
        return new SerializedMessageBusActionBuilder((serializedMessageBus, testEnvironment) -> {
            final Map<String, Object> map = new HashMap<>();
            map.put("someValue", new Object());
            testEnvironment.setPropertyIfNotSet(SEND_DATA, map);
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, DEFAULT_EVENT_TYPE);
            serializedMessageBus.send(eventType, map);
            return null;
        });
    }

    public static SerializedMessageBusActionBuilder aMapDataIsSendForTheGivenCorrelationId() {
        return new SerializedMessageBusActionBuilder((serializedMessageBus, testEnvironment) -> {
            final Map<String, Object> map = new HashMap<>();
            map.put("someValue", new Object());
            testEnvironment.setPropertyIfNotSet(SEND_DATA, map);
            final CorrelationId correlationId = testEnvironment.getPropertyAsType(EXPECTED_CORRELATION_ID, CorrelationId.class);
            serializedMessageBus.send(EVENT_TYPE_WITH_NO_SUBSCRIBERS, map, correlationId);
            return null;
        });
    }

    public static SerializedMessageBusActionBuilder aMapDataWithErrorDataIsSend() {
        return new SerializedMessageBusActionBuilder((serializedMessageBus, testEnvironment) -> {
            final Map<String, Object> data = new HashMap<>();
            data.put("someValue", new Object());
            testEnvironment.setPropertyIfNotSet(SEND_DATA, data);
            final Map<String, Object> errorData = new HashMap<>();
            errorData.put("exception", new Object());
            testEnvironment.setPropertyIfNotSet(SEND_ERROR_DATA, errorData);
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, DEFAULT_EVENT_TYPE);
            serializedMessageBus.send(eventType, data, errorData);
            return null;
        });
    }

    public static SerializedMessageBusActionBuilder aMapDataWithErrorDataIsSendForTheGivenCorrelationId() {
        return new SerializedMessageBusActionBuilder((serializedMessageBus, testEnvironment) -> {
            final Map<String, Object> data = new HashMap<>();
            data.put("someValue", new Object());
            testEnvironment.setPropertyIfNotSet(SEND_DATA, data);
            final Map<String, Object> errorData = new HashMap<>();
            errorData.put("exception", new Object());
            testEnvironment.setPropertyIfNotSet(SEND_ERROR_DATA, errorData);
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, DEFAULT_EVENT_TYPE);
            final CorrelationId correlationId = testEnvironment.getPropertyAsType(EXPECTED_CORRELATION_ID, CorrelationId.class);
            serializedMessageBus.send(eventType, data, errorData, correlationId);
            return null;
        });
    }

    public static SerializedMessageBusActionBuilder anObjectIsSend() {
        return new SerializedMessageBusActionBuilder((serializedMessageBus, testEnvironment) -> {
            final TestMessageOfInterest message = messageOfInterest();
            testEnvironment.setPropertyIfNotSet(SEND_DATA, message);
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, DEFAULT_EVENT_TYPE);
            serializedMessageBus.serializeAndSend(eventType, message);
            return null;
        });
    }

    public static SerializedMessageBusActionBuilder anObjectIsSendForACorrelationId() {
        return new SerializedMessageBusActionBuilder((serializedMessageBus, testEnvironment) -> {
            final TestMessageOfInterest message = messageOfInterest();
            testEnvironment.setPropertyIfNotSet(SEND_DATA, message);
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, DEFAULT_EVENT_TYPE);
            final CorrelationId correlationId = testEnvironment.getPropertyAsType(EXPECTED_CORRELATION_ID, CorrelationId.class);
            serializedMessageBus.serializeAndSend(eventType, message, correlationId);
            return null;
        });
    }

    public static SerializedMessageBusActionBuilder anObjectDataWithErrorDataIsSend() {
        return new SerializedMessageBusActionBuilder((serializedMessageBus, testEnvironment) -> {
            final TestMessageOfInterest message = messageOfInterest();
            testEnvironment.setPropertyIfNotSet(SEND_DATA, message);
            final ErrorTestMessage errorTestMessage = errorTestMessage();
            testEnvironment.setPropertyIfNotSet(SEND_ERROR_DATA, errorTestMessage);
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, DEFAULT_EVENT_TYPE);
            serializedMessageBus.serializeAndSend(eventType, message, errorTestMessage);
            return null;
        });
    }

    public static SerializedMessageBusActionBuilder anObjectDataWithErrorDataIsSendForAGivenCorrelationId() {
        return new SerializedMessageBusActionBuilder((serializedMessageBus, testEnvironment) -> {
            final TestMessageOfInterest message = messageOfInterest();
            testEnvironment.setPropertyIfNotSet(SEND_DATA, message);
            final ErrorTestMessage errorTestMessage = errorTestMessage();
            testEnvironment.setPropertyIfNotSet(SEND_ERROR_DATA, errorTestMessage);
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, DEFAULT_EVENT_TYPE);
            final CorrelationId correlationId = testEnvironment.getPropertyAsType(EXPECTED_CORRELATION_ID, CorrelationId.class);
            serializedMessageBus.serializeAndSend(eventType, message, errorTestMessage, correlationId);
            return null;
        });
    }

    public static SerializedMessageBusActionBuilder aMapIsSendAndTheResultIsWaited() {
        return new SerializedMessageBusActionBuilder((serializedMessageBus, testEnvironment) -> {
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, DEFAULT_EVENT_TYPE);
            final Map<String, Object> data = new HashMap<>();
            data.put("someValue", new Object());
            testEnvironment.setPropertyIfNotSet(SEND_DATA, data);
            try {
                final PayloadAndErrorPayload<Map<String, Object>, Map<String, Object>> result =
                        serializedMessageBus.invokeAndWait(eventType, data);
                testEnvironment.setPropertyIfNotSet(RESULT, result);
            } catch (final InterruptedException | ExecutionException e) {
                testEnvironment.setPropertyIfNotSet(EXCEPTION, e);
            }
            return null;
        });
    }

    public static SerializedMessageBusActionBuilder aMapIsSendAndTheResultIsWaitedWithTimeout() {
        return new SerializedMessageBusActionBuilder((serializedMessageBus, testEnvironment) -> {
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, DEFAULT_EVENT_TYPE);
            final Map<String, Object> data = new HashMap<>();
            data.put("someValue", new Object());
            testEnvironment.setPropertyIfNotSet(SEND_DATA, data);
            try {
                final PayloadAndErrorPayload<Map<String, Object>, Map<String, Object>> result =
                        serializedMessageBus.invokeAndWait(eventType, data, 10, MILLISECONDS);
                testEnvironment.setPropertyIfNotSet(RESULT, result);
            } catch (final InterruptedException | ExecutionException | TimeoutException e) {
                testEnvironment.setPropertyIfNotSet(EXCEPTION, e);
            }
            return null;
        });
    }

    public static SerializedMessageBusActionBuilder anObjectIsSendAndTheResultIsWaited() {
        return new SerializedMessageBusActionBuilder((serializedMessageBus, testEnvironment) -> {
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, DEFAULT_EVENT_TYPE);
            final TestMessageOfInterest message = TestMessageOfInterest.messageOfInterest();
            testEnvironment.setPropertyIfNotSet(SEND_DATA, message);
            try {
                final PayloadAndErrorPayload<TestMessageOfInterest, TestMessageOfInterest> result = serializedMessageBus
                        .invokeAndWaitDeserialized(eventType, message, TestMessageOfInterest.class, TestMessageOfInterest.class);
                testEnvironment.setPropertyIfNotSet(RESULT, result);
            } catch (final InterruptedException | ExecutionException e) {
                testEnvironment.setPropertyIfNotSet(EXCEPTION, e);
            }
            return null;
        });
    }

    public static SerializedMessageBusActionBuilder anObjectIsSendAndTheResultIsWaitedWithTimeout() {
        return new SerializedMessageBusActionBuilder((serializedMessageBus, testEnvironment) -> {
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, DEFAULT_EVENT_TYPE);
            final TestMessageOfInterest message = TestMessageOfInterest.messageOfInterest();
            testEnvironment.setPropertyIfNotSet(SEND_DATA, message);
            try {
                final Class<ErrorTestMessage> eClass = ErrorTestMessage.class;
                final TimeUnit unit = MILLISECONDS;
                final PayloadAndErrorPayload<TestMessageOfInterest, ErrorTestMessage> result = serializedMessageBus
                        .invokeAndWaitDeserialized(eventType, message, TestMessageOfInterest.class, eClass, 10, unit);
                testEnvironment.setPropertyIfNotSet(RESULT, result);
            } catch (final InterruptedException | ExecutionException | TimeoutException e) {
                testEnvironment.setPropertyIfNotSet(EXCEPTION, e);
            }
            return null;
        });
    }

    public static SerializedMessageBusActionBuilder anObjectIsSendAndTheNotSerializedResultIsWaited() {
        return new SerializedMessageBusActionBuilder((serializedMessageBus, testEnvironment) -> {
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, DEFAULT_EVENT_TYPE);
            final TestMessageOfInterest message = TestMessageOfInterest.messageOfInterest();
            testEnvironment.setPropertyIfNotSet(SEND_DATA, message);
            try {
                final PayloadAndErrorPayload<Map<String, Object>, Map<String, Object>> result = serializedMessageBus
                        .invokeAndWaitSerializedOnly(eventType, message);
                testEnvironment.setPropertyIfNotSet(RESULT, result);
            } catch (final InterruptedException | ExecutionException e) {
                testEnvironment.setPropertyIfNotSet(EXCEPTION, e);
            }
            return null;
        });
    }

    public static SerializedMessageBusActionBuilder anObjectIsSendAndTheNotSerializedResultIsWaitedWithTimeOut() {
        return new SerializedMessageBusActionBuilder((serializedMessageBus, testEnvironment) -> {
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, DEFAULT_EVENT_TYPE);
            final TestMessageOfInterest message = TestMessageOfInterest.messageOfInterest();
            testEnvironment.setPropertyIfNotSet(SEND_DATA, message);
            try {
                final PayloadAndErrorPayload<Map<String, Object>, Map<String, Object>> result = serializedMessageBus
                        .invokeAndWaitSerializedOnly(eventType, message, 10, MILLISECONDS);
                testEnvironment.setPropertyIfNotSet(RESULT, result);
            } catch (final InterruptedException | ExecutionException | TimeoutException e) {
                testEnvironment.setPropertyIfNotSet(EXCEPTION, e);
            }
            return null;
        });
    }

    public static SerializedMessageBusActionBuilder anObjectWithoutKnownSerializationIsSend() {
        return sendObjectWithoutKnownSerialization((eventType, serializedMessageBus, object) -> {
            return serializedMessageBus.invokeAndWaitDeserialized(eventType, object, Object.class, Object.class);
        });
    }

    public static SerializedMessageBusActionBuilder anObjectWithoutKnownSerializationIsSendForInvokeAndSerializeOnly() {
        return sendObjectWithoutKnownSerialization((eventType, serializedMessageBus, object) -> {
            return serializedMessageBus.invokeAndWaitSerializedOnly(eventType, object);
        });
    }

    public static SerializedMessageBusActionBuilder anObjectWithoutKnownSerializationIsSendWithTimeout() {
        return sendObjectWithoutKnownSerialization((eventType, serializedMessageBus, object) -> {
            return serializedMessageBus
                    .invokeAndWaitDeserialized(eventType, object, Object.class, Object.class, 10, MILLISECONDS);
        });
    }

    public static SerializedMessageBusActionBuilder aObjectWithoutKnownSerializationIsSendForInvokeAndSerializeOnlyWithTimeout() {
        return sendObjectWithoutKnownSerialization((eventType, serializedMessageBus, object) -> {
            return serializedMessageBus.invokeAndWaitSerializedOnly(eventType, object, 10, MILLISECONDS);
        });
    }

    public static SerializedMessageBusActionBuilder anObjectWithoutKnownReturnValueDeserializationIsSend() {
        return sendTestMessageResultingInSomeError((eventType, serializedMessageBus, object) -> {
            return serializedMessageBus
                    .invokeAndWaitDeserialized(eventType, object, InvalidTestMessage.class, InvalidTestMessage.class);
        });
    }

    public static SerializedMessageBusActionBuilder anObjectWithoutKnownReturnValueDeserializationIsSendWithTimeout() {
        return sendTestMessageResultingInSomeError((eventType, serializedMessageBus, object) -> {
            final Class<InvalidTestMessage> responseClasses = InvalidTestMessage.class;
            return serializedMessageBus
                    .invokeAndWaitDeserialized(eventType, object, responseClasses, responseClasses, 10, MILLISECONDS);
        });
    }

    private static SerializedMessageBusActionBuilder sendObjectWithoutKnownSerialization(final InvokeAndWaitCall<Object> call) {
        return new SerializedMessageBusActionBuilder((serializedMessageBus, testEnvironment) -> {
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, DEFAULT_EVENT_TYPE);
            try {
                final InvalidTestMessage testMessage = InvalidTestMessage.invalidTestMessage();
                call.execute(eventType, serializedMessageBus, testMessage);
                throw new IllegalStateException("This should not be called");
            } catch (final InterruptedException | ExecutionException | TimeoutException e) {
                testEnvironment.setPropertyIfNotSet(EXCEPTION, e);
            }
            return null;
        });
    }

    private static SerializedMessageBusActionBuilder sendTestMessageResultingInSomeError(final InvokeAndWaitCall<Object> call) {
        return new SerializedMessageBusActionBuilder((serializedMessageBus, testEnvironment) -> {
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, DEFAULT_EVENT_TYPE);
            try {
                final TestMessageOfInterest testMessage = TestMessageOfInterest.messageOfInterest();
                call.execute(eventType, serializedMessageBus, testMessage);
                throw new IllegalStateException("This should not be called");
            } catch (final InterruptedException | ExecutionException | TimeoutException e) {
                testEnvironment.setPropertyIfNotSet(EXCEPTION, e);
            }
            return null;
        });
    }

    public static SerializedMessageBusActionBuilder theSubscriberUnsubscribe() {
        return new SerializedMessageBusActionBuilder((serializedMessageBus, testEnvironment) -> {
            final List<SubscriptionId> subscriptionIdList = getUsedSubscriptionId(testEnvironment);
            subscriptionIdList.forEach(serializedMessageBus::unsubscribe);
            return null;
        });
    }

    @SuppressWarnings("unchecked")
    private static List<SubscriptionId> getUsedSubscriptionId(final TestEnvironment testEnvironment) {
        return (List<SubscriptionId>) testEnvironment.getProperty(USED_SUBSCRIPTION_ID);
    }

    public TestAction<SerializedMessageBus> build() {
        return testAction;
    }

    private interface InvokeAndWaitCall<T> {
        T execute(EventType eventType,
                  SerializedMessageBus serializedMessageBus,
                  T object) throws InterruptedException, ExecutionException, TimeoutException;
    }
}
