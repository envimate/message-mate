package com.envimate.messageMate.useCases.severalParameter;

public class SeveralParameterUseCase {

    public SeveralParameterUseCaseResponse useCaseMethod(final SeveralParameterUseCaseRequest1 request1,
                                                         final SeveralParameterUseCaseRequest2 request2) {
        return new SeveralParameterUseCaseResponse(request1.intParameter, request1.booleanParameter, request2.objectParameter, request2.stringParameter);
    }
}
