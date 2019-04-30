package com.envimate.messageMate.shared.pipeChannelMessageBus.testActions;

import com.envimate.messageMate.processingContext.EventType;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import com.envimate.messageMate.subscribing.Subscriber;

public interface SubscribeActions {
    void subscribe(EventType eventType, Subscriber<TestMessage> subscriber);
}
