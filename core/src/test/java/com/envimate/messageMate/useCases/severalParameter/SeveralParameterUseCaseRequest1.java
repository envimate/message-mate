package com.envimate.messageMate.useCases.severalParameter;

import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
public class SeveralParameterUseCaseRequest1 {
    public final int intParameter;
    public final Boolean booleanParameter;

}
