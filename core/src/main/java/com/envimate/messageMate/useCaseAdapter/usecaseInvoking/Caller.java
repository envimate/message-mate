package com.envimate.messageMate.useCaseAdapter.usecaseInvoking;

import com.envimate.messageMate.useCaseAdapter.methodInvoking.ParameterValueMappings;

@FunctionalInterface
public interface Caller<USECASE> {
    Object call(USECASE useCase, Object event, ParameterValueMappings parameterValueMappings);
}
