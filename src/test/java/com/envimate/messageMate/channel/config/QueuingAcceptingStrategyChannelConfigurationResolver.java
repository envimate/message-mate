package com.envimate.messageMate.channel.config;

import com.envimate.messageMate.shared.config.AbstractTestConfigProvider;

import static com.envimate.messageMate.channel.config.ChannelTestConfig.aSynchronousChannelWithQueuedAcceptingStrategy;

public class QueuingAcceptingStrategyChannelConfigurationResolver extends AbstractTestConfigProvider {

    @Override
    protected Class<?> forConfigClass() {
        return ChannelTestConfig.class;
    }

    @Override
    protected Object testConfig() {
        return aSynchronousChannelWithQueuedAcceptingStrategy();
    }
}
