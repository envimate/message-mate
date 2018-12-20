package com.envimate.messageMate.messageFunction;

import com.envimate.messageMate.correlation.CorrelationId;

public interface TestRequest {

    CorrelationId getCorrelationId();
}
