package com.envimate.messageMate.shared.givenWhenThen;


import com.envimate.messageMate.shared.context.TestExecutionContext;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Setup<T> {

    public final T t;
    public final TestExecutionContext testExecutionContext;
    public final List<SetupAction<T>> setupActions;

    public static <T> Setup<T> setup(final T t,
                                     final TestExecutionContext testExecutionContext,
                                     final List<SetupAction<T>> setupActions) {
        return new Setup<>(t, testExecutionContext, setupActions);
    }
}
