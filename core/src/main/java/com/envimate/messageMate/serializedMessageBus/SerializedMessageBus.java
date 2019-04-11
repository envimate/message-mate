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

package com.envimate.messageMate.serializedMessageBus;

import com.envimate.messageMate.identification.CorrelationId;
import com.envimate.messageMate.identification.MessageId;
import com.envimate.messageMate.mapping.Deserializer;
import com.envimate.messageMate.mapping.Serializer;
import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.messageFunction.MessageFunction;
import com.envimate.messageMate.processingContext.EventType;
import com.envimate.messageMate.processingContext.ProcessingContext;
import com.envimate.messageMate.subscribing.Subscriber;
import com.envimate.messageMate.subscribing.SubscriptionId;
import com.envimate.messageMate.useCases.payloadAndErrorPayload.PayloadAndErrorPayload;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A {@code SerializedMessageBus} enhances a typical {@link MessageBus} with serialization and deserialization functionality.
 * It provides methods to send, receive and subscribe to both serialized and not serialized data. It also incorporates the
 * functionality of a {@link MessageFunction} by providing functions, that wait on a matching response.
 *
 * @see <a href="https://github.com/envimate/message-mate#serializedmessagebus">Message Mate Documentation</a>
 */
public interface SerializedMessageBus {

    /**
     * Factory method to create a new {@code SerializedMessageBus} from the normal {@code MessageBus}, a {@code Deserializer}
     * and a {@code Serializer}.
     *
     * @param messageBus   the {@code MessageBus} to wrap
     * @param deserializer the {@code Deserializer} to deserialize {@link Map} data back to objects
     * @param serializer   the {@code Serializer} to serialize objects into a {@link Map}
     * @return the newly created {@code SerializedMessageBus}
     */
    static SerializedMessageBus aSerializedMessageBus(final MessageBus messageBus,
                                                      final Deserializer deserializer,
                                                      final Serializer serializer) {
        return new SerializedMessageBusImpl(messageBus, deserializer, serializer);
    }

    /**
     * Sends the given data in form of a {@code Map} on the {@code MessageBus} with the {@code EventType}.
     *
     * @param eventType the {@code EventType} to relate the message to
     * @param data      the data to send
     * @return a unique {@code MessageId} for the message
     */
    MessageId send(EventType eventType, Map<String, Object> data);

    /**
     * Sends the given data in form of a {@code Map} on the {@code MessageBus} with the {@code EventType} and
     * {@code CorrelationId} set.
     *
     * @param eventType     the {@code EventType} to relate the message to
     * @param data          the data to send
     * @param correlationId the {@code CorrelationId} relating to a previous {@code MessageId}
     * @return a unique {@code MessageId} for the message
     */
    MessageId send(EventType eventType, Map<String, Object> data, CorrelationId correlationId);

    /**
     * Sends the given data and error data both in form of a {@code Map} on the {@code MessageBus} with the {@code EventType}.
     *
     * @param eventType the {@code EventType} to relate the message to
     * @param data      the data to send
     * @param errorData the error data to send
     * @return a unique {@code MessageId} for the message
     */
    MessageId send(EventType eventType, Map<String, Object> data, Map<String, Object> errorData);

    /**
     * Sends the given data and error data both in form of a {@code Map} on the {@code MessageBus} with the {@code EventType}
     * and {@code CorrelationId}.
     *
     * @param eventType     the {@code EventType} to relate the message to
     * @param data          the data to send
     * @param errorData     the error data to send
     * @param correlationId the {@code CorrelationId} relating to a previous {@code MessageId}
     * @return a unique {@code MessageId} for the message
     */
    MessageId send(EventType eventType, Map<String, Object> data, Map<String, Object> errorData, CorrelationId correlationId);

    /**
     * Serializes the data to a {@link Map} and then sends it with the {@code EventType} on the {@code MessageBus}.
     *
     * @param eventType the {@code EventType} to relate the message to
     * @param data      the data to serialize and send
     * @return a unique {@code MessageId} for the message
     */
    MessageId serializeAndSend(EventType eventType, Object data);

    /**
     * Serializes the data to a {@link Map} and then sends it with the {@code EventType} and {@code CorrelationId} on the
     * {@code MessageBus}.
     *
     * @param eventType     the {@code EventType} to relate the message to
     * @param data          the data to serialize and send
     * @param correlationId the {@code CorrelationId} relating to a previous {@code MessageId}
     * @return a unique {@code MessageId} for the message
     */
    MessageId serializeAndSend(EventType eventType, Object data, CorrelationId correlationId);

