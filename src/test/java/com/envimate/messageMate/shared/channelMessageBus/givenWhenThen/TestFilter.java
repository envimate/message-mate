package com.envimate.messageMate.shared.channelMessageBus.givenWhenThen;

import com.envimate.messageMate.filtering.Filter;
import com.envimate.messageMate.shared.testMessages.InvalidTestMessage;
import com.envimate.messageMate.shared.testMessages.TestMessageOfInterest;

public final class TestFilter {
    public static final String CHANGED_CONTENT = "CHANGED";

    public static <T> Filter<T> aContentChangingFilter() {
        return (message, receivers, filterActions) -> {
            final TestMessageOfInterest testMessageOfInterest = (TestMessageOfInterest) message;
            testMessageOfInterest.content = CHANGED_CONTENT;
            filterActions.pass(message);
        };
    }

    public static <T> Filter<T> aContentAppendingFilter(final String contentToAppend) {
        return (message, receivers, filterActions) -> {
            final TestMessageOfInterest testMessageOfInterest = (TestMessageOfInterest) message;
            testMessageOfInterest.content += contentToAppend;
            filterActions.pass(message);
        };
    }

    public static <T> Filter<T> aMessageDroppingFilter() {
        return (message, receivers, filterActions) -> {
            if (message instanceof TestMessageOfInterest) {
                filterActions.pass(message);
            } else {
                filterActions.block(message);
            }
        };
    }

    @SuppressWarnings("unchecked")
    public static <T> Filter<T> aMessageReplacingFilter() {
        return (message, receivers, filterActions) -> {
            if (message instanceof InvalidTestMessage) {
                final TestMessageOfInterest messageOfInterest = TestMessageOfInterest.messageOfInterest();
                filterActions.replace((T) messageOfInterest);
            } else {
                filterActions.pass(message);
            }
        };
    }

    public static <T> Filter<T> aMessageFilterThatDoesNotCallAnyMethod() {
        return (message, receivers, filterActions) -> {

        };
    }

}
