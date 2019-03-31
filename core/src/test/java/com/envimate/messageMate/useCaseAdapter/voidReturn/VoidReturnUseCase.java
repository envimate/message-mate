package com.envimate.messageMate.useCaseAdapter.voidReturn;

public class VoidReturnUseCase {

    public void useCaseMethod(final CallbackTestRequest callbackTestRequest) {
        callbackTestRequest.invokeCallback();
    }
}
