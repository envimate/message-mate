package com.envimate.messageMate.useCaseAdapter.exceptionThrowing;

public class ExceptionThrowingUseCase {

    public void useCaseMethod(final ExceptionThrowingRequest request) {
        throw request.getExceptionToThrow();
    }
}
