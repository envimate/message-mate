package com.envimate.messageMate.useCases.exceptionThrowing;

public class ExceptionThrowingUseCase {

    public void useCaseMethod(final ExceptionThrowingRequest request) {
        throw request.getExceptionToThrow();
    }
}
