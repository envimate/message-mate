package com.envimate.messageMate.serializedMessageBus.synchronous;

import com.envimate.messageMate.serializedMessageBus.givenWhenThen.SerializedMessageBusTestConfig;
import com.envimate.messageMate.shared.config.AbstractTestConfigProvider;

import static com.envimate.messageMate.serializedMessageBus.givenWhenThen.SerializedMessageBusTestConfig.synchronousMessageBusTestConfig;

public class SynchronousSerializedMessageBusConfigurationResolver extends AbstractTestConfigProvider {

    @Override
    protected Class<?> forConfigClass() {
        return SerializedMessageBusTestConfig.class;
    }

    @Override
    protected Object testConfig() {
        return synchronousMessageBusTestConfig();
    }
}
