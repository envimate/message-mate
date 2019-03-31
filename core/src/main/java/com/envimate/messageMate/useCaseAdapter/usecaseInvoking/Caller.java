package com.envimate.messageMate.useCaseAdapter.usecaseInvoking;

import com.envimate.messageMate.useCaseAdapter.methodInvoking.ParameterValueMappings;

import java.util.Optional;

@FunctionalInterface
public interface Caller<USECASE, EVENT> {
    Optional<?> call(USECASE useCase, EVENT event, ParameterValueMappings parameterValueMappings);
}
