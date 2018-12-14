package com.envimate.messageMate.qcec.eventing.givenWhenThen;


import com.envimate.messageMate.shared.config.AbstractTestConfigProvider;

import static com.envimate.messageMate.qcec.eventing.givenWhenThen.DocumentBusTestEventBus.documentTestEventBus;

public class DocumentBusTestEventBusParameterResolver extends AbstractTestConfigProvider {
    @Override
    protected Class forConfigClass() {
        return TestEventBus.class;
    }

    @Override
    protected Object testConfig() {
        return documentTestEventBus();
    }
}
