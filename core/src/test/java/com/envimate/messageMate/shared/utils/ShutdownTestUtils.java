package com.envimate.messageMate.shared.utils;

import com.envimate.messageMate.processingContext.EventType;
import com.envimate.messageMate.shared.environment.TestEnvironment;
import com.envimate.messageMate.shared.pipeChannelMessageBus.testActions.CloseActions;
import com.envimate.messageMate.shared.pipeChannelMessageBus.testActions.SendingAndReceivingActions;
import com.envimate.messageMate.shared.polling.PollingUtils;
import com.envimate.messageMate.shared.subscriber.BlockingTestSubscriber;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import com.envimate.messageMate.shared.testMessages.TestMessageOfInterest;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.*;

import static com.envimate.messageMate.shared.environment.TestEnvironmentProperty.*;
import static com.envimate.messageMate.shared.eventType.TestEventType.testEventType;
import static com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeChannelMessageBusSharedTestProperties.EXECUTION_END_SEMAPHORE;
import static com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen.PipeChannelMessageBusSharedTestProperties.SINGLE_RECEIVER;
import static com.envimate.messageMate.shared.polling.PollingUtils.pollUntilEquals;
import static com.envimate.messageMate.shared.subscriber.BlockingTestSubscriber.blockingTestSubscriber;
import static com.envimate.messageMate.shared.testMessages.TestMessageOfInterest.messageOfInterest;
import static com.envimate.messageMate.shared.utils.AsynchronousSendingTestUtils.*;
import static com.envimate.messageMate.useCases.givenWhenThen.UseCaseInvocationTestProperties.EVENT_TYPE;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class ShutdownTestUtils {

    public static void shutdownTheSut(final CloseActions closeActions) {
        shutdownTheSut(closeActions, true);
    }

    public static void shutdownTheSut(final CloseActions closeActions, final boolean finishRemainingTasks) {
        closeActions.close(finishRemainingTasks);
    }

    public static void shutdownTheSutAsynchronouslyXTimes(final CloseActions closeActions, final int shutdownCalls) {
        final ExecutorService executorService = Executors.newFixedThreadPool(shutdownCalls);
        for (int i = 0; i < shutdownCalls; i++) {
            executorService.execute(() -> closeActions.close(false));
        }
        executorService.shutdown();
        try {
            final boolean terminationSuccessful = executorService.awaitTermination(1, SECONDS);
            if (!terminationSuccessful) {
                throw new RuntimeException("Executor service did not shutdown in a clean way.");
            }
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void sendMessagesBeforeShutdownAsynchronously(final SendingAndReceivingActions sutActions,
                                                                final TestEnvironment testEnvironment,
                                                                final int numberOfMessages,
                                                                final boolean finishRemainingTasks) {
        sendMessagesBeforeShutdownAsynchronously(sutActions, testEnvironment, numberOfMessages, finishRemainingTasks, numberOfMessages);
    }

    public static void sendMessagesBeforeShutdownAsynchronously(final SendingAndReceivingActions sutActions,
                                                                final TestEnvironment testEnvironment,
                                                                final int numberOfMessages,
                                                                final boolean finishRemainingTasks,
                                                                final int expectedNumberOfBlockedThreads) {
        final Semaphore semaphore = new Semaphore(0);
        final BlockingTestSubscriber<TestMessage> subscriber = blockingTestSubscriber(semaphore);
        addABlockingSubscriberAndThenSendXMessagesInEachThread(sutActions, subscriber, numberOfMessages, 1, testEnvironment, expectedNumberOfBlockedThreads);
        sutActions.close(finishRemainingTasks);
        semaphore.release(1337);
    }

    public static void callAwaitWithoutACloseIsCalled(final CloseActions closeActions, final TestEnvironment testEnvironment) {
        try {
            final boolean terminatedSuccessful = closeActions.await(0, SECONDS);
            testEnvironment.setProperty(RESULT, terminatedSuccessful);
            testEnvironment.setProperty(EXPECTED_RESULT, false);
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void shutDownTheSutThenSendAMessage(final SendingAndReceivingActions sutActions,
                                                      final TestEnvironment testEnvironment) {
        sutActions.close(false);
        try {
            sutActions.await(1, SECONDS);
            final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
            final TestMessageOfInterest message = messageOfInterest();
            sutActions.send(eventType, message);
        } catch (final Exception e) {
            testEnvironment.setProperty(EXCEPTION, e);
        }
    }

    public static void closeAndThenWaitForPendingTasksToFinished(final SendingAndReceivingActions sutActions,
                                                                 final int numberOfPendingMessages,
                                                                 final TestEnvironment testEnvironment) {
        try {
            final Semaphore semaphore = new Semaphore(0);
            final BlockingTestSubscriber<TestMessage> subscriber = sendMessagesToBlockingSubscriber(sutActions,
                    numberOfPendingMessages, testEnvironment, semaphore);
            sutActions.close(true);

            final CyclicBarrier cyclicBarrier = new CyclicBarrier(2);
            new Thread(() -> {
                try {
                    cyclicBarrier.await();
                    MILLISECONDS.sleep(10);
                    semaphore.release(1000);
                } catch (InterruptedException | BrokenBarrierException e) {
                    testEnvironment.setProperty(EXCEPTION, e);
                }
            }).start();
            cyclicBarrier.await();
            final boolean terminatedSuccessful = sutActions.await(1, SECONDS);
            pollUntilEquals(() -> subscriber.getReceivedMessages().size(), numberOfPendingMessages);
            testEnvironment.setProperty(RESULT, terminatedSuccessful);
        } catch (final InterruptedException | BrokenBarrierException e) {
            testEnvironment.setPropertyIfNotSet(EXCEPTION, e);
        }
    }

    private static BlockingTestSubscriber<TestMessage> sendMessagesToBlockingSubscriber(final SendingAndReceivingActions sutActions,
                                                                                        final int numberOfPendingMessages,
                                                                                        final TestEnvironment testEnvironment,
                                                                                        final Semaphore semaphore) {
        return sendMessagesToBlockingSubscriber(sutActions, numberOfPendingMessages, numberOfPendingMessages, testEnvironment, semaphore);
    }

    private static BlockingTestSubscriber<TestMessage> sendMessagesToBlockingSubscriber(final SendingAndReceivingActions sutActions,
                                                                                        final int numberOfPendingMessages,
                                                                                        final int expectedNumberOfBlockedThreads,
                                                                                        final TestEnvironment testEnvironment,
                                                                                        final Semaphore semaphore) {
        final BlockingTestSubscriber<TestMessage> subscriber = blockingTestSubscriber(semaphore);
        testEnvironment.setProperty(SINGLE_RECEIVER, subscriber);
        final EventType eventType = testEnvironment.getPropertyOrSetDefault(EVENT_TYPE, testEventType());
        sutActions.subscribe(eventType, subscriber);
        sendXMessagesInTheirOwnThreadThatWillBeBlocked(sutActions, numberOfPendingMessages, testEnvironment);
        pollUntilEquals(subscriber::getBlockedThreads, expectedNumberOfBlockedThreads);
        return subscriber;
    }

    public static void sendMessagesBeforeAndAfterShutdownAsynchronously(final SendingAndReceivingActions sutActions,
                                                                        final TestEnvironment testEnvironment,
                                                                        final int numberOfMessagesBeforeShutdown,
                                                                        final int numberOfMessagesAfterShutdown,
                                                                        final boolean finishRemainingTask) {
        final Semaphore semaphore = new Semaphore(0);
        final BlockingTestSubscriber<TestMessage> subscriber = blockingTestSubscriber(semaphore);
        addABlockingSubscriberAndThenSendXMessagesInEachThread(sutActions, subscriber, numberOfMessagesBeforeShutdown, testEnvironment);
        sutActions.close(finishRemainingTask);
        sendXMessagesAsynchronouslyThatWillFail(sutActions, numberOfMessagesAfterShutdown, testEnvironment);
        semaphore.release(1337);
        if (finishRemainingTask) {
            PollingUtils.pollUntilEquals(() -> subscriber.getReceivedMessages().size(), numberOfMessagesBeforeShutdown);
        }
    }

    public static void callCloseThenAwaitWithBlockedSubscriberWithoutReleasingLock(final SendingAndReceivingActions sutActions,
                                                                                   final TestEnvironment testEnvironment,
                                                                                   final int numberOfMessagesSend) {
        callCloseThenAwaitWithBlockedSubscriberWithoutReleasingLock(sutActions, testEnvironment, numberOfMessagesSend, numberOfMessagesSend);
    }

    public static void callCloseThenAwaitWithBlockedSubscriberWithoutReleasingLock(final SendingAndReceivingActions sutActions,
                                                                                   final TestEnvironment testEnvironment,
                                                                                   final int numberOfMessagesSend,
                                                                                   final int expectedNumberOfBlockedThreads) {
        try {
            final Semaphore semaphore = new Semaphore(0);
            final BlockingTestSubscriber<TestMessage> subscriber = sendMessagesToBlockingSubscriber(sutActions,
                    numberOfMessagesSend, expectedNumberOfBlockedThreads, testEnvironment, semaphore);
            sutActions.close(true);
            final boolean terminatedSuccessful = sutActions.await(15, MILLISECONDS);
            testEnvironment.setProperty(RESULT, terminatedSuccessful);
            testEnvironment.setProperty(SINGLE_RECEIVER, subscriber);
            testEnvironment.setProperty(EXECUTION_END_SEMAPHORE, semaphore);
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
