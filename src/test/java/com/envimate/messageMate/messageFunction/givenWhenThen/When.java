package com.envimate.messageMate.messageFunction.givenWhenThen;

import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
public class When {
    private final TestMessageFunctionBuilder testMessageFunctionBuilder;

    public Then when(TestMessageFunctionActionBuilder testMessageFunctionActionBuilder) {
        return new Then(testMessageFunctionBuilder, testMessageFunctionActionBuilder);
    }
}
