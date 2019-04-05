package com.envimate.messageMate.useCaseAdapter.primitiveReturnType;

public class PrimitiveReturnTypeUseCase {

    public int useCaseMethod(final PrimitiveReturnTypeRequest input) {
        return input.getValue();
    }
}
