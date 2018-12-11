package com.envimate.messageMate.shared.testMessages;


import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public final class TestMessageOfInterest implements TestMessage {

    public String content;

    private TestMessageOfInterest(final String content) {
        this.content = content;
    }

    public static TestMessageOfInterest messageOfInterest() {
        final String testContent = "TestContent";
        return new TestMessageOfInterest(testContent);
    }
}
