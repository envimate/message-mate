package com.envimate.messageMate.useCaseAdapter.severalParameter;

import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
public class SeveralParameterUseCaseRequest1 {
    public final int intParameter;
    public final Boolean booleanParameter;

}
