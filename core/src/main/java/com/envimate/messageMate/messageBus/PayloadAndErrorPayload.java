package com.envimate.messageMate.messageBus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class PayloadAndErrorPayload<P, E> {
    @Getter
    private final P payload;
    @Getter
    private final E errorPayload;

    public static <P, E> PayloadAndErrorPayload<P, E> payloadAndErrorPayload(final P payload, final E errorPayload) {
        return new PayloadAndErrorPayload<>(payload, errorPayload);
    }
}
