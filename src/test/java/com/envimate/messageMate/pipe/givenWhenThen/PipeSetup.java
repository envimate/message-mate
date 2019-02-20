package com.envimate.messageMate.pipe.givenWhenThen;

import com.envimate.messageMate.pipe.Pipe;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.SetupAction;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = PRIVATE)
public final class PipeSetup {
    public final Pipe<TestMessage> sut;
    public final TestEnvironment testEnvironment;
    public final List<SetupAction<Pipe<TestMessage>>> setupActions;

    public static PipeSetup setup(final Pipe<TestMessage> sut,
                                  final TestEnvironment testEnvironment,
                                  final List<SetupAction<Pipe<TestMessage>>> setupActions) {
        return new PipeSetup(sut, testEnvironment, setupActions);
    }
}
