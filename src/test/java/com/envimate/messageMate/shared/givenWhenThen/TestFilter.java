package com.envimate.messageMate.shared.givenWhenThen;

import com.envimate.messageMate.filtering.Filter;
import com.envimate.messageMate.filtering.FilterActions;
import com.envimate.messageMate.shared.testMessages.InvalidTestMessage;
import com.envimate.messageMate.shared.testMessages.TestMessageOfInterest;
import com.envimate.messageMate.subscribing.Subscriber;

import java.util.List;

public class TestFilter {
    public static final String CHANGED_CONTENT = "CHANGED";

    public static <T> Filter<T> aContentChangingFilter(Class<T> messageClass) {
        return new Filter<T>() {
            @Override
            public void apply(T message, List<Subscriber<T>> receivers, FilterActions<T> filterActions) {
                final TestMessageOfInterest testMessageOfInterest = (TestMessageOfInterest) message;
                testMessageOfInterest.content = CHANGED_CONTENT;
                filterActions.pass(message);
            }
        };
    }

    public static <T> Filter<T> aMessageDroppingFilter(Class<T> messageClass) {
        return new Filter<T>() {
            @Override
            public void apply(T message, List<Subscriber<T>> receivers, FilterActions<T> filterActions) {
                if (message instanceof TestMessageOfInterest) {
                    filterActions.pass(message);
                } else {
                    filterActions.block(message);
                }
            }
        };
    }

    public static <T> Filter<T> aMessageReplacingFilter(Class<T> messageClass) {
        return new Filter<T>() {
            @Override
            public void apply(T message, List<Subscriber<T>> receivers, FilterActions<T> filterActions) {
                if (message instanceof InvalidTestMessage) {
                    final TestMessageOfInterest messageOfInterest = TestMessageOfInterest.messageOfInterest();
                    filterActions.replace((T) messageOfInterest);
                } else {
                    filterActions.pass(message);
                }
            }
        };
    }

    public static <T> Filter<T> aMessageThatDoesNotCallAnyMethod(Class<T> messageClass) {
        return new Filter<T>() {
            @Override
            public void apply(T message, List<Subscriber<T>> receivers, FilterActions<T> filterActions) {

            }
        };
    }
}
