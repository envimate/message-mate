package com.envimate.messageMate.qcec.constrainig.givenWhenThen;


import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.qcec.constraintEnforcing.ConstraintEnforcer;
import com.envimate.messageMate.subscribing.SubscriptionId;

import java.util.function.Consumer;

import static com.envimate.messageMate.messageBus.MessageBusBuilder.aMessageBus;
import static com.envimate.messageMate.qcec.constraintEnforcing.ConstraintEnforcerFactory.aConstraintEnforcer;

public final class ConstraintEnforcingTestConstraintEnforcer extends TestConstraintEnforcer {
    private final ConstraintEnforcer constraintEnforcer;

    private ConstraintEnforcingTestConstraintEnforcer() {
        final MessageBus messageBus = aMessageBus()
                .withExceptionCatchingCondition(e -> false)
                .build();
        this.constraintEnforcer = aConstraintEnforcer(messageBus);
    }

    public static ConstraintEnforcingTestConstraintEnforcer constraintEnforcingTestConstraintEnforcer() {
        return new ConstraintEnforcingTestConstraintEnforcer();
    }

    @Override
    public void enforce(final Object constraint) {
        constraintEnforcer.enforce(constraint);
    }

    @Override
    public <T> TestConstraintEnforcer withASubscriber(final Class<T> constraintClass, final Consumer<T> consumer) {
        constraintEnforcer.respondTo(constraintClass, consumer);
        return this;
    }

    @Override
    public <T> SubscriptionId subscribing(final Class<T> constraintClass, final Consumer<T> consumer) {
        return constraintEnforcer.respondTo(constraintClass, consumer);
    }

    @Override
    public void unsubscribe(final SubscriptionId subscriptionId) {
        constraintEnforcer.unsubscribe(subscriptionId);
    }
}
