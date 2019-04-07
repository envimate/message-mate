package com.envimate.messageMate.useCases.useCaseBus;

import com.envimate.messageMate.messageBus.EventType;
import com.envimate.messageMate.messageBus.PayloadAndErrorPayload;
import com.envimate.messageMate.serializedMessageBus.SerializedMessageBus;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public interface UseCaseBus {

    static UseCaseBus useCaseBus(final SerializedMessageBus serializedMessageBus){
        return new UseCaseBusImpl(serializedMessageBus);
    }

    <P,E> PayloadAndErrorPayload<P,E> invokeAndWait(EventType eventType,
                                                    Object data,
                                                    Class<P> payloadClass,
                                                    Class<E> errorPayloadClass) throws InterruptedException, ExecutionException, TimeoutException;

    <P,E> PayloadAndErrorPayload<P,E> invokeAndWait(EventType eventType,
                                                    Object data,
                                                    Class<P> payloadClass,
                                                    Class<E> errorPayloadClass,
                                                    long timeout,
                                                    TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException;

     PayloadAndErrorPayload<Map<String, Object>, Map<String, Object>> invokeAndWaitNotDeserialized(EventType eventType,
                                                                                                   Object data) throws InterruptedException, ExecutionException, TimeoutException;

    PayloadAndErrorPayload<Map<String, Object>,Map<String, Object>> invokeAndWaitNotDeserialized(EventType eventType,
                                                                                                 Object data,
                                                                                                 long timeout,
                                                                                                 TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException;

}
