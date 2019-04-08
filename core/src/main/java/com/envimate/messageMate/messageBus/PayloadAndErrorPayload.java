package com.envimate.messageMate.messageBus;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static lombok.AccessLevel.PRIVATE;

@ToString
@EqualsAndHashCode
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
