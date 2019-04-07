package com.envimate.messageMate.serializedMessageBus;

import com.envimate.messageMate.identification.CorrelationId;
import com.envimate.messageMate.identification.MessageId;
import com.envimate.messageMate.messageBus.EventType;
import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.messageBus.PayloadAndErrorPayload;
import com.envimate.messageMate.messageFunction.MessageFunction;
import com.envimate.messageMate.messageFunction.MessageFunctionBuilder;
import com.envimate.messageMate.messageFunction.ResponseFuture;
import com.envimate.messageMate.processingContext.ProcessingContext;
import com.envimate.messageMate.subscribing.AcceptingBehavior;
import com.envimate.messageMate.subscribing.Subscriber;
import com.envimate.messageMate.subscribing.SubscriptionId;
import com.envimate.messageMate.useCases.useCaseAdapter.mapping.RequestDeserializer;
import com.envimate.messageMate.useCases.useCaseAdapter.mapping.ResponseSerializer;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.envimate.messageMate.messageBus.PayloadAndErrorPayload.payloadAndErrorPayload;
import static lombok.AccessLevel.PRIVATE;

public class SerializedMessageBusImpl implements SerializedMessageBus {
    private final MessageBus messageBus;
    private final RequestDeserializer requestDeserializer; //TODO: better names
    private final ResponseSerializer responseSerializer;
    private final MessageFunction messageFunction;

    SerializedMessageBusImpl(MessageBus messageBus, RequestDeserializer requestDeserializer, ResponseSerializer responseSerializer) {
        this.messageBus = messageBus;
        this.requestDeserializer = requestDeserializer;
        this.responseSerializer = responseSerializer;
        this.messageFunction = MessageFunctionBuilder.aMessageFunction(messageBus);
    }

    @Override
    public MessageId send(EventType eventType, Map<String, Object> data) {
        return messageBus.send(eventType, data);
    }

    @Override
    public MessageId send(EventType eventType, Map<String, Object> data, CorrelationId correlationId) {
        return messageBus.send(eventType, data, correlationId);
    }

    @Override
    public MessageId send(EventType eventType, Map<String, Object> data, Map<String, Object> errorData) {
        final ProcessingContext<Object> processingContext = ProcessingContext.processingContextForPayloadAndError(eventType, data, errorData);
        return messageBus.send(processingContext);
    }

    @Override
    public MessageId send(EventType eventType, Map<String, Object> data, Map<String, Object> errorData, CorrelationId correlationId) {
        final ProcessingContext<Object> processingContext = ProcessingContext.processingContextForPayloadAndError(eventType, correlationId, data, errorData);
        return messageBus.send(processingContext);
    }

    @Override
    public MessageId serializeAndSend(EventType eventType, Object data) {
        final Map<String, Object> map = responseSerializer.serializeReturnValue(data);
        return send(eventType, map);
    }

    @Override
    public MessageId serializeAndSend(EventType eventType, Object data, CorrelationId correlationId) {
        final Map<String, Object> map = responseSerializer.serializeReturnValue(data);
        return send(eventType, map, correlationId);
    }

    @Override
    public MessageId serializeAndSend(EventType eventType, Object data, Object errorData) {
        final Map<String, Object> map = responseSerializer.serializeReturnValue(data);
        return send(eventType, map, responseSerializer.serializeReturnValue(errorData));
    }

    @Override
    public MessageId serializeAndSend(EventType eventType, Object data, Object errorData, CorrelationId correlationId) {
        final Map<String, Object> payloadMap = responseSerializer.serializeReturnValue(data);
        final Map<String, Object> errorPayloadMap = responseSerializer.serializeReturnValue(errorData);
        return send(eventType, payloadMap, errorPayloadMap, correlationId);
    }

    @Override
    public PayloadAndErrorPayload<Map<String, Object>, Map<String, Object>> invokeAndWait(EventType eventType, Map<String, Object> data) throws ExecutionException, InterruptedException {
        final ResponseFuture responseFuture = messageFunction.request(eventType, data);
        final ProcessingContext<Object> processingContext = responseFuture.getRaw();
        final Map<String, Object> payload = (Map<String, Object>) processingContext.getPayload();
        final Map<String, Object> errorPayload = (Map<String, Object>) processingContext.getErrorPayload();
        return payloadAndErrorPayload(payload, errorPayload);
    }


    @Override
    public <P, E> PayloadAndErrorPayload<P, E> invokeAndWaitDeserialized(EventType eventType, Object data, Class<P> responseClass, Class<E> errorPayloadClass) throws InterruptedException, ExecutionException {
        final Map<String, Object> map = responseSerializer.serializeReturnValue(data);
        final PayloadAndErrorPayload<Map<String, Object>, Map<String, Object>> mapPayloadAndErrorPayload = invokeAndWait(eventType, map);
        final PayloadAndErrorPayload<P, E> payloadAndErrorPayload = deserialize(mapPayloadAndErrorPayload, responseClass, errorPayloadClass);
        return payloadAndErrorPayload;
    }

    @Override
    public PayloadAndErrorPayload<Map<String, Object>, Map<String, Object>> invokeAndWait(EventType eventType, Map<String, Object> data, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        final ResponseFuture responseFuture = messageFunction.request(eventType, data);
        final ProcessingContext<Object> processingContext = responseFuture.getRaw(timeout, unit);
        final Map<String, Object> payload = (Map<String, Object>) processingContext.getPayload();
        final Map<String, Object> errorPayload = (Map<String, Object>) processingContext.getErrorPayload();
        return payloadAndErrorPayload(payload, errorPayload);
    }

