package com.envimate.messageMate.pipe.config;

import com.envimate.messageMate.shared.config.AbstractTestConfigProvider;

import static com.envimate.messageMate.pipe.config.PipeTestConfig.aSynchronousPipeWithQueuedAcceptingStrategy;

public class QueuingAcceptingStrategyPipeConfigurationResolver extends AbstractTestConfigProvider {

    @Override
    protected Class<?> forConfigClass() {
        return PipeTestConfig.class;
    }

    @Override
    protected Object testConfig() {
        return aSynchronousPipeWithQueuedAcceptingStrategy();
    }
}
