package com.envimate.messageMate.qcec.querying.givenWhenThen;


import com.envimate.messageMate.shared.config.AbstractTestConfigProvider;

import static com.envimate.messageMate.qcec.querying.givenWhenThen.QueryResolverTestQueryResolver.queryResolverTestQueryResolver;

public class QueryResolverConfigurationResolver extends AbstractTestConfigProvider {

    @Override
    protected Class<?> forConfigClass() {
        return TestQueryResolver.class;
    }

    @Override
    protected Object testConfig() {
        return queryResolverTestQueryResolver();
    }
}
