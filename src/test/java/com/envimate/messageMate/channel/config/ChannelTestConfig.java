package com.envimate.messageMate.channel.config;

import com.envimate.messageMate.channel.ChannelType;
import com.envimate.messageMate.pipe.configuration.AsynchronousConfiguration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static com.envimate.messageMate.channel.ChannelType.ASYNCHRONOUS;
import static com.envimate.messageMate.channel.ChannelType.SYNCHRONOUS;
import static com.envimate.messageMate.pipe.configuration.AsynchronousConfiguration.constantPoolSizeAsynchronousPipeConfiguration;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class ChannelTestConfig {
    public static final int ASYNCHRONOUS_CHANNEL_CONFIG_POOL_SIZE = 5;
    @Getter
    private final ChannelType type;
    @Getter
    private final AsynchronousConfiguration asynchronousConfiguration;
    @Getter
    private final long millisecondsSleepBetweenExecutionActionSteps;
    @Getter
    private final long millisecondsSleepAfterExecution;


    public static ChannelTestConfig synchronousChannelTestConfig() {
        return new ChannelTestConfig(SYNCHRONOUS, null, 0, 0);
    }

    public static ChannelTestConfig asynchronousChannelTestConfig() {
        final AsynchronousConfiguration asynchronousConfiguration = constantPoolSizeAsynchronousPipeConfiguration(ASYNCHRONOUS_CHANNEL_CONFIG_POOL_SIZE);
        return new ChannelTestConfig(ASYNCHRONOUS, asynchronousConfiguration, 5, 20);
    }
}
