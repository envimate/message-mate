package com.envimate.messageMate.useCaseAdapter.severalParameter;

import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
public class SeveralParameterUseCaseRequest2 {
    public final String stringParameter;
    public final Object objectParameter;
}
