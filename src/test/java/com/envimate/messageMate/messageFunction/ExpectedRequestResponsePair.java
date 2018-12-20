package com.envimate.messageMate.messageFunction;

import com.envimate.messageMate.correlation.CorrelationId;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class ExpectedRequestResponsePair {
    public final TestRequest request;
    public final TestResponse response;

    public static ExpectedRequestResponsePair generateNewPair() {
        final SimpleTestRequest testRequest = SimpleTestRequest.testRequest();
        final CorrelationId correlationId = testRequest.getCorrelationId();
        final SimpleTestResponse testResponse = SimpleTestResponse.testResponse(correlationId);
        return new ExpectedRequestResponsePair(testRequest, testResponse);
    }

    public static ExpectedRequestResponsePair generateNewPairWithAlternativeResponse() {
        final SimpleTestRequest testRequest = SimpleTestRequest.testRequest();
        final CorrelationId correlationId = testRequest.getCorrelationId();
        final AlternativTestResponse testResponse = AlternativTestResponse.alternativTestResponse(correlationId);
        return new ExpectedRequestResponsePair(testRequest, testResponse);
    }
}
