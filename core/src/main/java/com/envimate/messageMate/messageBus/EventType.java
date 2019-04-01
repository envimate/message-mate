package com.envimate.messageMate.messageBus;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static com.envimate.messageMate.internal.enforcing.StringValidator.cleaned;
import static lombok.AccessLevel.PRIVATE;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = PRIVATE)
public final class EventType {
    private final String value;

    public static EventType eventTypeFromString(final String value) {
        final String cleaned = cleaned(value);
        return new EventType(cleaned);
    }

    public String stringValue() {
        return value;
    }
}
