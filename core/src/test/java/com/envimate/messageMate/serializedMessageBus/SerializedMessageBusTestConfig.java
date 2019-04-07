package com.envimate.messageMate.serializedMessageBus;

import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.messageBus.MessageBusType;
import lombok.RequiredArgsConstructor;

import static com.envimate.messageMate.messageBus.MessageBusBuilder.aMessageBus;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class SerializedMessageBusTestConfig {
    private final MessageBus messageBus;

    public static SerializedMessageBusTestConfig synchronousMessageBusTestConfig() {
        final MessageBus messageBus = aMessageBus().forType(MessageBusType.SYNCHRONOUS)
                .build();
        return new SerializedMessageBusTestConfig(messageBus);
    }

    public MessageBus getMessageBus() {
        return messageBus;
    }
}
