package com.envimate.messageMate.messageBus.config;

import com.envimate.messageMate.shared.config.AbstractTestConfigProvider;

import static com.envimate.messageMate.messageBus.config.MessageBusTestConfig.aSynchronousMessageBus;

public class SynchronisedMessageBusConfigurationResolver extends AbstractTestConfigProvider {

    @Override
    protected Class<?> forConfigClass() {
        return MessageBusTestConfig.class;
    }

    @Override
    protected Object testConfig() {
        return aSynchronousMessageBus();
    }

}
