package com.envimate.messageMate.qcec.eventing.givenWhenThen;

import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.subscribing.SubscriptionId;
import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;

import static com.envimate.messageMate.qcec.shared.TestEnvironment.emptyTestEnvironment;
import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
public abstract class TestEventBus {

    private final TestEnvironment testEnvironment = emptyTestEnvironment();

    public TestEnvironment getTestEnvironment() {
        return testEnvironment;
    }

    public abstract void publish(Object event);

    public abstract <T> SubscriptionId reactTo(Class<T> tClass, Consumer<T> consumer);

    public abstract void unsubscribe(SubscriptionId subscriptionId);
}
