package com.envimate.messageMate.messageFunction.testResponses;

import com.envimate.messageMate.correlation.CorrelationId;

public interface TestRequest {

    CorrelationId getCorrelationId();
}
