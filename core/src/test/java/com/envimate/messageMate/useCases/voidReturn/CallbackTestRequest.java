package com.envimate.messageMate.useCases.voidReturn;

import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class CallbackTestRequest {
    private final Consumer<Object> callback;

    public static CallbackTestRequest callbackTestRequest(final Consumer<Object> callback) {
        return new CallbackTestRequest(callback);
    }

    public void invokeCallback() {
        if (callback != null) {
            callback.accept(this);
        }
    }
}
