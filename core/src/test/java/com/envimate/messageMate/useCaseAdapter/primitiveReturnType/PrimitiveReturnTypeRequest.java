package com.envimate.messageMate.useCaseAdapter.primitiveReturnType;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
public final class PrimitiveReturnTypeRequest {
    @Getter
    private final int value;
}