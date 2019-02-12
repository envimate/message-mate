package com.envimate.messageMate.shared.channelMessageBus.givenWhenThen;


import com.envimate.messageMate.qcec.shared.TestEnvironment;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = PRIVATE)
public final class Setup<T> {
    public final T t;
    public final TestEnvironment testEnvironment;
    public final List<SetupAction<T>> setupActions;

    public static <T> Setup<T> setup(final T t,
                                     final TestEnvironment testEnvironment,
                                     final List<SetupAction<T>> setupActions) {
        return new Setup<>(t, testEnvironment, setupActions);
    }
}
