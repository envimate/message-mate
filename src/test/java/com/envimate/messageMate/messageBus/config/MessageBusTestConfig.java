package com.envimate.messageMate.messageBus.config;

import com.envimate.messageMate.messageBus.MessageBusType;
import com.envimate.messageMate.pipe.configuration.AsynchronousConfiguration;
import lombok.*;

import static com.envimate.messageMate.messageBus.MessageBusType.ASYNCHRONOUS;
import static com.envimate.messageMate.messageBus.MessageBusType.SYNCHRONOUS;
import static com.envimate.messageMate.pipe.configuration.AsynchronousConfiguration.constantPoolSizeAsynchronousPipeConfiguration;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MessageBusTestConfig {
    public static final int ASYNCHRONOUS_DELIVERY_POOL_SIZE = 3;
    @Getter
    private final MessageBusType type;
    @Getter
    private final AsynchronousConfiguration asynchronousConfiguration;
    @Getter
    private final long millisecondsSleepBetweenExecutionActionSteps;
    @Getter
    private final long millisecondsSleepAfterExecution;

    static MessageBusTestConfig aSynchronousMessageBus() {
        return new MessageBusTestConfig(SYNCHRONOUS, null, 0, 0);
    }

    static MessageBusTestConfig anAsynchronousMessageBus() {
        final AsynchronousConfiguration asynchronousConfiguration = constantPoolSizeAsynchronousPipeConfiguration(ASYNCHRONOUS_DELIVERY_POOL_SIZE);
        return new MessageBusTestConfig(ASYNCHRONOUS, asynchronousConfiguration, 5, 10);
    }

}
