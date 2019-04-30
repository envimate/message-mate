package com.envimate.messageMate.shared.pipeChannelMessageBus.testActions;

import com.envimate.messageMate.processingContext.EventType;
import com.envimate.messageMate.shared.testMessages.TestMessage;

public interface SendingActions {
    void send(EventType eventType, TestMessage message);
}
