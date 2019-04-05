package com.envimate.messageMate.useCaseAdapter.usecaseInvoking;

import com.envimate.messageMate.useCaseAdapter.mapping.RequestDeserializer;

@FunctionalInterface
public interface Caller<USECASE> {
    Object call(USECASE useCase, Object event, RequestDeserializer requestDeserializer);
}
