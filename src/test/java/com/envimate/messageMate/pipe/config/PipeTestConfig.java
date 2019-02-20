package com.envimate.messageMate.pipe.config;

import com.envimate.messageMate.pipe.PipeType;
import com.envimate.messageMate.pipe.configuration.AsynchronousConfiguration;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import static com.envimate.messageMate.pipe.PipeType.ASYNCHRONOUS;
import static com.envimate.messageMate.pipe.PipeType.SYNCHRONOUS;
import static com.envimate.messageMate.pipe.configuration.AsynchronousConfiguration.constantPoolSizeAsynchronousPipeConfiguration;


@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class PipeTestConfig {
    public static final int ASYNCHRONOUS_POOL_SIZE = 5;
    public static final int ASYNCHRONOUS_QUEUED_BOUND = 3;
    public final PipeType pipeType;
    public final AsynchronousConfiguration asynchronousConfiguration;

    static PipeTestConfig aSynchronousPipe() {
        return new PipeTestConfig(SYNCHRONOUS, null);
    }

    static PipeTestConfig anAsynchronousPipe() {
        return new PipeTestConfig(ASYNCHRONOUS, constantPoolSizeAsynchronousPipeConfiguration(ASYNCHRONOUS_POOL_SIZE));
    }

    public static PipeTestConfig anAsynchronousBoundedPipe() {
        return new PipeTestConfig(ASYNCHRONOUS, constantPoolSizeAsynchronousPipeConfiguration(ASYNCHRONOUS_POOL_SIZE, ASYNCHRONOUS_QUEUED_BOUND));
    }
}
