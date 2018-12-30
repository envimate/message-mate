package com.envimate.messageMate.messageFunction.givenWhenThen;

import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class Given {
    public static When given(final TestMessageFunctionBuilder testMessageFunctionBuilder) {
        return new When(testMessageFunctionBuilder);
    }
}
