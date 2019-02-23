package com.envimate.messageMate.messageBus.givenWhenThen;


import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.SetupAction;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = PRIVATE)
public final class MessageBusSetup {
    public final MessageBus messageBus;
    public final TestEnvironment testEnvironment;
    public final List<SetupAction<MessageBus>> setupActions;

    public static MessageBusSetup setup(final MessageBus messageBus,
                                        final TestEnvironment testEnvironment,
                                        final List<SetupAction<MessageBus>> setupActions) {
        return new MessageBusSetup(messageBus, testEnvironment, setupActions);
    }
}
