package com.envimate.messageMate.shared.channelMessageBus.givenWhenThen;


import com.envimate.messageMate.qcec.shared.TestValidation;


public interface TestValidationBuilder<T> {

    TestValidation build();

    TestValidationBuilder<T> and(final TestValidationBuilder<T> validationBuilder);

}
