package com.envimate.messageMate.messageFunction.testResponses;

import com.envimate.messageMate.messageFunction.ResponseFuture;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class RequestResponseFuturePair {

    @Getter
    private final TestRequest testRequest;

    @Getter
    private final ResponseFuture<TestResponse> responseFuture;

    public static RequestResponseFuturePair requestResponseFuturePair(final TestRequest testRequest, final ResponseFuture<TestResponse> responseFuture) {
        return new RequestResponseFuturePair(testRequest, responseFuture);
    }

}
