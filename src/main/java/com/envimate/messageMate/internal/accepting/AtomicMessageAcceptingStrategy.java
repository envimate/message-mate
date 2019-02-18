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

package com.envimate.messageMate.internal.accepting;

import com.envimate.messageMate.internal.eventloop.AcceptingEventLoop;
import lombok.RequiredArgsConstructor;

import java.util.Date;

import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
final class AtomicMessageAcceptingStrategy<T> implements MessageAcceptingStrategy<T> {
    private final AcceptingEventLoop<T> eventLoop;
    private volatile boolean isRunning = true;

    @Override
    public synchronized boolean accept(final T message) {
        if (!isRunning) {
            eventLoop.messageDropped(message);
            return false;
        } else {
            eventLoop.messageAccepted(message);
            //TODO: corrrect?
            while (!eventLoop.requestTransport(message)) {
                try {
                    System.out.println("Waiting "+Thread.currentThread());
                    this.wait();
                    System.out.println("Resumed "+Thread.currentThread());
                } catch (final InterruptedException e) {
                    return false;
                }
            }
            return true;
        }
    }

    @Override
    public boolean acceptDirectFollowUpMessage(final T message) {
        return accept(message);
    }

    @Override
    public synchronized void markSuccessfulDelivered(final T message) {
        this.notifyAll();
    }

    @Override
    public synchronized void markDeliveryAborted(final T message) {
        this.notifyAll();
    }

    @Override
    public synchronized void informTransportAvailable(final int numberOfAvailableTransportProcesses) {
        this.notifyAll();
    }

    @Override
    public void close(final boolean finishRemainingTasks) {
        if (isRunning) {
            isRunning = false;
        }
    }

    @Override
    public boolean awaitTermination(final Date deadline) {
        return true;
    }

    @Override
    public boolean isShutdown() {
        return true;
    }

}
