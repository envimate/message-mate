package com.envimate.messageMate.messageFunction;

import com.envimate.messageMate.correlation.CorrelationId;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class SimpleTestRequest implements TestRequest {
    private final CorrelationId correlationId = CorrelationId.newUniqueId();

    public static SimpleTestRequest testRequest() {
        return new SimpleTestRequest();
    }

    @Override
    public CorrelationId getCorrelationId() {
        return correlationId;
    }
}
