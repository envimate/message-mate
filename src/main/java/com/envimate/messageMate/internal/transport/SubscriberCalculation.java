package com.envimate.messageMate.internal.transport;

import com.envimate.messageMate.subscribing.Subscriber;

import java.util.List;
import java.util.function.Function;

public interface SubscriberCalculation<T> extends Function<T, List<Subscriber<T>>> {
}
