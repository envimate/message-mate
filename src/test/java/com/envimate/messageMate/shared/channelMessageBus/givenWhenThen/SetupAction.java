package com.envimate.messageMate.shared.channelMessageBus.givenWhenThen;

import com.envimate.messageMate.qcec.shared.TestEnvironment;

public interface SetupAction<T> {

    void execute(T t, TestEnvironment testEnvironment);
}
