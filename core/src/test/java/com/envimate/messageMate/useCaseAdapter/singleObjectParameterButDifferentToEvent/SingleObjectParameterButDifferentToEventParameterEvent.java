package com.envimate.messageMate.useCaseAdapter.singleObjectParameterButDifferentToEvent;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class SingleObjectParameterButDifferentToEventParameterEvent {
    @Getter
    private final String message;

    public static SingleObjectParameterButDifferentToEventParameterEvent singleObjectParameterButDifferentToEvent(final String message) {
        return new SingleObjectParameterButDifferentToEventParameterEvent(message);
    }
}
