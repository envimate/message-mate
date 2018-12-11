package com.envimate.messageMate.shared.givenWhenThen;



import com.envimate.messageMate.internal.statistics.MessageStatistics;
import com.envimate.messageMate.shared.subscriber.BlockingTestSubscriber;
import com.envimate.messageMate.shared.subscriber.TestSubscriber;
import com.envimate.messageMate.shared.testMessages.InvalidTestMessage;
import com.envimate.messageMate.shared.testMessages.TestMessage;
import com.envimate.messageMate.shared.testMessages.TestMessageOfInterest;
import com.envimate.messageMate.shared.testMessages.TestMessageToFilterOut;
import com.envimate.messageMate.subscribing.Subscriber;
import com.envimate.messageMate.subscribing.SubscriptionId;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

import static com.envimate.messageMate.shared.context.TestExecutionProperty.*;
import static com.envimate.messageMate.shared.subscriber.BlockingTestSubscriber.blockingTestSubscriber;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public abstract class ActionBuilder<T> {
    private final List<TestAction<T>> actions;

    protected ActionBuilder() {
        this.actions = new LinkedList<>();
    }

    protected ActionBuilder<T> thatSendsASingleMessage() {
        final ActionBuilder<T> that = this;
        return this.withAnAction((t, executionContext) -> {
            final TestMessageOfInterest message = TestMessageOfInterest.messageOfInterest();
            executionContext.setProperty(SINGLE_SEND_MESSAGE, message);
            that.send(t, message);
        });
    }

    protected ActionBuilder<T> withAnAction(final TestAction<T> testAction) {
        this.actions.add(testAction);
        return this;
    }

    protected ActionBuilder<T> thatSendsSeveralMessages(final int numberOfMessages) {
        final ActionBuilder<T> that = this;
        return this.withAnAction((t, executionContext) -> {
            for (int i = 0; i < numberOfMessages; i++) {
                final TestMessageOfInterest message = TestMessageOfInterest.messageOfInterest();
                executionContext.addToListProperty(MESSAGES_SEND_OF_INTEREST, message);
                that.send(t, message);
            }
        });
    }

    protected ActionBuilder<T> thatSendsSeveralMessagesAsynchronously(final int numberOfSender, final int numberOfMessagesPerSender) {
        return sendSeveralMessagesInTheirOwnThread(numberOfSender, numberOfMessagesPerSender, true);
    }

    protected ActionBuilder<T> thatSendsSeveralMessagesAsynchronouslyButWillBeBlocked(final int numberOfSender, final int numberOfMessagesPerSender) {
        return sendSeveralMessagesInTheirOwnThread(numberOfSender, numberOfMessagesPerSender, false);
    }

    protected ActionBuilder<T> sendSeveralMessagesInTheirOwnThread(final int numberOfSender, final int numberOfMessagesPerSender, final boolean expectCleanShutdown) {
        final ActionBuilder<T> that = this;
        final CyclicBarrier sendingStartBarrier = new CyclicBarrier(numberOfSender);
        return this.withAnAction((t, executionContext) -> {
            final ExecutorService executorService = Executors.newFixedThreadPool(numberOfSender);
            for (int i = 0; i < numberOfSender; i++) {
                executorService.execute(() -> {
                    final List<TestMessageOfInterest> messagesToSend = new ArrayList<>();

                    for (int j = 0; j < numberOfMessagesPerSender; j++) {
                        final TestMessageOfInterest message = TestMessageOfInterest.messageOfInterest();
                        messagesToSend.add(message);
                        executionContext.addToListProperty(MESSAGES_SEND_OF_INTEREST, message);
                    }
                    try {
                        sendingStartBarrier.await(3, SECONDS);
                    } catch (final InterruptedException | BrokenBarrierException | TimeoutException e) {
                        throw new RuntimeException(e);
                    }
                    for (final TestMessageOfInterest message : messagesToSend) {
                        that.send(t, message);
                    }
                });
            }
            executorService.shutdown();
            if (expectCleanShutdown) {
                try {
                    final boolean isTerminated = executorService.awaitTermination(3, SECONDS);
                    if (!isTerminated) {
                        throw new RuntimeException("ExecutorService did not shutdown within timeout.");
                    }
                } catch (final InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    protected ActionBuilder<T> thatUnsubscribesASubscriber() {
        return this.thatUnsubscribesASubscriberSeveralTimes(1);
    }

    protected ActionBuilder<T> thatUnsubscribesASubscriberSeveralTimes(final int numberOfUnsubscriptions) {
        final ActionBuilder<T> that = this;
        return this.withAnAction((t, executionContext) -> {
            final List<Subscriber<Object>> currentSubscriber;
            if (executionContext.has(EXPECTED_SUBSCRIBER)) {
                currentSubscriber = (List<Subscriber<Object>>) executionContext.getProperty(EXPECTED_SUBSCRIBER);
            } else {
                currentSubscriber = (List<Subscriber<Object>>) executionContext.getProperty(INITIAL_SUBSCRIBER);
            }
            final Subscriber<Object> firstSubscriber = currentSubscriber.get(0);
            final SubscriptionId subscriptionId = firstSubscriber.getSubscriptionId();
            for (int i = 0; i < numberOfUnsubscriptions; i++) {
                that.unsubscribe(t, subscriptionId);
            }
            final List<Subscriber<Object>> remainingSubscriber = currentSubscriber.subList(1, currentSubscriber.size());
            executionContext.setProperty(EXPECTED_SUBSCRIBER, remainingSubscriber);
        });
    }

    protected ActionBuilder<T> thatSendsBothValidAndInvalidMessagesAsynchronously(final int numberOfSender, final int numberOfMessagesPerSender) {
        final ActionBuilder<T> that = this;
        final CyclicBarrier sendingStartBarrier = new CyclicBarrier(numberOfSender);
        return this.withAnAction((t, executionContext) -> {
            final ExecutorService executorService = Executors.newFixedThreadPool(numberOfSender);
            for (int i = 0; i < numberOfSender; i++) {
                executorService.execute(() -> {
                    final List<TestMessage> messagesToSend = new ArrayList<>();

                    for (int j = 0; j < numberOfMessagesPerSender; j++) {
                        if (Math.random() > 0.5) {
                            final TestMessageOfInterest message = TestMessageOfInterest.messageOfInterest();
                            executionContext.addToListProperty(MESSAGES_SEND_OF_INTEREST, message);
                            messagesToSend.add(message);
                        } else {
                            final TestMessageToFilterOut message = TestMessageToFilterOut.testMessageToFilterOut();
                            messagesToSend.add(message);
                        }
                    }
                    try {
                        sendingStartBarrier.await(3, SECONDS);
                    } catch (final InterruptedException | BrokenBarrierException | TimeoutException e) {
                        throw new RuntimeException(e);
                    }
                    for (final TestMessage message : messagesToSend) {
                        that.send(t, message);
                    }
                });
            }
            executorService.shutdown();
            try {
                final boolean isTerminated = executorService.awaitTermination(3, SECONDS);
                if (!isTerminated) {
                    throw new RuntimeException("ExecutorService did not shutdown within timeout.");
                }
            } catch (final InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    protected ActionBuilder<T> thatSendsSeveralInvalidMessagesAsynchronously(final int numberOfSender, final int numberOfMessagesPerSender) {
        final CyclicBarrier sendingStartBarrier = new CyclicBarrier(numberOfSender);
        final ActionBuilder<T> that = this;
        return this.withAnAction((t, executionContext) -> {
            final ExecutorService executorService = Executors.newFixedThreadPool(numberOfSender);
            for (int i = 0; i < numberOfSender; i++) {
                executorService.execute(() -> {
                    final List<TestMessage> messagesToSend = new ArrayList<>();

                    for (int j = 0; j < numberOfMessagesPerSender; j++) {
                        final InvalidTestMessage message = InvalidTestMessage.invalidTestMessage();
                        messagesToSend.add(message);
                        executionContext.addToListProperty(MESSAGES_SEND, message);
                    }
                    try {
                        sendingStartBarrier.await(3, SECONDS);
                    } catch (final InterruptedException | BrokenBarrierException | TimeoutException e) {
                        throw new RuntimeException(e);
                    }
                    for (final TestMessage message : messagesToSend) {
                        that.send(t, message);
                    }
                });
            }
            executorService.shutdown();
            try {
                final boolean isTerminated = executorService.awaitTermination(3, SECONDS);
                if (!isTerminated) {
                    throw new RuntimeException("ExecutorService did not shutdown within timeout.");
                }
            } catch (final InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    protected ActionBuilder<T> thatPerformsAShortWait(final long timeout, final TimeUnit timeUnit) {
        return this.withAnAction((t, executionContext) -> {
            try {
                timeUnit.sleep(timeout);
            } catch (final InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    protected ActionBuilder<T> thatQueriesTheNumberOfAcceptedMessages() {
        final ActionBuilder<T> that = this;
        return this.withAnAction((t, executionContext) -> {
            final MessageStatistics messageStatistics = that.getMessageStatistics(t);
            final BigInteger bigInteger = messageStatistics.getAcceptedMessages();
            final long longValueExact = bigInteger.longValueExact();
            executionContext.setProperty(RESULT, longValueExact);
        });
    }

    protected ActionBuilder<T> thatQueriesTheNumberOfAcceptedMessagesAsynchronously() {
        final ActionBuilder<T> that = this;
        return this.withAnAction((t, executionContext) -> {
            final Semaphore semaphore = new Semaphore(0);
            new Thread(() -> {
                final MessageStatistics messageStatistics = that.getMessageStatistics(t);
                final BigInteger bigInteger = messageStatistics.getAcceptedMessages();
                final long longValueExact = bigInteger.longValueExact();
                executionContext.setProperty(RESULT, longValueExact);
                semaphore.release();
            }).start();
            try {
                semaphore.acquire();
            } catch (final InterruptedException e) {
                //not necessary to do anything here
            }
        });
    }

    protected ActionBuilder<T> thatQueriesTheNumberOfWaitingMessages() {
        final ActionBuilder<T> that = this;
        return this.withAnAction((t, executionContext) -> {
            final MessageStatistics messageStatistics = that.getMessageStatistics(t);
            final BigInteger bigInteger = messageStatistics.getWaitingMessages();
            final long longValueExact = bigInteger.longValueExact();
            executionContext.setProperty(RESULT, longValueExact);
        });
    }

    protected ActionBuilder<T> thatQueriesTheNumberOfSuccessfulDeliveredMessages() {
        final ActionBuilder<T> that = this;
        return this.withAnAction((t, executionContext) -> {
            final MessageStatistics messageStatistics = that.getMessageStatistics(t);
            final BigInteger bigInteger = messageStatistics.getSuccessfulMessages();
            final long longValueExact = bigInteger.longValueExact();
            executionContext.setProperty(RESULT, longValueExact);
        });
    }

    protected ActionBuilder<T> thatQueriesTheNumberOfFailedDeliveredMessages() {
        final ActionBuilder<T> that = this;
        return this.withAnAction((t, executionContext) -> {
            final MessageStatistics messageStatistics = that.getMessageStatistics(t);
            final BigInteger bigInteger = messageStatistics.getFailedMessages();
            final long longValueExact = bigInteger.longValueExact();
            executionContext.setProperty(RESULT, longValueExact);
        });
    }

    protected ActionBuilder<T> thatQueriesTheNumberOfDroppedMessages() {
        final ActionBuilder<T> that = this;
        return this.withAnAction((t, executionContext) -> {
            final MessageStatistics messageStatistics = that.getMessageStatistics(t);
            final BigInteger bigInteger = messageStatistics.getDroppedMessages();
            final long longValueExact = bigInteger.longValueExact();
            executionContext.setProperty(RESULT, longValueExact);
        });
    }

    protected ActionBuilder<T> thatQueriesTheNumberOfReplacedMessages() {
        final ActionBuilder<T> that = this;
        return this.withAnAction((t, executionContext) -> {
            final MessageStatistics messageStatistics = that.getMessageStatistics(t);
            final BigInteger bigInteger = messageStatistics.getReplacedMessages();
            final long longValueExact = bigInteger.longValueExact();
            executionContext.setProperty(RESULT, longValueExact);
        });
    }

    protected ActionBuilder<T> thatQueriesTheNumberOfForgottenMessages() {
        final ActionBuilder<T> that = this;
        return this.withAnAction((t, executionContext) -> {
            final MessageStatistics messageStatistics = that.getMessageStatistics(t);
            final BigInteger bigInteger = messageStatistics.getForgottenMessages();
            final long longValueExact = bigInteger.longValueExact();
            executionContext.setProperty(RESULT, longValueExact);
        });
    }

    protected ActionBuilder<T> thatQueriesTheNumberOfCurrentlyDeliveredMessages() {
        final ActionBuilder<T> that = this;
        return this.withAnAction((t, executionContext) -> {
            final MessageStatistics messageStatistics = that.getMessageStatistics(t);
            final BigInteger bigInteger = messageStatistics.getCurrentlyDeliveredMessages();
            final long longValueExact = bigInteger.longValueExact();
            executionContext.setProperty(RESULT, longValueExact);
        });
    }

    protected ActionBuilder<T> thatQueriesTheNumberOfCurrentlyTransportedMessages() {
        final ActionBuilder<T> that = this;
        return this.withAnAction((t, executionContext) -> {
            final MessageStatistics messageStatistics = that.getMessageStatistics(t);
            final BigInteger bigInteger = messageStatistics.getCurrentlyTransportedMessages();
            final long longValueExact = bigInteger.longValueExact();
            executionContext.setProperty(RESULT, longValueExact);
        });
    }

    protected ActionBuilder<T> thatQueriesTheTimestampOfTheMessageStatistics() {
        final ActionBuilder<T> that = this;
        return this.withAnAction((t, executionContext) -> {
            final MessageStatistics messageStatistics = that.getMessageStatistics(t);
            final Date timestamp = messageStatistics.getTimestamp();
            executionContext.setProperty(RESULT, timestamp);
        });
    }

    protected ActionBuilder<T> thatShutdownsTheObject() {
        final ActionBuilder<T> that = this;
        return this.withAnAction((t, executionContext) -> that.close(t, true));
    }

    protected ActionBuilder<T> thatSendsHalfOfMessagesThenCallsShutdown(final int numberOfMessages) {
        final int numberOfMessagesBeforeShutdown = numberOfMessages / 2;
        return thatSendsXMessagesAShutdownsIsCalledThenSendsYMessage(numberOfMessagesBeforeShutdown, 0, true);
    }

    protected ActionBuilder<T> thatSendsHalfOfMessagesThenCallsShutdownwithoutFinishingRemainingTasks(final int numberOfMessages) {
        final int numberOfMessagesBeforeShutdown = numberOfMessages / 2;
        return thatSendsXMessagesAShutdownsIsCalledThenSendsYMessage(numberOfMessagesBeforeShutdown, 0, false);
    }

    protected ActionBuilder<T> thatSendsXMessagesAShutdownsIsCalledThenSendsYMessage(final int numberOfMessagesBeforeShutdown, final int numberOfMessagesAfterShutdown, final boolean finishRemainingTask) {
        final ActionBuilder<T> that = this;
        return this.withAnAction((t, executionContext) -> {
            final Semaphore semaphore = new Semaphore(0);
            final TestSubscriber<TestMessageOfInterest> subscriber = blockingTestSubscriber(semaphore);
            that.subscribe(t, TestMessageOfInterest.class, subscriber);
            executionContext.setProperty(SINGLE_RECEIVER, subscriber);

            final ExecutorService executorService = Executors.newFixedThreadPool(numberOfMessagesBeforeShutdown);
            final CyclicBarrier barrierToWaitBeforeContinuing = new CyclicBarrier(numberOfMessagesBeforeShutdown + 1);
            for (int i = 0; i < numberOfMessagesBeforeShutdown; i++) {
                executorService.execute(() -> {
                    final TestMessageOfInterest message = TestMessageOfInterest.messageOfInterest();
                    executionContext.addToListProperty(MESSAGES_SEND, message);
                    try {
                        barrierToWaitBeforeContinuing.await();
                    } catch (final InterruptedException | BrokenBarrierException e) {
                        throw new RuntimeException(e);
                    }
                    that.send(t, message);
                });
            }
            try {
                barrierToWaitBeforeContinuing.await();
                MILLISECONDS.sleep(100);
            } catch (final InterruptedException | BrokenBarrierException e) {
                throw new RuntimeException(e);
            }
            that.close(t, finishRemainingTask);
            semaphore.release(1000);
            for (int i = 0; i < numberOfMessagesAfterShutdown; i++) {
                final TestMessageOfInterest message = TestMessageOfInterest.messageOfInterest();
                executionContext.addToListProperty(MESSAGES_SEND, message);
                that.send(t, message);
            }
        });
    }

    protected ActionBuilder<T> thatSendsSeveralMessagesAsynchronouslyBeforeTheObjectIsShutdown(final int numberOfSenders, final int numberOfMessages) {
        final ActionBuilder<T> that = this;
        return this.withAnAction((t, executionContext) -> {
            final Semaphore semaphore = new Semaphore(0);
            executionContext.setProperty(EXECUTION_END_SEMAPHORE, semaphore);
            final BlockingTestSubscriber<TestMessageOfInterest> subscriber = blockingTestSubscriber(semaphore);
            that.subscribe(t, TestMessageOfInterest.class, subscriber);
            executionContext.setProperty(SINGLE_RECEIVER, subscriber);

            final ExecutorService executorService = Executors.newFixedThreadPool(numberOfSenders);
            for (int i = 0; i < numberOfSenders; i++) {
                executorService.execute(() -> {
                    for (int j = 0; j < numberOfMessages; j++) {
                        final TestMessageOfInterest message = TestMessageOfInterest.messageOfInterest();
                        executionContext.addToListProperty(MESSAGES_SEND, message);
                        that.send(t, message);
                    }
                });
            }
            try {
                MILLISECONDS.sleep(10);
            } catch (final InterruptedException e) {
                throw new RuntimeException(e);
            }
            that.close(t, false);
        });
    }


    protected ActionBuilder<T> thatShutdownsTheObjectAsynchronouslyXTimes(final int numberOfThreads) {
        final ActionBuilder<T> that = this;
        return this.withAnAction((t, executionContext) -> {
            final ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
            for (int i = 0; i < numberOfThreads; i++) {
                executorService.execute(() -> {
                    that.close(t, false);
                });
            }
        });
    }

    protected ActionBuilder<T> thatAwaitsTheShutdownTimeoutInSeconds(final int timeoutInSeconds) {
        final ActionBuilder<T> that = this;
        return this.withAnAction((t, executionContext) -> {
            try {
                final boolean terminatedSuccessful = that.awaitTermination(t, timeoutInSeconds, TimeUnit.SECONDS);
                executionContext.setProperty(RESULT, terminatedSuccessful);
            } catch (final InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }


    protected abstract <R> void subscribe(T t, Class<R> messageClass, Subscriber<R> subscriber);

    protected abstract void close(T t, boolean finishRemainingTasks);

    protected abstract boolean awaitTermination(T t, int timeout, TimeUnit timeUnit) throws InterruptedException;


    public ActionBuilder<T> andThen(final ActionBuilder<T> followUpBuilder) {
        actions.addAll(followUpBuilder.actions);
        return this;
    }


    protected abstract void unsubscribe(T t, SubscriptionId subscriptionId);


    protected abstract void send(T t, TestMessage message);

    protected abstract MessageStatistics getMessageStatistics(T t);


    public ActionSetup<T> build() {
        return ActionSetup.actionSetup(actions);
    }

}
