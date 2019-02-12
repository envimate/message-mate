package com.envimate.messageMate.shared.channelMessageBus.givenWhenThen;

import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class ChannelMessageBusTestProperties {
    public static final String SINGLE_SEND_MESSAGE = "SINGLE_SEND_MESSAGE";
    public static final String EXPECTED_RECEIVERS = "EXPECTED_RECEIVERS";
    public static final String INITIAL_SUBSCRIBER = "INITIAL_SUBSCRIBER";
    public static final String EXPECTED_SUBSCRIBER = "EXPECTED_SUBSCRIBER";
    public static final String POTENTIAL_RECEIVERS = "POTENTIAL_RECEIVERS";
    public static final String SINGLE_RECEIVER = "SINGLE_RECEIVER";
    public static final String MESSAGES_SEND = "MESSAGES_SEND";
    public static final String MESSAGES_SEND_OF_INTEREST = "MESSAGES_SEND_OF_INTEREST";
    public static final String EXPECTED_CHANGED_CONTENT = "EXPECTED_CHANGED_CONTENT";
    public static final String EXECUTION_END_SEMAPHORE = "EXECUTION_END_SEMAPHORE";
    public static final String EXECUTE_MESSAGE_BUS_IN_OWN_THREAD = "EXECUTE_MESSAGE_BUS_IN_OWN_THREAD";
    public static final String ERROR_SUBSCRIBER = "ERROR_SUBSCRIBER";
    public static final String EXPECTED_FILTER = "EXPECTED_FILTER";
}
