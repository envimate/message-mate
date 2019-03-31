package com.envimate.messageMate.useCaseAdapter.singleEventParameter;

public class SingleEventParameterUseCase {

    public String useCaseMethod(final SingleParameterEvent request) {
        return request.getMessage();
    }
}
