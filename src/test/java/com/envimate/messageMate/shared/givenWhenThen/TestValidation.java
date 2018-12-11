package com.envimate.messageMate.shared.givenWhenThen;

import com.envimate.messageMate.shared.context.TestExecutionContext;

public interface TestValidation<T> {

    void validate(T t, TestExecutionContext executionContext);
}
