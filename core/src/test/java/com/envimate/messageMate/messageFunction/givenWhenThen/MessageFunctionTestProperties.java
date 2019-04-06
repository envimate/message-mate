package com.envimate.messageMate.messageFunction.givenWhenThen;

import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class MessageFunctionTestProperties {
    public final static String EVENT_TYPE = "EVENT_TYPE";
    public final static String CANCEL_RESULTS = "CANCEL_RESULTS";
    public final static String EXCEPTION_OCCURRED_DURING_SEND = "EXCEPTION_OCCURRED_DURING_SEND";
    public final static String EXCEPTION_OCCURRED_DURING_FOLLOW_UP = "EXCEPTION_OCCURRED_DURING_FOLLOW_UP";
    public final static String RESPONSE_PROCESSING_CONTEXT = "RESPONSE_PROCESSING_CONTEXT";
}
