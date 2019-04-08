package com.envimate.messageMate.useCases.severalParameter;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PACKAGE;

@EqualsAndHashCode
@RequiredArgsConstructor(access = PACKAGE)
public class SeveralParameterUseCaseCombinedRequest {
    public final int intParameter;
    public final Boolean booleanParameter;
    public final String stringParameter;
    public final Object objectParameter;

}
