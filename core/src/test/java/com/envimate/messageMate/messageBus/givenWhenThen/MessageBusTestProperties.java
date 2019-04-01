package com.envimate.messageMate.messageBus.givenWhenThen;

import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class MessageBusTestProperties {
    public final static String EVENT_TYPE = "EVENT_TYPE";
    public final static String CORRELATION_SUBSCRIPTION_ID = "CORRELATION_SUBSCRIPTION_ID";
}
