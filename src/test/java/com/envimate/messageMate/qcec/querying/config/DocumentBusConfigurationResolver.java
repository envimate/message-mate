package com.envimate.messageMate.qcec.querying.config;


import com.envimate.messageMate.shared.config.AbstractTestConfigProvider;

import static com.envimate.messageMate.qcec.querying.config.DocumentBusTestQueryResolver.documentBusTestQueryResolver;

public class DocumentBusConfigurationResolver extends AbstractTestConfigProvider {

    @Override
    protected Class<?> forConfigClass() {
        return TestQueryResolver.class;
    }

    @Override
    protected Object testConfig() {
        return documentBusTestQueryResolver();
    }
}
