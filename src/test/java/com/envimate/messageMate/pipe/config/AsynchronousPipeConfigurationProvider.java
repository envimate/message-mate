package com.envimate.messageMate.pipe.config;

import com.envimate.messageMate.shared.config.AbstractTestConfigProvider;

import static com.envimate.messageMate.pipe.config.PipeTestConfig.anAsynchronousPipe;

public class AsynchronousPipeConfigurationProvider extends AbstractTestConfigProvider {

    @Override
    protected Class<?> forConfigClass() {
        return PipeTestConfig.class;
    }

    @Override
    protected Object testConfig() {
        return anAsynchronousPipe();
    }
}
