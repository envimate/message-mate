package com.envimate.messageMate.messageFunction.testResponses;

import com.envimate.messageMate.correlation.CorrelationId;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class AlternativTestResponse implements TestResponse {
    private final CorrelationId correlationId;

    public static AlternativTestResponse alternativTestResponse(final CorrelationId correlationId) {
        return new AlternativTestResponse(correlationId);
    }

    @Override
    public CorrelationId getCorrelationId() {
        return correlationId;
    }
}
