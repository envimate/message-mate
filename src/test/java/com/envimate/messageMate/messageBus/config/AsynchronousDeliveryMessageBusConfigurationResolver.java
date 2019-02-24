package com.envimate.messageMate.messageBus.config;

import com.envimate.messageMate.shared.config.AbstractTestConfigProvider;

import static com.envimate.messageMate.messageBus.config.MessageBusTestConfig.anAsynchronousMessageBus;

public class AsynchronousDeliveryMessageBusConfigurationResolver extends AbstractTestConfigProvider {

    @Override
    protected Class<?> forConfigClass() {
        return MessageBusTestConfig.class;
    }

    @Override
    protected Object testConfig() {
        return anAsynchronousMessageBus();
    }
}
