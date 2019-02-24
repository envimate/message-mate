package com.envimate.messageMate.shared.testMessages;


import lombok.ToString;

@ToString
public final class SubClassingTestMessageOfInterest extends TestMessageOfInterest {

    private SubClassingTestMessageOfInterest(final String content) {
        super(content);
    }

    public static SubClassingTestMessageOfInterest subClassingTestMessageOfInterest() {
        return new SubClassingTestMessageOfInterest(TestMessageOfInterest.CONTENT);
    }
}
