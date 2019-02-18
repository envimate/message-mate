package com.envimate.messageMate.internal.transport.pooled;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Consumer;

import static java.lang.Integer.MAX_VALUE;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

class PooledMessageTransportThreadPoolExecutor extends ThreadPoolExecutor {

    public PooledMessageTransportThreadPoolExecutor(final int numberOfThreads) {
        super(numberOfThreads, numberOfThreads, MAX_VALUE, MILLISECONDS, new SynchronousQueue<>());
    }

    @Override
    protected void afterExecute(final Runnable r, final Throwable t) {
        super.afterExecute(r, t);
        System.out.println("Finished: "+r);
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
        System.out.println("Started in Thread: "+t);
    }

}
