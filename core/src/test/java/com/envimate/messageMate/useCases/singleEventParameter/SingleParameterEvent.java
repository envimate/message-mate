package com.envimate.messageMate.useCases.singleEventParameter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class SingleParameterEvent {
    @Getter
    private final String message;

    public static SingleParameterEvent singleParameterEvent(final String message) {
        return new SingleParameterEvent(message);
    }
}
