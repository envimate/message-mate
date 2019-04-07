package com.envimate.messageMate.serializedMessageBus;

import com.envimate.messageMate.messageBus.EventType;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class SerializedMessageBusTestProperties {
    public static final String EVENT_TYPE = "EVENT_TYPE";
    public static final EventType DEFAULT_EVENT_TYPE = EventType.eventTypeFromString("defaultEventType");
    public static final EventType EVENT_TYPE_WITH_NO_SUBSCRIBERS = EventType.eventTypeFromString("eventTypeWithNoSubscribers");
    public static final String SEND_DATA = "SEND_DATA";
    public static final String SEND_ERROR_DATA = "SEND_ERROR_DATA";
}
