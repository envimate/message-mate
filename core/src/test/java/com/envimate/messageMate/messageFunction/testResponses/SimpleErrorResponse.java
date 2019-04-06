package com.envimate.messageMate.messageFunction.testResponses;

import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class SimpleErrorResponse implements TestResponse {
    private final Object request;

    public static SimpleErrorResponse simpleErrorResponse(final Object request) {
        return new SimpleErrorResponse(request);
    }

    @Override
    public Object getCorrelatedRequest() {
        return request;
    }
}
