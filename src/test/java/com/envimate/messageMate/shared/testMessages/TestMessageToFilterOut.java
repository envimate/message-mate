package com.envimate.messageMate.shared.testMessages;


import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class TestMessageToFilterOut implements TestMessage {

    public static TestMessageToFilterOut testMessageToFilterOut() {
        return new TestMessageToFilterOut();
    }
}
