package com.envimate.messageMate.qcec.constrainig.givenWhenThen;

import com.envimate.messageMate.qcec.domainBus.DocumentBus;
import com.envimate.messageMate.subscribing.SubscriptionId;
import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;

import static com.envimate.messageMate.qcec.domainBus.DocumentBusBuilder.aDefaultDocumentBus;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class DocumentBusTestConstraintEnforcer extends TestConstraintEnforcer {
    private final DocumentBus documentBus = aDefaultDocumentBus();

    public static DocumentBusTestConstraintEnforcer documentBusTestConstraintEnforcer() {
        return new DocumentBusTestConstraintEnforcer();
    }

    @Override
    public void enforce(Object constraint) {
        documentBus.enforce(constraint);
    }

    @Override
    public <T> TestConstraintEnforcer withASubscriber(Class<T> constraintClass, Consumer<T> consumer) {
        documentBus.ensure(constraintClass)
                .using(consumer);
        return this;
    }

    @Override
    public <T> SubscriptionId subscribing(Class<T> constraintClass, Consumer<T> consumer) {
        return documentBus.ensure(constraintClass)
                .using(consumer);
    }

    @Override
    public void unsubscribe(SubscriptionId subscriptionId) {
        documentBus.unsubscribe(subscriptionId);
    }
}
