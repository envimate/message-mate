package com.envimate.messageMate.qcec.eventing.givenWhenThen;


import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.qcec.eventBus.EventBus;
import com.envimate.messageMate.subscribing.SubscriptionId;

import java.util.function.Consumer;

import static com.envimate.messageMate.messageBus.MessageBusBuilder.aMessageBus;
import static com.envimate.messageMate.qcec.eventBus.EventBusFactory.aEventBus;


public final class EventBusTestEventBus extends TestEventBus {
    private final EventBus eventBus;

    private EventBusTestEventBus() {
        final MessageBus messageBus = aMessageBus()
                .build();
        this.eventBus = aEventBus(messageBus);
    }

    public static EventBusTestEventBus eventBusTestEventBus() {
        return new EventBusTestEventBus();
    }

    @Override
    public void publish(final Object event) {
        eventBus.publish(event);
    }

    @Override
    public <T> SubscriptionId reactTo(final Class<T> tClass, final Consumer<T> consumer) {
        return eventBus.reactTo(tClass, consumer);
    }

    @Override
    public void unsubscribe(final SubscriptionId subscriptionId) {
        eventBus.unsubscribe(subscriptionId);
    }
}
