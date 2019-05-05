package com.envimate.messageMate.shared.pipeChannelMessageBus.testActions;

import java.util.concurrent.TimeUnit;

public interface CloseActions {

    void close(boolean finishRemainingTasks);

    boolean await(final int timeout, final TimeUnit timeUnit) throws InterruptedException;

    boolean isClosed();
}
