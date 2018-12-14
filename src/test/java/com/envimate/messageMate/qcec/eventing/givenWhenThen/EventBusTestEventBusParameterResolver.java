package com.envimate.messageMate.qcec.eventing.givenWhenThen;


import com.envimate.messageMate.shared.config.AbstractTestConfigProvider;

import static com.envimate.messageMate.qcec.eventing.givenWhenThen.EventBusTestEventBus.eventBusTestEventBus;

public class EventBusTestEventBusParameterResolver extends AbstractTestConfigProvider {
    @Override
    protected Class forConfigClass() {
        return TestEventBus.class;
    }

    @Override
    protected Object testConfig() {
        return eventBusTestEventBus();
    }
}
