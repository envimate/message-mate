package com.envimate.messageMate.shared.pipeChannelMessageBus.testActions;

import com.envimate.messageMate.identification.MessageId;
import com.envimate.messageMate.processingContext.EventType;
import com.envimate.messageMate.processingContext.ProcessingContext;
import com.envimate.messageMate.shared.testMessages.TestMessage;

public interface ProcessingContextSendingActions {
    MessageId send(ProcessingContext<TestMessage> processingContext);
}
