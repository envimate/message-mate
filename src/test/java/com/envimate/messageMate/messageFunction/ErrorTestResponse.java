package com.envimate.messageMate.messageFunction;

import com.envimate.messageMate.correlation.CorrelationId;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class ErrorTestResponse implements TestResponse {
    private final CorrelationId correlationId;

    public static ErrorTestResponse errorTestResponse(final CorrelationId correlationId) {
        return new ErrorTestResponse(correlationId);
    }

    @Override
    public CorrelationId getCorrelationId() {
        return correlationId;
    }
}
