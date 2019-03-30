package com.envimate.messageMate.useCaseAdapter.useCases.noReturnValue;

public class NoReturnValueUseCase {

    public void useCaseMethod(final CallbackTestRequest callbackTestRequest) {
        callbackTestRequest.invokeCallback();
    }
}