    /**
     * Serializes the data and error data to a {@link Map} and then sends both with the {@code EventType} on the
     * {@code MessageBus}.
     *
     * @param eventType the {@code EventType} to relate the message to
     * @param data      the data to serialize and send
     * @param errorData the error data to send
     * @return a unique {@code MessageId} for the message
     */
    MessageId serializeAndSend(EventType eventType, Object data, Object errorData);

    /**
     * Serializes the data and error data to a {@link Map} and then sends both with the {@code EventType} and
     * {@code CorrelationId} on the {@code MessageBus}.
     *
     * @param eventType     the {@code EventType} to relate the message to
     * @param data          the data to serialize and send
     * @param errorData     the error data to send
     * @param correlationId the {@code CorrelationId} relating to a previous {@code MessageId}
     * @return a unique {@code MessageId} for the message
     */
    MessageId serializeAndSend(EventType eventType, Object data, Object errorData, CorrelationId correlationId);

    /**
     * Sends the data and waits for a matching response or an exception.
     *
     * @param eventType the {@code EventType} to relate the message to
     * @param data      the error data to send
     * @return the not deserialized normal and error payload
     * @throws InterruptedException if the waiting {@link Thread} is interrupted
     * @throws ExecutionException   if the message or response caused an exception
     */
    PayloadAndErrorPayload<Map<String, Object>, Map<String, Object>> invokeAndWait(
            EventType eventType,
            Map<String, Object> data) throws InterruptedException, ExecutionException;

