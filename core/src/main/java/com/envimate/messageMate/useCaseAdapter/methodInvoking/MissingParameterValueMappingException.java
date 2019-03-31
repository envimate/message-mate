package com.envimate.messageMate.useCaseAdapter.methodInvoking;

public class MissingParameterValueMappingException extends RuntimeException {

    public MissingParameterValueMappingException(final Class<?> parameterClass) {
        super("No parameter value mapping known for class " + parameterClass);
    }
}
