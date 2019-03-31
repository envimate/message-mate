package com.envimate.messageMate.useCaseAdapter.exceptionThrowing;

public class ExceptionThrowingUseCase {

    public void useCaseMethod(final ExceptionThrowingRequest request) throws Exception {
        throw request.getExceptionToThrow();
    }
}
