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

package com.envimate.messageMate.messageBus;

import com.envimate.messageMate.exceptions.AlreadyClosedException;
import com.envimate.messageMate.filtering.Filter;
import com.envimate.messageMate.internal.autoclosable.NoErrorAutoClosable;
import com.envimate.messageMate.messageBus.exception.MessageBusExceptionListener;
import com.envimate.messageMate.subscribing.Subscriber;
import com.envimate.messageMate.subscribing.SubscriptionId;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Messages of different types can be sent over a {@code MessageBus}. {@code Subscribers} interested in specific messages
 * can subscribe on these selected messages. A {@code MessageBus} allows adding {@code Filters} to alter the message flow.
 *
 * @see <a href="https://github.com/envimate/message-mate#messagebus">Message Mate Documentation</a>
 */
public interface MessageBus extends NoErrorAutoClosable {

    /**
     * Sends the message on the {@code MessageBus}
     *
     * @param message the message to send
     * @throws AlreadyClosedException if {@code MessageBus} already closed
     */
    void send(Object message);

    /**
     * Adds the given {@code Consumer} wrapped in a {@code Subscriber} object.
     *
     * @param messageClass the class of interest
     * @param consumer     the consumer as {@code Subscriber}
     * @param <T>          the type of the message and the {@code consumer}
     * @return the {@code SubscriptionId} of the created {@code Subscriber}
     */
    <T> SubscriptionId subscribe(Class<T> messageClass, Consumer<T> consumer);

    /**
     * Adds the given {@code Subscriber}.
     *
     * @param messageClass the class of interest
     * @param subscriber   the {@code Subscriber} to add
     * @param <T>          the type of the message and the {@code Subscriber}
     * @return the {@code SubscriptionId} of the {@code Subscriber}
     */
    <T> SubscriptionId subscribe(Class<T> messageClass, Subscriber<T> subscriber);

    /**
     * Removes all {@code Subscribers} with the given {@code SubscriptionId}
     *
     * @param subscriptionId the {@code SubscriptionId} to remove {@code Subscribers}
     */
    void unsubcribe(SubscriptionId subscriptionId);

    /**
     * Adds a {@code Filter} to the accepting {@code Channel}.
     *
     * @param filter the {@code Filter} to be added
     */
    void add(Filter<Object> filter);

    /**
     * Adds the {@code Filter} to the accepting {@code Channel}
     *
     * @param filter   the {@code Filter} to be added
     * @param position the position of the {@code Filter}
     * @throws ArrayIndexOutOfBoundsException if the position is higher than the number of {@code Filter} or negative
     */
    void add(Filter<Object> filter, int position);

    /**
     * Returns all currently added {@code Filters}.
     *
     * @return all {@code Filters}
     */
    List<Filter<Object>> getFilter();

    /**
     * Removes the given {@code Filter}.
     *
     * @param filter the {@code Filter} to remove
     */
    void remove(Filter<Object> filter);

    /**
     * Adds a dynamic exception listener for the given message class.
     *
     * @param messageClass      the class of the message the listener should be called on
     * @param exceptionListener the exception listener
     * @param <T>               the type of the exception class
     * @return a {@code SubscriptionId} identifying the exception listener
     */
    <T> SubscriptionId onException(Class<T> messageClass, MessageBusExceptionListener<T> exceptionListener);

    /**
     * Adds a dynamic exception listener for the all classes contained in the list.
     *
     * @param messageClasses    list of classes of messages the listener should be called on
     * @param exceptionListener the exception listener
     * @param <T>               the type of the exception class
     * @return a {@code SubscriptionId} identifying exception listener
     */
    <T> SubscriptionId onException(List<Class<? extends T>> messageClasses,
                                   MessageBusExceptionListener<? extends T> exceptionListener);

    /**
     * Removes all exceptionListener with the given {@code SubscriptionId}.
     *
     * @param subscriptionId the {@code SubscriptionId} to remove exception listener for
     */
    void unregisterExceptionListener(SubscriptionId subscriptionId);

    /**
     * Removes the {@code MessageBusStatusInformation} interface, that allows querying statistics and {@code Subscribers}.
     *
     * @return the {@code MessageBusStatusInformation} interface
     */
    MessageBusStatusInformation getStatusInformation();

    /**
     * Closes the {@code MessageBus}.
     *
     * @param finishRemainingTasks when {@code true}, the {@code MessageBus} tries to deliver queued messages, otherwise
     *                             they are discarded
     */
    void close(boolean finishRemainingTasks);

    /**
     * Blocks the caller until all remaining tasks have completed execution after a {@code close} has been called, the timeout
     * occurs or the current thread is interrupted.
     *
     * @param timeout the duration to wait
     * @param unit    the time unit of the timeout
     * @return {@code true} if this {@code MessageBus} terminated,
     * {@code false} if the timeout elapsed before termination or
     * {@code false} if {@code close} was not yet called
     * @throws InterruptedException if interrupted while waiting
     */
    boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException;

    /**
     * Returns {@code true} if {@code close} has been called on this {@code MessageBus}.
     *
     * @return true, if a {@code close} was already called, or false otherwise
     */
    boolean isClosed();
}
