package com.envimate.messageMate.internal.transport.pooled;

import com.envimate.messageMate.filtering.Filter;
import com.envimate.messageMate.internal.eventloop.TransportEventLoop;
import com.envimate.messageMate.internal.filtering.FilterApplier;
import com.envimate.messageMate.internal.transport.MessageTransportProcess;
import com.envimate.messageMate.internal.transport.MessageTransportProcessFactory;
import com.envimate.messageMate.internal.transport.SubscriberCalculation;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import static com.envimate.messageMate.internal.filtering.FilterApplierFactory.filterApplier;
import static com.envimate.messageMate.internal.transport.pooled.PoolControlThread.poolControlThread;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class PooledMessageTransportProcessFactory<T> implements MessageTransportProcessFactory<T> {
    private final TransportEventLoop<T> eventLoop;
    private final FilterApplier<T> filterApplier;
    private final SubscriberCalculation<T> subscriberCalculation;
    private final ThreadPoolExecutor threadPoolExecutor;
    private final AtomicInteger availableProcesses;
    private final PoolControlThread<T> controlThread;
    private volatile boolean closed; //TODO: atomic boolean maybe better?

    public PooledMessageTransportProcessFactory(final TransportEventLoop<T> eventLoop,
                                                final SubscriberCalculation<T> subscriberCalculation, int numberOfThreads) {
        numberOfThreads = 1;
        this.eventLoop = eventLoop;
        this.filterApplier = filterApplier();
        this.subscriberCalculation = subscriberCalculation;
        this.controlThread = poolControlThread(process -> {
            markProcessAsFinished();
        }, eventLoop);
        this.availableProcesses = new AtomicInteger(numberOfThreads);
        this.threadPoolExecutor = new PooledMessageTransportThreadPoolExecutor(numberOfThreads);
        controlThread.start();

    }

    private void markProcessAsFinished() {
        System.out.println("Active: " + threadPoolExecutor.getActiveCount() + "; " + threadPoolExecutor.getCompletedTaskCount());
        availableProcesses.incrementAndGet();
    }

    @Override
    public synchronized MessageTransportProcess<T> getNext(final T message) {
        final int availProcesses = availableProcesses.get();
        final long completedTaskCount = threadPoolExecutor.getCompletedTaskCount();
        if (availProcesses > 0) {
            System.out.println("getNext|proc| avail=" + availProcesses + ", completed=" + completedTaskCount + ", active=" + threadPoolExecutor.getActiveCount());
            availableProcesses.decrementAndGet();
            final List<Filter<T>> filters = null;
            return new PooledMessageTransportProcess<>(eventLoop, filterApplier, filters, threadPoolExecutor, subscriberCalculation, controlThread);
        } else {
            System.out.println("getNext|null| avail=" + availProcesses + ", completed=" + completedTaskCount + ", active=" + threadPoolExecutor.getActiveCount());
            return null;
        }
    }

    @Override
    public synchronized void close(final boolean finishRemainingTasks) {
        if (closed) {
            return;
        }
        closed = true;
        if (finishRemainingTasks) {
            threadPoolExecutor.shutdown();
        } else {
            threadPoolExecutor.shutdownNow();
        }
    }

    @Override
    public boolean awaitTermination(final Date deadline) throws InterruptedException {
        final long remainingMilliseconds = deadline.getTime() - System.currentTimeMillis();
        return threadPoolExecutor.awaitTermination(remainingMilliseconds, MILLISECONDS);
    }

    @Override
    public boolean isShutdown() {
        return threadPoolExecutor.isShutdown();
    }

}
