package com.envimate.messageMate.serializedMessageBus.asynchronous;

import com.envimate.messageMate.serializedMessageBus.SerializedMessageBusTestConfig;
import com.envimate.messageMate.shared.config.AbstractTestConfigProvider;

import static com.envimate.messageMate.serializedMessageBus.SerializedMessageBusTestConfig.asynchronousMessageBusTestConfig;
import static com.envimate.messageMate.serializedMessageBus.SerializedMessageBusTestConfig.synchronousMessageBusTestConfig;

public class AsynchronousSerializedMessageBusConfigurationResolver extends AbstractTestConfigProvider {

    @Override
    protected Class<?> forConfigClass() {
        return SerializedMessageBusTestConfig.class;
    }

    @Override
    protected Object testConfig() {
        return asynchronousMessageBusTestConfig();
    }
}
