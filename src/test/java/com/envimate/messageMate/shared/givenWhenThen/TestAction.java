package com.envimate.messageMate.shared.givenWhenThen;

import com.envimate.messageMate.shared.context.TestExecutionContext;

public interface TestAction<T> {

    void execute(T t, TestExecutionContext executionContext);
}
