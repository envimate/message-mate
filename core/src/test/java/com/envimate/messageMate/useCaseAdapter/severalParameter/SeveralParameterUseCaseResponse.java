package com.envimate.messageMate.useCaseAdapter.severalParameter;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PACKAGE;

@EqualsAndHashCode
@RequiredArgsConstructor(access = PACKAGE)
public class SeveralParameterUseCaseResponse {
    @Getter
    private final int intParameter;
    @Getter
    private final Boolean booleanParameter;
    @Getter
    private final Object objectParameter;
    @Getter
    private final String stringParameter;

}
