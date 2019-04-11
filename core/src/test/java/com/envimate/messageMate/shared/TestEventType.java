package com.envimate.messageMate.shared;

import com.envimate.messageMate.processingContext.EventType;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class TestEventType {

    public static EventType testEventType() {
        return EventType.eventTypeFromString("testEventType_A");
    }

    public static EventType differentTestEventType() {
        return EventType.eventTypeFromString("testEventType_B");
    }
}
