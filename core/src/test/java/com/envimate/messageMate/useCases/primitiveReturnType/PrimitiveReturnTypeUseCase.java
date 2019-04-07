package com.envimate.messageMate.useCases.primitiveReturnType;

public class PrimitiveReturnTypeUseCase {

    public int useCaseMethod(final PrimitiveReturnTypeRequest input) {
        return input.getValue();
    }
}
