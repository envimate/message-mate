package com.envimate.messageMate.shared.subscriber;

import com.envimate.messageMate.subscribing.AcceptingBehavior;
import com.envimate.messageMate.subscribing.SubscriptionId;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ErrorThrowingTestSubscriber<T> implements TestSubscriber<T> {
    private final SubscriptionId subscriptionId = SubscriptionId.newUniqueId();

    public static <T> ErrorThrowingTestSubscriber<T> errorThrowingTestSubscriber() {
        return new ErrorThrowingTestSubscriber<>();
    }

    @Override
    public AcceptingBehavior accept(final T message) {
        throw new TestException();
    }

    @Override
    public SubscriptionId getSubscriptionId() {
        return subscriptionId;
    }

    public List<T> getReceivedMessages() {
        return Collections.emptyList();
    }

}
