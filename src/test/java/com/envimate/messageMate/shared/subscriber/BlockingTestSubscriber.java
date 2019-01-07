package com.envimate.messageMate.shared.subscriber;

import com.envimate.messageMate.subscribing.AcceptingBehavior;
import com.envimate.messageMate.subscribing.SubscriptionId;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;

import static com.envimate.messageMate.subscribing.AcceptingBehavior.MESSAGE_ACCEPTED;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class BlockingTestSubscriber<T> implements TestSubscriber<T> {
    private final SubscriptionId subscriptionId = SubscriptionId.newUniqueId();
    private final Semaphore semaphoreToWaitUntilExecutionIsDone;
    private final List<T> receivedMessages = new CopyOnWriteArrayList<>();

    public static <T> BlockingTestSubscriber<T> blockingTestSubscriber(final Semaphore semaphore) {
        return new BlockingTestSubscriber<>(semaphore);
    }


    @Override
    public AcceptingBehavior accept(final T message) {
        try {
            semaphoreToWaitUntilExecutionIsDone.acquire();
            receivedMessages.add(message);
        } catch (final InterruptedException ignored) {
            receivedMessages.add(message);
        }
        return MESSAGE_ACCEPTED;
    }

    @Override
    public SubscriptionId getSubscriptionId() {
        return subscriptionId;
    }

    public List<T> getReceivedMessages() {
        return receivedMessages;
    }
}
