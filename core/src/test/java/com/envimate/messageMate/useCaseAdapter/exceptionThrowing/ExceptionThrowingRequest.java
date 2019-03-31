package com.envimate.messageMate.useCaseAdapter.exceptionThrowing;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
public class ExceptionThrowingRequest {
    @Getter
    private final Exception exceptionToThrow;
}
