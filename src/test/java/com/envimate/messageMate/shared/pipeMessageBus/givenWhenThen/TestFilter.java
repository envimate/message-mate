package com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen;

import com.envimate.messageMate.channel.ChannelProcessingFrame;
import com.envimate.messageMate.channel.ProcessingContext;
import com.envimate.messageMate.filtering.Filter;
import com.envimate.messageMate.shared.testMessages.InvalidTestMessage;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import com.envimate.messageMate.shared.testMessages.TestMessageOfInterest;

//TODO: cleanup
public final class TestFilter {
    public static final String CHANGED_CONTENT = "CHANGED";

    public static <T> Filter<T> aContentChangingFilter_old() {
        return (message, receivers, filterActions) -> {
            final TestMessageOfInterest testMessageOfInterest = (TestMessageOfInterest) message;
            testMessageOfInterest.content = CHANGED_CONTENT;
            filterActions.pass(message);
        };
    }

    public static <T> Filter<T> aContentAppendingFilter_old(final String contentToAppend) {
        return (message, receivers, filterActions) -> {
            final TestMessageOfInterest testMessageOfInterest = (TestMessageOfInterest) message;
            testMessageOfInterest.content += contentToAppend;
            filterActions.pass(message);
        };
    }

    public static <T> Filter<T> aMessageDroppingFilter_old() {
        return (message, receivers, filterActions) -> {
            if (message instanceof TestMessageOfInterest) {
                filterActions.pass(message);
            } else {
                filterActions.block(message);
            }
        };
    }

    public static <T> Filter<T> aMessageDroppingFilter() {
        return (message, receivers, filterActions) -> {
            filterActions.block(message);
        };
    }

    @SuppressWarnings("unchecked")
    public static <T> Filter<T> aMessageReplacingFilter_old() {
        return (message, receivers, filterActions) -> {
            if (message instanceof InvalidTestMessage) {
                final TestMessageOfInterest messageOfInterest = TestMessageOfInterest.messageOfInterest();
                filterActions.replace((T) messageOfInterest);
            } else {
                filterActions.pass(message);
            }
        };
    }

    @SuppressWarnings("unchecked")
    public static Filter<ProcessingContext<TestMessage>> aMessageReplacingFilter(final ProcessingContext<TestMessage> replacement) {
        return (processingContext, receivers, filterActions) -> {
            final ChannelProcessingFrame<TestMessage> currentProcessingFrame = processingContext.getCurrentProcessingFrame();
            replacement.setCurrentProcessingFrame(currentProcessingFrame);
            filterActions.replace(replacement);
        };
    }

    public static <T> Filter<T> aMessageFilterThatDoesNotCallAnyMethod() {
        return (message, receivers, filterActions) -> {

        };
    }

}
