package com.envimate.messageMate.messageFunction;

public class Given {
    public static When given(TestMessageFunctionBuilder testMessageFunctionBuilder) {
        return new When(testMessageFunctionBuilder);
    }
}
