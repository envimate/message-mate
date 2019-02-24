package com.envimate.messageMate.qcec.querying.config;


import com.envimate.messageMate.qcec.queryresolving.Query;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.subscribing.SubscriptionId;

import java.util.Optional;
import java.util.function.Consumer;

import static com.envimate.messageMate.qcec.shared.TestEnvironment.emptyTestEnvironment;


public abstract class TestQueryResolver {
    private final TestEnvironment testEnvironment = emptyTestEnvironment();

    public abstract <R> Optional<R> executeQuery(Query<R> query);

    public abstract <R> R executeRequiredQuery(Query<R> query);

    public abstract <T extends Query<?>> SubscriptionId subscribing(final Class<T> queryClass, Consumer<T> consumer);

    public abstract <T extends Query<?>> TestQueryResolver withASubscriber(final Class<T> queryClass, Consumer<T> consumer);

    public abstract void unsubscribe(SubscriptionId subscriptionId);

    public TestEnvironment getEnvironment() {
        return testEnvironment;
    }
}
