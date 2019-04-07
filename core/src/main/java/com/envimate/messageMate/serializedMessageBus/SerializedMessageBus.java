package com.envimate.messageMate.serializedMessageBus;

import com.envimate.messageMate.identification.CorrelationId;
import com.envimate.messageMate.identification.MessageId;
import com.envimate.messageMate.messageBus.EventType;
import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.messageBus.PayloadAndErrorPayload;
import com.envimate.messageMate.processingContext.ProcessingContext;
import com.envimate.messageMate.subscribing.Subscriber;
import com.envimate.messageMate.subscribing.SubscriptionId;
import com.envimate.messageMate.useCases.useCaseAdapter.mapping.RequestDeserializer;
import com.envimate.messageMate.useCases.useCaseAdapter.mapping.ResponseSerializer;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public interface SerializedMessageBus {

    static SerializedMessageBus aSerializedMessageBus(final MessageBus messageBus,
                                                      final RequestDeserializer requestDeserializer,
                                                      final ResponseSerializer responseSerializer) {
        return new SerializedMessageBusImpl(messageBus, requestDeserializer, responseSerializer);
    }

    MessageId send(EventType eventType, Map<String, Object> data);

    MessageId send(EventType eventType, Map<String, Object> data, CorrelationId correlationId);

    MessageId send(EventType eventType, Map<String, Object> data, Map<String, Object> errorData);

    MessageId send(EventType eventType, Map<String, Object> data, Map<String, Object> errorData, CorrelationId correlationId);

    MessageId serializeAndSend(EventType eventType, Object data);

    MessageId serializeAndSend(EventType eventType, Object data, CorrelationId correlationId);

    MessageId serializeAndSend(EventType eventType, Object data, Object errorData);

    MessageId serializeAndSend(EventType eventType, Object data, Object errorData, CorrelationId correlationId);

    PayloadAndErrorPayload<Map<String, Object>, Map<String, Object>> invokeAndWait(EventType eventType, Map<String, Object> data) throws InterruptedException, ExecutionException;

    <P, E> PayloadAndErrorPayload<P, E> invokeAndWaitDeserialized(EventType eventType, Object data, Class<P> responseClass, Class<E> errorPayloadClass) throws InterruptedException, ExecutionException;

    //TODO: cancel on exception
    PayloadAndErrorPayload<Map<String, Object>, Map<String, Object>> invokeAndWait(EventType eventType, Map<String, Object> data, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException;

    <P, E> PayloadAndErrorPayload<P, E> invokeAndWaitDeserialized(EventType eventType, Object data, Class<P> responseClass, Class<E> errorPayloadClass, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException;

    SubscriptionId subscribe(EventType eventType, Subscriber<PayloadAndErrorPayload<Map<String, Object>, Map<String, Object>>> subscriber);

    SubscriptionId subscribe(CorrelationId correlationId, Subscriber<PayloadAndErrorPayload<Map<String, Object>, Map<String, Object>>> subscriber);

    <P,E> SubscriptionId subscribeDeserialized(EventType eventType, Subscriber<PayloadAndErrorPayload<P, E>> subscriber, Class<P> responseClass, Class<E> errorClass);

    <P,E> SubscriptionId subscribeDeserialized(CorrelationId correlationId, Subscriber<PayloadAndErrorPayload<P, E>> subscriber, Class<P> responseClass, Class<E> errorClass);

    SubscriptionId subscribeRaw(EventType eventType, Subscriber<ProcessingContext<Map<String, Object>>> subscriber);

}
