package com.envimate.messageMate.useCaseAdapter.methodInvoking;

public class RedundantParameterValueMappingException extends RuntimeException {

    public RedundantParameterValueMappingException(final Class<?> parameterClass) {
        super("Found redundant parameter value mapping for class " + parameterClass);
    }
}
