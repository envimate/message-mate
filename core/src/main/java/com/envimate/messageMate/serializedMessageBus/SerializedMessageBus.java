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

package com.envimate.messageMate.serializedMessageBus;

import com.envimate.messageMate.identification.CorrelationId;
import com.envimate.messageMate.identification.MessageId;
import com.envimate.messageMate.mapping.Deserializer;
import com.envimate.messageMate.mapping.Serializer;
import com.envimate.messageMate.messageBus.EventType;
import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.messageBus.PayloadAndErrorPayload;
import com.envimate.messageMate.processingContext.ProcessingContext;
import com.envimate.messageMate.subscribing.Subscriber;
import com.envimate.messageMate.subscribing.SubscriptionId;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public interface SerializedMessageBus {

    static SerializedMessageBus aSerializedMessageBus(final MessageBus messageBus,
                                                      final Deserializer deserializer,
                                                      final Serializer serializer) {
        return new SerializedMessageBusImpl(messageBus, deserializer, serializer);
    }

    MessageId send(EventType eventType, Map<String, Object> data);

    MessageId send(EventType eventType, Map<String, Object> data, CorrelationId correlationId);

    MessageId send(EventType eventType, Map<String, Object> data, Map<String, Object> errorData);

    MessageId send(EventType eventType, Map<String, Object> data, Map<String, Object> errorData, CorrelationId correlationId);

    MessageId serializeAndSend(EventType eventType, Object data);

    MessageId serializeAndSend(EventType eventType, Object data, CorrelationId correlationId);

    MessageId serializeAndSend(EventType eventType, Object data, Object errorData);

    MessageId serializeAndSend(EventType eventType, Object data, Object errorData, CorrelationId correlationId);

    PayloadAndErrorPayload<Map<String, Object>, Map<String, Object>> invokeAndWait(
            EventType eventType,
            Map<String, Object> data) throws InterruptedException, ExecutionException;

    PayloadAndErrorPayload<Map<String, Object>, Map<String, Object>> invokeAndWait(
            EventType eventType,
            Map<String, Object> data,
            long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException;

    PayloadAndErrorPayload<Map<String, Object>, Map<String, Object>> invokeAndWaitSerializedOnly(
            EventType eventType,
            Object data) throws InterruptedException, ExecutionException;

    PayloadAndErrorPayload<Map<String, Object>, Map<String, Object>> invokeAndWaitSerializedOnly(
            EventType eventType,
            Object data,
            long timeout,
            TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException;

    <P, E> PayloadAndErrorPayload<P, E> invokeAndWaitDeserialized(
            EventType eventType,
            Object data,
            Class<P> responseClass,
            Class<E> errorPayloadClass) throws InterruptedException, ExecutionException;

    <P, E> PayloadAndErrorPayload<P, E> invokeAndWaitDeserialized(
            EventType eventType,
            Object data,
            Class<P> responseClass,
            Class<E> errorPayloadClass,
            long timeout,
            TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException;

    SubscriptionId subscribe(EventType eventType,
                             Subscriber<PayloadAndErrorPayload<Map<String, Object>, Map<String, Object>>> subscriber);

    SubscriptionId subscribe(CorrelationId correlationId,
                             Subscriber<PayloadAndErrorPayload<Map<String, Object>, Map<String, Object>>> subscriber);

    <P, E> SubscriptionId subscribeDeserialized(EventType eventType,
                                                Subscriber<PayloadAndErrorPayload<P, E>> subscriber,
                                                Class<P> responseClass,
                                                Class<E> errorClass);

    <P, E> SubscriptionId subscribeDeserialized(CorrelationId correlationId,
                                                Subscriber<PayloadAndErrorPayload<P, E>> subscriber,
                                                Class<P> responseClass,
                                                Class<E> errorClass);

    SubscriptionId subscribeRaw(EventType eventType, Subscriber<ProcessingContext<Map<String, Object>>> subscriber);

    void unsubscribe(SubscriptionId subscriptionId);

}
