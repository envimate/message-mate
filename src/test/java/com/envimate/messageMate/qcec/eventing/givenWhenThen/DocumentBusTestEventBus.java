package com.envimate.messageMate.qcec.eventing.givenWhenThen;

import com.envimate.messageMate.qcec.domainBus.DocumentBus;
import com.envimate.messageMate.qcec.domainBus.DocumentBusBuilder;
import com.envimate.messageMate.subscribing.SubscriptionId;
import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class DocumentBusTestEventBus extends TestEventBus {
    private final DocumentBus documentBus = DocumentBusBuilder.aDefaultDocumentBus();

    public static DocumentBusTestEventBus documentTestEventBus() {
        return new DocumentBusTestEventBus();
    }

    @Override
    public void publish(final Object event) {
        documentBus.publish(event);
    }

    @Override
    public <T> SubscriptionId reactTo(final Class<T> tClass, final Consumer<T> consumer) {
        return documentBus.reactTo(tClass)
                .using(consumer);
    }

    @Override
    public void unsubscribe(final SubscriptionId subscriptionId) {
        documentBus.unsubscribe(subscriptionId);
    }
}
