package com.envimate.messageMate.useCaseAdapter.methodInvoking;

import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class ParameterValueMappings {
    private final Map<Class<?>, ParameterValueMapping> parameterValueMappingMap = new HashMap<>();

    public static ParameterValueMappings emptyParameterValueMappings() {
        return new ParameterValueMappings();
    }

    public Object getParameterValue(final Class<?> parameterClass, final Object event) {
        if (parameterValueMappingMap.containsKey(parameterClass)) {
            final ParameterValueMapping parameterValueMapping = parameterValueMappingMap.get(parameterClass);
            return parameterValueMapping.getParameter(event);
        } else {
            throw new MissingParameterValueMappingException(parameterClass);
        }
    }

    public void registerMapping(final Class<?> parameterClass, final ParameterValueMapping parameterValueMapping) {
        if (parameterValueMappingMap.containsKey(parameterClass)) {
            throw new RedundantParameterValueMappingException(parameterClass);
        } else {
            parameterValueMappingMap.put(parameterClass, parameterValueMapping);
        }
    }
}