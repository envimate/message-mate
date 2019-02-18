package com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen;


import com.envimate.messageMate.qcec.shared.TestValidation;


public interface TestValidationBuilder<T> {

    TestValidation build();

    TestValidationBuilder<T> and(final TestValidationBuilder<T> validationBuilder);

}
