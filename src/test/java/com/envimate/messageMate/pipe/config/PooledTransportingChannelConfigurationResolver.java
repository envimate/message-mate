package com.envimate.messageMate.pipe.config;

import com.envimate.messageMate.shared.config.AbstractTestConfigProvider;

import static com.envimate.messageMate.pipe.config.ChannelTestConfig.aConfigurationForAtomicAcceptingStrategyPooledTransportAndSynchronousDelivery;

public class PooledTransportingChannelConfigurationResolver extends AbstractTestConfigProvider {

    @Override
    protected Class<?> forConfigClass() {
        return ChannelTestConfig.class;
    }

    @Override
    protected Object testConfig() {
        return aConfigurationForAtomicAcceptingStrategyPooledTransportAndSynchronousDelivery();
    }
}