    /**
     * Sends the data and waits for a matching response, an exception or the timeout to expire.
     *
     * @param eventType the {@code EventType} to relate the message to
     * @param data      the error data to send
     * @param timeout   the timeout interval
     * @param unit      the unit to measure the interval in
     * @return the not deserialized normal and error payload
     * @throws InterruptedException if the waiting {@link Thread} is interrupted
     * @throws ExecutionException   if the message or response caused an exception
     * @throws TimeoutException     if the timeout expired
     */
    PayloadAndErrorPayload<Map<String, Object>, Map<String, Object>> invokeAndWait(
            EventType eventType,
            Map<String, Object> data,
            long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException;

    /**
     * Serializes the data before sending it and waiting for a matching response or an exception.
     *
     * @param eventType the {@code EventType} to relate the message to
     * @param data      the error data to send
     * @return the not deserialized normal and error payload
     * @throws InterruptedException if the waiting {@link Thread} is interrupted
     * @throws ExecutionException   if the message or response caused an exception
     */
    PayloadAndErrorPayload<Map<String, Object>, Map<String, Object>> invokeAndWaitSerializedOnly(
            EventType eventType,
            Object data) throws InterruptedException, ExecutionException;

    /**
     * Serializes the data before sending it and waiting for a matching response, an exception or the expiration of the timeout.
     *
     * @param eventType the {@code EventType} to relate the message to
     * @param data      the error data to send
     * @param timeout   the timeout interval
     * @param unit      the unit to measure the interval in
     * @return the not deserialized normal and error payload
     * @throws InterruptedException if the waiting {@link Thread} is interrupted
     * @throws ExecutionException   if the message or response caused an exception
     * @throws TimeoutException     if the timeout expired
     */
    PayloadAndErrorPayload<Map<String, Object>, Map<String, Object>> invokeAndWaitSerializedOnly(
            EventType eventType,
            Object data,
            long timeout,
            TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException;

    /**
     * Serializes and sends the data on the {@code MessageBus}. Then the methods waits until a response is received or an
     * exception occurred. The response is deserialized into the two given classes.
     *
     * @param eventType         the {@code EventType} to relate the message to
     * @param data              the error data to send
     * @param responseClass     the class to deserialize the normal response to
     * @param errorPayloadClass the class to deserialize the error response to
     * @param <P>               the type to deserialize the normal response to
     * @param <E>               the type to deserialize the error response to
     * @return the deserialized normal and error payload
     * @throws InterruptedException if the waiting {@link Thread} is interrupted
     * @throws ExecutionException   if the message or response caused an exception
     */
    <P, E> PayloadAndErrorPayload<P, E> invokeAndWaitDeserialized(
            EventType eventType,
            Object data,
            Class<P> responseClass,
            Class<E> errorPayloadClass) throws InterruptedException, ExecutionException;

    /**
     * Serializes and sends the data on the {@code MessageBus}. Then the methods waits until a response is received, an
     * exception occurred or the timeout expired. The response is deserialized into the two given classes.
     *
     * @param eventType         the {@code EventType} to relate the message to
     * @param data              the error data to send
     * @param responseClass     the class to deserialize the normal response to
     * @param errorPayloadClass the class to deserialize the error response to
     * @param timeout           the timeout interval
     * @param unit              the unit to measure the interval in
     * @param <P>               the type to deserialize the normal response to
     * @param <E>               the type to deserialize the error response to
     * @return the deserialized normal and error payload
     * @throws InterruptedException if the waiting {@link Thread} is interrupted
     * @throws ExecutionException   if the message or response caused an exception
     * @throws TimeoutException     if the timeout expired
     */
    <P, E> PayloadAndErrorPayload<P, E> invokeAndWaitDeserialized(
            EventType eventType,
            Object data,
            Class<P> responseClass,
            Class<E> errorPayloadClass,
            long timeout,
            TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException;

    /**
     * Adds the given {@code Subscriber} for all not deserialized messages of the given {@code EventType}.
     *
     * @param eventType  the {@code EventType} of the messages to receive
     * @param subscriber the {@code Subscriber} to invoke
     * @return a {@code SubscriptionId} to identify the {@code Subscriber}
     */
    SubscriptionId subscribe(EventType eventType,
                             Subscriber<PayloadAndErrorPayload<Map<String, Object>, Map<String, Object>>> subscriber);

    /**
     * Adds the given {@code Subscriber} for all not deserialized messages of the given {@code CorrelationId}.
     *
     * @param correlationId the {@code CorrelationId} of the messages to receive
     * @param subscriber    the {@code Subscriber} to invoke
     * @return a {@code SubscriptionId} to identify the {@code Subscriber}
     */
    SubscriptionId subscribe(CorrelationId correlationId,
                             Subscriber<PayloadAndErrorPayload<Map<String, Object>, Map<String, Object>>> subscriber);

    /**
     * Adds the given {@code Subscriber} for all messages of the given {@code EventType}. The normal and error payload are
     * deserialized into the to given {@code Classes}.
     *
     * @param eventType     the {@code EventType} of the messages to receive
     * @param subscriber    the {@code Subscriber} to invoke
     * @param responseClass the {@code Class} to deserialize the normal payload into
     * @param errorClass    the {@code Class} to deserialize the error payload into
     * @param <P>           the type to deserialize the normal payload into
     * @param <E>           the type to deserialize the error payload into
     * @return a {@code SubscriptionId} to identify the {@code Subscriber}
     */
    <P, E> SubscriptionId subscribeDeserialized(EventType eventType,
                                                Subscriber<PayloadAndErrorPayload<P, E>> subscriber,
                                                Class<P> responseClass,
                                                Class<E> errorClass);

    /**
     * Adds the given {@code Subscriber} for all messages of the given {@code EventType}. The normal and error payload are
     * deserialized into the to given {@code Classes}.
     *
     * @param correlationId the {@code CorrelationId} of the messages to receive
     * @param subscriber    the {@code Subscriber} to invoke
     * @param responseClass the {@code Class} to deserialize the normal payload into
     * @param errorClass    the {@code Class} to deserialize the error payload into
     * @param <P>           the type to deserialize the normal payload into
     * @param <E>           the type to deserialize the error payload into
     * @return a {@code SubscriptionId} to identify the {@code Subscriber}
     */
    <P, E> SubscriptionId subscribeDeserialized(CorrelationId correlationId,
                                                Subscriber<PayloadAndErrorPayload<P, E>> subscriber,
                                                Class<P> responseClass,
                                                Class<E> errorClass);

    /**
     * Adds the {@code Subscriber} with access to the raw {@code ProcessingContext} for all messages of the {@code EventType}.
     *
     * @param eventType  the {@code EventType} of the messages to receive
     * @param subscriber the {@code Subscriber} to invoke
     * @return a {@code SubscriptionId} to identify the {@code Subscriber}
     */
    SubscriptionId subscribeRaw(EventType eventType, Subscriber<ProcessingContext<Map<String, Object>>> subscriber);

    /**
     * Removes all {@code Subscribers} with the given {@code SubscriptionId}.
     *
     * @param subscriptionId the {@code SubscriptionId} to remove
     */
    void unsubscribe(SubscriptionId subscriptionId);

}
