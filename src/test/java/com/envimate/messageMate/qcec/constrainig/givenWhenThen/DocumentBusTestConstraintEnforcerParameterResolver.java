package com.envimate.messageMate.qcec.constrainig.givenWhenThen;


import com.envimate.messageMate.shared.config.AbstractTestConfigProvider;

import static com.envimate.messageMate.qcec.constrainig.givenWhenThen.DocumentBusTestConstraintEnforcer.documentBusTestConstraintEnforcer;

public class DocumentBusTestConstraintEnforcerParameterResolver extends AbstractTestConfigProvider {
    @Override
    protected Class<?> forConfigClass() {
        return TestConstraintEnforcer.class;
    }

    @Override
    protected Object testConfig() {
        return documentBusTestConstraintEnforcer();
    }
}
