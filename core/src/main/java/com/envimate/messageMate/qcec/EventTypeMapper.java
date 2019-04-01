package com.envimate.messageMate.qcec;

import com.envimate.messageMate.messageBus.EventType;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class EventTypeMapper {

    public static EventType eventTypeFor(final Object query) {
        final Class<?> queryClass = query.getClass();
        return eventTypeFor(queryClass);
    }

    public static EventType eventTypeFor(final Class<?> queryClass) {
        final String canonicalName = queryClass.getCanonicalName();
        return EventType.eventTypeFromString(canonicalName);
    }
}
