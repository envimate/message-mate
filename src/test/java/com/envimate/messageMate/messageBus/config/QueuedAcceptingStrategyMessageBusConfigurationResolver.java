package com.envimate.messageMate.messageBus.config;

import com.envimate.messageMate.shared.config.AbstractTestConfigProvider;

import static com.envimate.messageMate.messageBus.config.MessageBusTestConfig.aQueuingAcceptingSynchronousMessageBus;

public class QueuedAcceptingStrategyMessageBusConfigurationResolver extends AbstractTestConfigProvider {

    @Override
    protected Class<?> forConfigClass() {
        return MessageBusTestConfig.class;
    }

    @Override
    protected Object testConfig() {
        return aQueuingAcceptingSynchronousMessageBus();
    }
}