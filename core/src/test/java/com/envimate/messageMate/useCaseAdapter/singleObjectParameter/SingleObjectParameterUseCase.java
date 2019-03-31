package com.envimate.messageMate.useCaseAdapter.singleObjectParameter;

public class SingleObjectParameterUseCase {

    public String useCaseMethod(final TestUseCaseRequest request){
        System.out.println("request = " + request);
        return request.getMessage();
    }
}
