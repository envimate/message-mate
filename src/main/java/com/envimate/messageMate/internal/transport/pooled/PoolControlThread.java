package com.envimate.messageMate.internal.transport.pooled;

import com.envimate.messageMate.internal.eventloop.TransportEventLoop;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

import static com.envimate.messageMate.internal.transport.pooled.TransportResult.RESULT_TYPE.*;
import static com.envimate.messageMate.internal.transport.pooled.TransportResult.transportResult;
import static lombok.AccessLevel.PRIVATE;

public class PoolControlThread<T> extends Thread {
    private final ConcurrentLinkedQueue<TransportResult<T>> transportResults;
    private final Object resultsAvailableLock;
    private volatile boolean stopped;

    public PoolControlThread(final ConcurrentLinkedQueue<TransportResult<T>> transportResults,
                             final Object resultsAvailableLock,
                             final Consumer<T> onThreadFinishedCallback,
                             final TransportEventLoop<T> transportEventLoop) {
        super(new ControlRunnable<>(resultsAvailableLock, transportResults, onThreadFinishedCallback, transportEventLoop));
        this.transportResults = transportResults;
        this.resultsAvailableLock = resultsAvailableLock;
    }

    public static <T> PoolControlThread<T> poolControlThread(final Consumer<T> onThreadFinishedCallback,
                                                             final TransportEventLoop<T> transportEventLoop) {
        final ConcurrentLinkedQueue<TransportResult<T>> transportResults = new ConcurrentLinkedQueue<>();
        final Object resultsAvailableLock = new Object();
        return new PoolControlThread<>(transportResults, resultsAvailableLock, onThreadFinishedCallback, transportEventLoop);
    }

    public void messagePassedAllFilter(final T message) {
        informTransportFinished(transportResult(message, PASSED_BUT_YET_TO_DELIVER));
    }

    public void messageHandedOverToDelivery(final T message) {
        informTransportFinished(transportResult(message, MESSAGES_HANDED_OF_TO_DELIVERY));
    }

    public void messageBlockedByFilter(final T message) {
        informTransportFinished(transportResult(message, BLOCKED));
    }

    public void messageReplacedByFilter(final T message) {
        informTransportFinished(transportResult(message, REPLACED));
    }

    public void messageForgottenByFilter(final T message) {
        informTransportFinished(transportResult(message, FORGOTTEN));
    }

    private void informTransportFinished(final TransportResult<T> transportResult) {
        System.out.println("Result added to list " + transportResult.getResultType());
        transportResults.add(transportResult);
        synchronized (resultsAvailableLock) {
            resultsAvailableLock.notifyAll();
        }
    }

    @RequiredArgsConstructor(access = PRIVATE)
    private static final class ControlRunnable<T> implements Runnable {
        private final Object resultsAvailableLock;
        private final ConcurrentLinkedQueue<TransportResult<T>> transportResults;
        private final Consumer<T> onThreadFinishedCallback;
        private final TransportEventLoop<T> transportEventLoop;

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                synchronized (resultsAvailableLock) {
                    while (transportResults.isEmpty()) {
                        try {
                            System.out.println("ControlThread waits");
                            resultsAvailableLock.wait();
                        } catch (final InterruptedException e) {
                            return;
                        }
                    }
                    final TransportResult<T> transportResult = transportResults.poll();
                    System.out.println("handling result " + transportResult.getResultType());
                    final T message = transportResult.getMessage();
                    final TransportResult.RESULT_TYPE resultType = transportResult.getResultType();
                    switch (resultType) {
                        case PASSED_BUT_YET_TO_DELIVER:
                            transportEventLoop.messagePassedAllFilter(message);
                            break;
                        case BLOCKED:
                            onThreadFinishedCallback.accept(message);
                            transportEventLoop.messageBlockedByFilter(message);
                            transportEventLoop.messageTransportFinished(message);
                            break;
                        case REPLACED:
                            onThreadFinishedCallback.accept(message);
                            transportEventLoop.messageReplacedByFilter(message);
                            transportEventLoop.messageTransportFinished(message);
                            break;
                        case FORGOTTEN:
                            onThreadFinishedCallback.accept(message);
                            transportEventLoop.messageForgottenByFilter(message);
                            transportEventLoop.messageTransportFinished(message);
                            break;
                        case MESSAGES_HANDED_OF_TO_DELIVERY:
                            onThreadFinishedCallback.accept(message);
                            transportEventLoop.messageTransportFinished(message);
                            transportEventLoop.markTransportProcessesAsAvailable(1);
                            break;
                        default:
                            throw new IllegalStateException("Got a result with an unknown type: " + resultType);
                    }
                    System.out.println("Handling completed");
                }
            }
        }
    }
}
