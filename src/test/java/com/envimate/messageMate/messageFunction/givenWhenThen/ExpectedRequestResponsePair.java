package com.envimate.messageMate.messageFunction.givenWhenThen;

import com.envimate.messageMate.correlation.CorrelationId;
import com.envimate.messageMate.messageFunction.testResponses.*;
import lombok.RequiredArgsConstructor;

import static com.envimate.messageMate.messageFunction.testResponses.AlternativTestResponse.alternativTestResponse;
import static com.envimate.messageMate.messageFunction.testResponses.SimpleTestResponse.testResponse;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class ExpectedRequestResponsePair {
    public final TestRequest request;
    public final TestResponse response;

    public static ExpectedRequestResponsePair generateNewPair() {
        final SimpleTestRequest testRequest = SimpleTestRequest.testRequest();
        final CorrelationId correlationId = testRequest.getCorrelationId();
        final SimpleTestResponse testResponse = testResponse(correlationId);
        return new ExpectedRequestResponsePair(testRequest, testResponse);
    }

    public static ExpectedRequestResponsePair generateNewPairWithAlternativeResponse() {
        final SimpleTestRequest testRequest = SimpleTestRequest.testRequest();
        final CorrelationId correlationId = testRequest.getCorrelationId();
        final AlternativTestResponse testResponse = alternativTestResponse(correlationId);
        return new ExpectedRequestResponsePair(testRequest, testResponse);
    }
}
