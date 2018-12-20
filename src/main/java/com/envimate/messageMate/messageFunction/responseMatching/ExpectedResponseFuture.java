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

package com.envimate.messageMate.messageFunction.responseMatching;

import com.envimate.messageMate.messageFunction.ResponseFuture;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.*;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class ExpectedResponseFuture<S> implements ResponseFuture<S> {
    private final CountDownLatch countDownLatch = new CountDownLatch(1);
    private volatile boolean isCancelled;
    private volatile S response;
    private volatile boolean successful;
    private volatile FollowUpAction<S> followUpAction;

    static <S> ExpectedResponseFuture<S> expectedResponseFuture() {
        return new ExpectedResponseFuture<>();
    }

    synchronized void fullFill(final S response, final boolean successful) {
        this.response = response;
        this.successful = successful;
        countDownLatch.countDown();
        if (followUpAction != null) {
            followUpAction.apply(response, successful);
        }
    }

    @Override
    public boolean wasSuccessful() {
        return !isCancelled && successful;
    }

    @Override
    public synchronized boolean cancel(final boolean mayInterruptIfRunning) {
        isCancelled = true;
        countDownLatch.countDown();
        return !alreadyCompleted();
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public boolean isDone() {
        return alreadyCompleted() || isCancelled();
    }

    private boolean alreadyCompleted() {
        return response != null;
    }

    @Override
    public S get() throws InterruptedException, ExecutionException {
        if (!isDone()) {
            countDownLatch.await();
            //if threads is woken up with countDown in "cancel", then it should be handled as Interrupt;
            if (isCancelled()) {
                throw new InterruptedException();
            }
        }
        if (isCancelled()) {
            throw new CancellationException();
        } else {
            return response;
        }
    }

    @Override
    public S get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
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
        } else {
            return response;
        }
    }

    @Override
    public synchronized void then(final FollowUpAction<S> followUpAction) {
        if (this.followUpAction != null) {
            throw new UnsupportedOperationException("Then can only be called once.");
        } else {
            this.followUpAction = followUpAction;
            if (isDone()) {
                if (isCancelled()) {
                    throw new CancellationException();
                } else {
                    followUpAction.apply(response, wasSuccessful());
                }
            }
        }
    }
}
