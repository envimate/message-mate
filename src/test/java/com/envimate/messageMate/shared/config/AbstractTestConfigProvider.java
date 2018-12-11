package com.envimate.messageMate.shared.config;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

public abstract class AbstractTestConfigProvider implements ParameterResolver {

    @Override
    public boolean supportsParameter(final ParameterContext parameterContext, final ExtensionContext extensionContext) throws ParameterResolutionException {
        final Parameter parameter = parameterContext.getParameter();
        final Type type = parameter.getAnnotatedType().getType();
        final Class suitedConfigClass = forConfigClass();
        return type.equals(suitedConfigClass);
    }

    protected abstract Class forConfigClass();

    @Override
    public Object resolveParameter(final ParameterContext parameterContext, final ExtensionContext extensionContext) throws ParameterResolutionException {
        return testConfig();
    }

    protected abstract Object testConfig();
}
