package com.envimate.messageMate.shared.pipeChannelMessageBus.testActions;

import com.envimate.messageMate.internal.pipe.Pipe;
import com.envimate.messageMate.processingContext.EventType;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import com.envimate.messageMate.subscribing.Subscriber;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.TimeUnit;

import static lombok.AccessLevel.PRIVATE;

//TODO: rename and maybe move
@RequiredArgsConstructor(access = PRIVATE)
public final class PipeSutActions implements SendingAndReceivingActions {
    private final Pipe<TestMessage> pipe;

    public static PipeSutActions pipeSutActions(final Pipe<TestMessage> pipe) {
        return new PipeSutActions(pipe);
    }

    @Override
    public void close(final boolean finishRemainingTasks) {
        pipe.close(finishRemainingTasks);
    }

    @Override
    public boolean await(final int timeout, final TimeUnit timeUnit) throws InterruptedException {
        return pipe.awaitTermination(timeout, timeUnit);
    }

    @Override
    public void send(final EventType eventType, final TestMessage message) {
        pipe.send(message);
    }

    @Override
    public void subscribe(final EventType eventType, final Subscriber<TestMessage> subscriber) {
        pipe.subscribe(subscriber);
    }
}
