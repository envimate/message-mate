package com.envimate.messageMate.shared.testMessages;


import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class TestMessageOfInterest implements TestMessage {
    public static final String CONTENT = "TestContent";
    public String content;

    protected TestMessageOfInterest(final String content) {
        this.content = content;
    }

    public static TestMessageOfInterest messageOfInterest() {
        return new TestMessageOfInterest(CONTENT);
    }
}
