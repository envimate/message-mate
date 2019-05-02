package com.envimate.messageMate.shared.pipeChannelMessageBus.testActions;

import com.envimate.messageMate.subscribing.Subscriber;

import java.util.List;

public interface SubscriberQueryActions {
    List<Subscriber<?>> getAllSubscribers();
}
