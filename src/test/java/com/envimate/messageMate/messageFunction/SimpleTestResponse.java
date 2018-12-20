package com.envimate.messageMate.messageFunction;

import com.envimate.messageMate.correlation.CorrelationId;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class SimpleTestResponse implements TestResponse {
    private final CorrelationId correlationId;

    public static SimpleTestResponse testResponse(final CorrelationId correlationId) {
        return new SimpleTestResponse(correlationId);
    }

    @Override
    public CorrelationId getCorrelationId() {
        return correlationId;
    }
}
