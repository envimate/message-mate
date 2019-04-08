package com.envimate.messageMate.useCases;

import com.envimate.messageMate.messageBus.EventType;
import com.envimate.messageMate.messageBus.PayloadAndErrorPayload;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
public final class UseCaseBusCall {
    @Getter
    private final EventType eventType;
    @Getter
    private final Object data;
    @Getter
    private final Class<?> payloadClass;
    @Getter
    private final Class<?> errorPayloadClass;
    @Getter
    private final PayloadAndErrorPayload<?, ?> expectedResult;
    @Getter
    private final PayloadAndErrorPayload<Map<String,Object>, Map<String, Object>> notDeserializedExpectedResult;
}
