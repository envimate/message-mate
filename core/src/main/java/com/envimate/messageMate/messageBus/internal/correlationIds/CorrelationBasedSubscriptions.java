package com.envimate.messageMate.messageBus.internal.correlationIds;

import com.envimate.messageMate.channel.ProcessingContext;
import com.envimate.messageMate.identification.CorrelationId;
import com.envimate.messageMate.subscribing.Subscriber;
import com.envimate.messageMate.subscribing.SubscriptionId;

import java.util.List;

public interface CorrelationBasedSubscriptions {

    SubscriptionId addCorrelationBasedSubscriber(CorrelationId correlationId, Subscriber<ProcessingContext<Object>> subscriber);

    void unsubscribe(SubscriptionId subscriptionId);

    List<Subscriber<ProcessingContext<Object>>> getSubscribersFor(CorrelationId correlationId);
}
