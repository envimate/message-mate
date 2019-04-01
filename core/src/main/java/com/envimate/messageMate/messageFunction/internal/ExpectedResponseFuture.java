/*
 * Copyright (c) 2018 envimate GmbH - https://envimate.com/.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.envimate.messageMate.messageFunction.internal;

import com.envimate.messageMate.messageFunction.ResponseFuture;
import com.envimate.messageMate.messageFunction.followup.FollowUpAction;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.*;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExpectedResponseFuture implements ResponseFuture {
    private final CountDownLatch countDownLatch = new CountDownLatch(1);
    private volatile boolean isCancelled;
    private volatile Object response;
    private volatile boolean successful;
    private volatile FollowUpAction followUpAction;
    private volatile Exception thrownException;

    public static ExpectedResponseFuture expectedResponseFuture() {
        return new ExpectedResponseFuture();
    }

    public synchronized void fullFill(final Object response) {
        if (!isCancelled()) {
            this.response = response;
            this.successful = true;
            countDownLatch.countDown();
            if (followUpAction != null) {
                followUpAction.apply(response, null);
            }
        }
    }

    public void fullFillWithException(final Exception e) {
        if (!isCancelled()) {
            this.thrownException = e;
            this.successful = false;
            countDownLatch.countDown();
            if (followUpAction != null) {
                followUpAction.apply(null, e);
            }
        }
    }

    @Override
    public boolean wasSuccessful() {
        return !isCancelled && successful;
    }

    @Override
    public synchronized boolean cancel(final boolean mayInterruptIfRunning) {
        if (!isDone()) {
            isCancelled = true;
        }
        countDownLatch.countDown();
        return !alreadyCompleted();
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public boolean isDone() {
        return alreadyCompleted() || isCancelled() || hasExceptionInExecution();
    }

    private boolean alreadyCompleted() {
        return response != null;
    }

    private boolean hasExceptionInExecution() {
        return thrownException != null;
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        if (!isDone()) {
            countDownLatch.await();
            //if threads is woken up with countDown in "cancel", then it should be handled as Interrupt;
            if (isCancelled()) {
                throw new InterruptedException();
            }
        }
        if (hasExceptionInExecution()) {
            throw new ExecutionException(thrownException);
        } else if (isCancelled()) {
            throw new CancellationException();
        } else {
            return response;
        }
    }

    @Override
    public Object get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (!isDone()) {
            if (!countDownLatch.await(timeout, unit)) {
                throw new TimeoutException("Response future timed out");
            }

            //if threads is woken up with countDown in "cancel", then it should be handled as Interrupt;
            if (isCancelled()) {
                throw new InterruptedException();
            }
        }
        if (isCancelled()) {
            throw new CancellationException();
        }
        if (hasExceptionInExecution()) {
            throw new ExecutionException(thrownException);
        } else {
            return response;
        }
    }

    @Override
    public synchronized void then(final FollowUpAction followUpAction) {
        if (this.followUpAction != null) {
            throw new UnsupportedOperationException("Then can only be called once.");
        } else {
            this.followUpAction = followUpAction;
            if (isDone()) {
                if (isCancelled()) {
                    throw new CancellationException();
                } else {
                    followUpAction.apply(this.response, this.thrownException);
                }
            }
        }
    }
}