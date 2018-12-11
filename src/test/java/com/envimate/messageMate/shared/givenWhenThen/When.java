package com.envimate.messageMate.shared.givenWhenThen;

import com.envimate.messageMate.channel.Channel;
import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.shared.context.TestExecutionContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static com.envimate.messageMate.shared.context.TestExecutionProperty.*;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class When<T> {
    private final SetupBuilder<T> setupBuilder;


    public Then<T> when(final ActionBuilder<T> actionBuilder) throws InterruptedException {
        final Setup<T> setup = setupBuilder.build();
        final TestExecutionContext testExecutionContext = setup.testExecutionContext;
        final T t = setup.t;
        setup.setupActions.forEach(setupAction -> setupAction.execute(t, testExecutionContext));
        final ActionSetup<T> actionSetup = actionBuilder.build();
        testExecutionContext.setProperty(CHANNEL, t);
        testExecutionContext.setProperty(MESSAGE_BUS, t);
        testExecutionContext.setProperty(SUT, t);

        final List<TestAction<T>> channelActions = actionSetup.channelActions;
        for (final TestAction<T> testAction : channelActions) {
            testAction.execute(t, testExecutionContext);
        }
        if (testExecutionContext.has(EXECUTION_END_SEMAPHORE)) {
            final Semaphore blockingSemaphoreToReleaseAfterExecution = testExecutionContext.getPropertyAsType(EXECUTION_END_SEMAPHORE, Semaphore.class);
            blockingSemaphoreToReleaseAfterExecution.release(1000);
        }
        if (t instanceof Channel) {
            final Channel channel = (Channel) t;
            channel.close(true);
            if (!channel.awaitTermination(3, TimeUnit.SECONDS)) {
                throw new RuntimeException("Channel did shutdown within timeout interval.");
            }
        }
        if (t instanceof MessageBus) {
            final MessageBus messageBus = (MessageBus) t;
            messageBus.close(true);
            if (!messageBus.awaitTermination(3, TimeUnit.SECONDS)) {
                throw new RuntimeException("Messagebus did shutdown within timeout interval.");
            }
        }
        return new Then<>(setup);
    }
}
