package com.envimate.messageMate.channel.givenWhenThen;

import com.envimate.messageMate.channel.ProcessingContext;
import com.envimate.messageMate.channel.error.ChannelExceptionHandler;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.shared.subscriber.TestException;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import lombok.RequiredArgsConstructor;

import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.EXCEPTION;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.RESULT;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class TestChannelErrorHandler {

    public static ChannelExceptionHandler<TestMessage> ignoringChannelExceptionHandler() {
        return new ChannelExceptionHandler<>() {
            @Override
            public boolean shouldSubscriberErrorBeHandledAndDeliveryAborted(final ProcessingContext<TestMessage> message, final Exception e) {
                return true;
            }

            @Override
            public void handleSubscriberException(final ProcessingContext<TestMessage> message, final Exception e) {

            }

            @Override
            public void handleFilterException(final ProcessingContext<TestMessage> message, final Exception e) {

            }
        };
    }

    public static ChannelExceptionHandler<TestMessage> exceptionInResultStoringChannelExceptionHandler(final TestEnvironment testEnvironment) {
        return new ChannelExceptionHandler<>() {
            @Override
            public boolean shouldSubscriberErrorBeHandledAndDeliveryAborted(final ProcessingContext<TestMessage> message, final Exception e) {
                return true;
            }

            @Override
            public void handleSubscriberException(final ProcessingContext<TestMessage> message, final Exception e) {
                testEnvironment.setProperty(RESULT, e);
            }

            @Override
            public void handleFilterException(final ProcessingContext<TestMessage> message, final Exception e) {
                testEnvironment.setProperty(RESULT, e);
            }
        };
    }

    public static ChannelExceptionHandler<TestMessage> catchingChannelExceptionHandler(final TestEnvironment testEnvironment) {
        return new ChannelExceptionHandler<>() {
            @Override
            public boolean shouldSubscriberErrorBeHandledAndDeliveryAborted(final ProcessingContext<TestMessage> message, final Exception e) {
                return true;
            }

            @Override
            public void handleSubscriberException(final ProcessingContext<TestMessage> message, final Exception e) {
                testEnvironment.setProperty(EXCEPTION, e);
            }

            @Override
            public void handleFilterException(final ProcessingContext<TestMessage> message, final Exception e) {
                testEnvironment.setProperty(EXCEPTION, e);
            }
        };
    }

    public static ChannelExceptionHandler<TestMessage> testExceptionIgnoringChannelExceptionHandler(final TestEnvironment testEnvironment) {
        return new ChannelExceptionHandler<>() {
            @Override
            public boolean shouldSubscriberErrorBeHandledAndDeliveryAborted(final ProcessingContext<TestMessage> message, final Exception e) {
                return !(e instanceof TestException);
            }

            @Override
            public void handleSubscriberException(final ProcessingContext<TestMessage> message, final Exception e) {
                testEnvironment.setProperty(EXCEPTION, e);
            }

            @Override
            public void handleFilterException(final ProcessingContext<TestMessage> message, final Exception e) {
                testEnvironment.setProperty(EXCEPTION, e);
            }
        };
    }
}
