package com.envimate.messageMate.useCases.useCaseBus;

import com.envimate.messageMate.messageBus.EventType;
import com.envimate.messageMate.messageBus.PayloadAndErrorPayload;
import com.envimate.messageMate.serializedMessageBus.SerializedMessageBus;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
public class UseCaseBusImpl implements UseCaseBus {
    private final SerializedMessageBus serializedMessageBus;

    @Override
    public <P, E> PayloadAndErrorPayload<P, E> invokeAndWait(EventType eventType, Object data, Class<P> payloadClass, Class<E> errorPayloadClass) throws InterruptedException, ExecutionException, TimeoutException {
        return serializedMessageBus.invokeAndWaitDeserialized(eventType, data, payloadClass, errorPayloadClass);
    }

    @Override
    public <P, E> PayloadAndErrorPayload<P, E> invokeAndWait(EventType eventType, Object data, Class<P> payloadClass, Class<E> errorPayloadClass, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return serializedMessageBus.invokeAndWaitDeserialized(eventType, data, payloadClass, errorPayloadClass, timeout, unit);
    }
}
