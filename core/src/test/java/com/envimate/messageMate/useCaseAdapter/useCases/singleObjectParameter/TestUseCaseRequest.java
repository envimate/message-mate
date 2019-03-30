package com.envimate.messageMate.useCaseAdapter.useCases.singleObjectParameter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class TestUseCaseRequest {
    @Getter
    private final String message;

    public static TestUseCaseRequest testUseCaseRequest(final String message) {
        return new TestUseCaseRequest(message);
    }
}
