package com.envimate.messageMate.useCaseAdapter.building;

import java.util.function.Function;

public interface UseCaseAdapterAddParameterValueExtractionBuilder<USECASE> {

    <PARAM> UseCaseAdapterStep3Builder<USECASE> mappingEventToParameter(Class<PARAM> paramClass, Function<Object, Object> mapping);
}
