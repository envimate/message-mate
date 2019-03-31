package com.envimate.messageMate.useCaseAdapter.severalParameter;

public class SeveralParameterUseCase {

    public SeveralParameterUseCaseResponse useCaseMethod(final String stringParameter, final Object objectParameter,
                                                         final int intParameter, final Boolean booleanParameter) {
        return new SeveralParameterUseCaseResponse(stringParameter, objectParameter, intParameter, booleanParameter);
    }
}
