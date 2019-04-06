package com.envimate.messageMate.useCaseAdapter;

import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class UseCaseAdapterTestProperties {
    public static final String EVENT_TYPE = "EVENT_TYPE";
    public static final String MESSAGE_FUNCTION_USED = "MESSAGE_FUNCTION_USED";
    public static final String RETRIEVE_ERROR_FROM_FUTURE = "RETRIEVE_ERROR_FROM_FUTURE";
}
