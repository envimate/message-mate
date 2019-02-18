package com.envimate.messageMate.internal.accepting;

import com.envimate.messageMate.internal.eventloop.AcceptingEventLoop;
import lombok.RequiredArgsConstructor;

import java.util.Date;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CountDownLatch;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
public final class QueuingMessageAcceptingStrategy<T> implements MessageAcceptingStrategy<T> {
    private final AcceptingEventLoop<T> eventLoop;
    private final Deque<T> queuedMessages = new ConcurrentLinkedDeque<>();
    private final CountDownLatch allTasksFinishedCountDownLatch = new CountDownLatch(1);
    private final Object closeSynchronisationObject = new Object();
    private volatile boolean closed;
    private volatile boolean finishedRemainingTasksAfterClose;

    @Override
    public boolean accept(final T message) {
        if (closed) {
            //TODO: dropped
            return false;
        }
        eventLoop.messageAccepted(message);
        //requestTransport never blocks, therefore no interrupting logic needed
        final boolean transportStarted = eventLoop.requestTransport(message);
        if (!transportStarted) {
            queuedMessages.add(message);
            eventLoop.messageQueued(message);
        }
        return true;
    }

    @Override
    public boolean acceptDirectFollowUpMessage(final T message) {
        return accept(message);
    }

    @Override
    public void markSuccessfulDelivered(final T message) {
        if (!closed) {
            tryToTransportNextQueuedMessage();
        } else {
            if (finishedRemainingTasksAfterClose) {
                tryToTransportAllRemainingMessages();
            }
        }
    }

    @Override
    public void markDeliveryAborted(final T message) {
        if (!closed) {
            tryToTransportNextQueuedMessage();
        } else {
            if (finishedRemainingTasksAfterClose) {
                tryToTransportAllRemainingMessages();
            }
        }
    }

    @Override
    public void close(final boolean finishRemainingTasks) {
        synchronized (closeSynchronisationObject) {
            if (closed) {
                return;
            }
            closed = true;
            finishedRemainingTasksAfterClose = finishRemainingTasks;
        }

        if (finishRemainingTasks) {
            tryToTransportAllRemainingMessages();
        }
    }

    //TODO: test await for alle with exact these conditions
    /*
    Contract:
     without close: false
     after close(true): - true if queue empty or false otherwise
     after close(false) -> always true
    */
    @Override
    public boolean awaitTermination(final Date deadline) throws InterruptedException {
        if (!closed) {
            return false;
        } else {
            //if we do not have to clean up we are always done
            if (!finishedRemainingTasksAfterClose) {
                return true;
            } else {
                //no check for negative timout, because countDownLatch can cope with those
                final long remainingMilliseconds = deadline.getTime() - System.currentTimeMillis();
                //countdownLatch will always return the result of the first await call -> no need to save it somewhere
                allTasksFinishedCountDownLatch.await(remainingMilliseconds, MILLISECONDS);
                return queuedMessages.isEmpty();
            }
        }
    }

    @Override
    public boolean isShutdown() {
        return closed;
    }

    @Override
    public void informTransportAvailable(final int numberOfAvailableTransportProcesses) {
        //try to send as much as possible, so that we don't forget messages
        //performance overhead for one unnecessary "requestTransport " (nonblocking) call should not be high
        tryToTransportAsManyMessagesAsPossible();
    }

    private void tryToTransportAsManyMessagesAsPossible() {
        //Stop when closed signal is received -> only one last thread should try to send all remaining messages
        while (!queuedMessages.isEmpty() && !closed) {
            final boolean messageTransported = tryToTransportNextQueuedMessage();
            if (!messageTransported) {
                break;
            }
        }
    }

    private boolean tryToTransportNextQueuedMessage() {
        final T nextMessage = queuedMessages.pollFirst();
        if (nextMessage == null) {
            return false;
        }
        final boolean transportStarted = eventLoop.requestTransport(nextMessage);
        if (transportStarted) {
            eventLoop.messageDequeued(nextMessage);
            return true;
        } else {
            queuedMessages.addFirst(nextMessage);
            return false;
        }
    }

    private void tryToTransportAllRemainingMessages() {
        while (!queuedMessages.isEmpty()) {
            final boolean transportProceeded = tryToTransportNextQueuedMessage();
            if (!transportProceeded) {
                return;
            }
        }
        //when all remaining messages could be transported, let waiting threads return early from "await"
        allTasksFinishedCountDownLatch.countDown();
    }
}
