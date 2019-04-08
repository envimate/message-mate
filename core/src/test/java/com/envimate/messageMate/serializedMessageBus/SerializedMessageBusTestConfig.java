package com.envimate.messageMate.serializedMessageBus;

import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.messageBus.givenWhenThen.MessageBusTestExceptionHandler;
import lombok.RequiredArgsConstructor;

import static com.envimate.messageMate.internal.pipe.configuration.AsynchronousConfiguration.constantPoolSizeAsynchronousPipeConfiguration;
import static com.envimate.messageMate.messageBus.MessageBusBuilder.aMessageBus;
import static com.envimate.messageMate.messageBus.MessageBusType.ASYNCHRONOUS;
import static com.envimate.messageMate.messageBus.MessageBusType.SYNCHRONOUS;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class SerializedMessageBusTestConfig {
    private final MessageBus messageBus;

    public static SerializedMessageBusTestConfig synchronousMessageBusTestConfig() {
        final MessageBus messageBus = aMessageBus().forType(SYNCHRONOUS)
                .build();
        return new SerializedMessageBusTestConfig(messageBus);
    }

    public static SerializedMessageBusTestConfig asynchronousMessageBusTestConfig() {
        final MessageBus messageBus = aMessageBus().forType(ASYNCHRONOUS)
                .withAsynchronousConfiguration(constantPoolSizeAsynchronousPipeConfiguration(3))
                .withExceptionHandler(MessageBusTestExceptionHandler.allExceptionIgnoringExceptionHandler())
                .build();
        return new SerializedMessageBusTestConfig(messageBus);
    }

    public MessageBus getMessageBus() {
        return messageBus;
    }
}
