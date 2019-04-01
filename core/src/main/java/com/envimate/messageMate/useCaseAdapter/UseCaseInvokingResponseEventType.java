package com.envimate.messageMate.useCaseAdapter;

import com.envimate.messageMate.messageBus.EventType;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class UseCaseInvokingResponseEventType {
    public static final EventType USE_CASE_RESPONSE_EVENT_TYPE = EventType.eventTypeFromString("UseCaseResponse");
}
