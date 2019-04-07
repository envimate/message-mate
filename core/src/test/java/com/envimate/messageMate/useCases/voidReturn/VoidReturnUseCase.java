package com.envimate.messageMate.useCases.voidReturn;

public class VoidReturnUseCase {

    public void useCaseMethod(final CallbackTestRequest callbackTestRequest) {
        callbackTestRequest.invokeCallback();
    }
}
