package com.envimate.messageMate.useCaseAdapter.severalParameter;

public class SeveralParameterUseCase {

    public SeveralParameterUseCaseResponse useCaseMethod(final String stringParameter, final Object objectParameter, final int intParameter) {
        return new SeveralParameterUseCaseResponse(stringParameter, objectParameter, intParameter);
    }
}
