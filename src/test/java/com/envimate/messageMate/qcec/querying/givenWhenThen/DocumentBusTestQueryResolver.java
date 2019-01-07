package com.envimate.messageMate.qcec.querying.givenWhenThen;

import com.envimate.messageMate.qcec.domainBus.DocumentBus;
import com.envimate.messageMate.qcec.domainBus.DocumentBusBuilder;
import com.envimate.messageMate.qcec.queryresolving.Query;
import com.envimate.messageMate.subscribing.SubscriptionId;

import java.util.Optional;
import java.util.function.Consumer;

public final class DocumentBusTestQueryResolver extends TestQueryResolver {

    private final DocumentBus documentBus;

    private DocumentBusTestQueryResolver() {
        documentBus = DocumentBusBuilder.aDefaultDocumentBus();
    }

    public static DocumentBusTestQueryResolver documentBusTestQueryResolver() {
        return new DocumentBusTestQueryResolver();
    }

    @Override
    public <R> Optional<R> executeQuery(final Query<R> query) {
        return documentBus.query(query);
    }

    @Override
    public <R> R executeRequiredQuery(final Query<R> query) {
        return documentBus.queryRequired(query);
    }

    @Override
    public <T extends Query<?>> SubscriptionId subscribing(final Class<T> queryClass, final Consumer<T> consumer) {
        return documentBus.answer(queryClass)
                .using(consumer);
    }

    @Override
    public <T extends Query<?>> TestQueryResolver withASubscriber(final Class<T> queryClass, final Consumer<T> consumer) {
        subscribing(queryClass, consumer);
        return this;
    }

    @Override
    public void unsubscribe(final SubscriptionId subscriptionId) {
        documentBus.unsubscribe(subscriptionId);
    }
}
