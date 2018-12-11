package com.envimate.messageMate.shared.testMessages;


import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class InvalidTestMessage implements TestMessage {

    public static InvalidTestMessage invalidTestMessage() {
        return new InvalidTestMessage();
    }
}
