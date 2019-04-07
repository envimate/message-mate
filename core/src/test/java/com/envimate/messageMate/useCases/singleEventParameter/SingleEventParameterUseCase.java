package com.envimate.messageMate.useCases.singleEventParameter;

public class SingleEventParameterUseCase {

    public String useCaseMethod(final SingleParameterEvent request) {
        return request.getMessage();
    }
}