    @Override
    public <P, E> PayloadAndErrorPayload<P, E> invokeAndWaitDeserialized(EventType eventType, Object data, Class<P> responseClass, Class<E> errorPayloadClass, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        final Map<String, Object> map = responseSerializer.serializeReturnValue(data);
        final PayloadAndErrorPayload<Map<String, Object>, Map<String, Object>> mapPayloadAndErrorPayload = invokeAndWait(eventType, map, timeout, unit);
        final PayloadAndErrorPayload<P, E> payloadAndErrorPayload = deserialize(mapPayloadAndErrorPayload, responseClass, errorPayloadClass);
        return payloadAndErrorPayload;
    }

    @Override
    public SubscriptionId subscribe(EventType eventType, Subscriber<PayloadAndErrorPayload<Map<String, Object>, Map<String, Object>>> subscriber) {
        return messageBus.subscribeRaw(eventType, new PayloadAndErrorPayloadSubscriberWrapper(subscriber));
    }

    @Override
    public SubscriptionId subscribe(CorrelationId correlationId, Subscriber<PayloadAndErrorPayload<Map<String, Object>, Map<String, Object>>> subscriber) {
        return messageBus.subscribe(correlationId, new PayloadAndErrorPayloadSubscriberWrapper(subscriber));
    }

    @Override
    public <P, E> SubscriptionId subscribeDeserialized(EventType eventType, Subscriber<PayloadAndErrorPayload<P, E>> subscriber, Class<P> responseClass, Class<E> errorClass) {
        return messageBus.subscribeRaw(eventType, new DeserializingSubscriberWrapper<>(subscriber, responseClass, errorClass));
    }

    @Override
    public <P, E> SubscriptionId subscribeDeserialized(CorrelationId correlationId, Subscriber<PayloadAndErrorPayload<P, E>> subscriber, Class<P> responseClass, Class<E> errorClass) {
        return messageBus.subscribe(correlationId, new DeserializingSubscriberWrapper<>(subscriber, responseClass, errorClass));
    }


    @Override
    public SubscriptionId subscribeRaw(EventType eventType, Subscriber<ProcessingContext<Map<String, Object>>> subscriber) {
        final Subscriber genericErasedSubscriber = subscriber;
        final Subscriber<ProcessingContext<Object>> castedSubscriber = (Subscriber<ProcessingContext<Object>>) genericErasedSubscriber;
        return messageBus.subscribeRaw(eventType, castedSubscriber);
    }

    private <P, E> PayloadAndErrorPayload<P, E> deserialize(PayloadAndErrorPayload<Map<String, Object>, Map<String, Object>> mapPayloadAndErrorPayload, Class<P> responseClass, Class<E> errorPayloadClass) {
        final Map<String, Object> payloadMap = mapPayloadAndErrorPayload.getPayload();
        final Map<String, Object> errorPayloadMap = mapPayloadAndErrorPayload.getErrorPayload();
        return deserialize(payloadMap, responseClass, errorPayloadMap, errorPayloadClass);
    }

    private <P, E> PayloadAndErrorPayload<P, E> deserialize(Map<String, Object> payloadMap, Class<P> responseClass, Map<String, Object> errorPayloadMap, Class<E> errorPayloadClass) {
        final P payload = requestDeserializer.deserializeRequest(responseClass, payloadMap);
        final E errorPayload;
        if(errorPayloadMap != null){
            errorPayload = requestDeserializer.deserializeRequest(errorPayloadClass, errorPayloadMap);
        }else{
            errorPayload = null;
        }
        return payloadAndErrorPayload(payload, errorPayload);
    }

    @RequiredArgsConstructor(access = PRIVATE)
    private static final class PayloadAndErrorPayloadSubscriberWrapper implements Subscriber<ProcessingContext<Object>> {

        private final Subscriber<PayloadAndErrorPayload<Map<String, Object>, Map<String, Object>>> subscriber;

        @Override
        public AcceptingBehavior accept(ProcessingContext<Object> processingContext) {
            final Map<String, Object> payload = (Map<String, Object>) processingContext.getPayload();
            final Map<String, Object> errorPayload = (Map<String, Object>) processingContext.getErrorPayload();
            final PayloadAndErrorPayload<Map<String, Object>, Map<String, Object>> payloadAndErrorPayload = payloadAndErrorPayload(payload, errorPayload);
            return subscriber.accept(payloadAndErrorPayload);
        }

        @Override
        public SubscriptionId getSubscriptionId() {
            return SubscriptionId.newUniqueId();
        }
    }

    @RequiredArgsConstructor(access = PRIVATE)
    private class DeserializingSubscriberWrapper<P, E> implements Subscriber<ProcessingContext<Object>> {
        private final Subscriber<PayloadAndErrorPayload<P, E>> subscriber;
        private final Class<P> responseClass;
        private final Class<E> errorClass;

        @Override
        public AcceptingBehavior accept(ProcessingContext<Object> processingContext) {
            final Map<String, Object> payloadMap = (Map<String, Object>) processingContext.getPayload();
            final Map<String, Object> errorPayloadMap = (Map<String, Object>) processingContext.getErrorPayload();
            final PayloadAndErrorPayload<P, E> pePayloadAndErrorPayload = deserialize(payloadMap, responseClass, errorPayloadMap, errorClass);
            return subscriber.accept(pePayloadAndErrorPayload);
        }


        @Override
        public SubscriptionId getSubscriptionId() {
            return SubscriptionId.newUniqueId();
        }
    }
}
