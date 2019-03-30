package com.envimate.messageMate.useCaseAdapter.useCases.singleObjectParameter;

public class SingleObjectParameterUseCase {

    public String useCaseMethod(final TestUseCaseRequest request){
        return request.getMessage();
    }
}
