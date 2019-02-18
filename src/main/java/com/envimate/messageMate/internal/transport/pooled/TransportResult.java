package com.envimate.messageMate.internal.transport.pooled;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
final class TransportResult<T> {
    @Getter
    private final T message;
    @Getter
    private final RESULT_TYPE resultType;


    public static <T> TransportResult<T> transportResult(final T message, final RESULT_TYPE resultType) {
        return new TransportResult<>(message, resultType);
    }

    enum RESULT_TYPE {PASSED_BUT_YET_TO_DELIVER, BLOCKED, REPLACED, FORGOTTEN, MESSAGES_HANDED_OF_TO_DELIVERY}
}
