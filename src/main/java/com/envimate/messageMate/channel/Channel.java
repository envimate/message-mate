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

package com.envimate.messageMate.channel;

import com.envimate.messageMate.channel.action.Action;
import com.envimate.messageMate.error.AlreadyClosedException;
import com.envimate.messageMate.filtering.Filter;
import com.envimate.messageMate.internal.autoclosable.NoErrorAutoClosable;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Channel is the concept used for transporting messages from sender to an consuming action at the end of the channel.
 *
 * <p>Each channel has a default action, which, if not changed by filter, is executed for every message at the end of the
 * transport. Different action exists, that allow to add subscriber, execute specific logic or move the message to different
 * channels. During the transport filter can be added, that alter the message, its flow or the action.
 * Channels can be synchronous or asynchronous. Synchronous channel execute the transport on the Thread calling {@code send}.
 * Asynchronous channels provide their own Threads and mechanism to queue messages, for which no Threads is available right away.
 * Messages collect statistics over messages, that can be queried anytime. During creation exception handler can be set, that
 * control the channel's behavior, when an exception is thrown.</p>
 *
 * <p>The channel implements the {@code AutoCloseable} interface, so that it can be used in try-with-resource statements.</p>
 *
 * @param <T> the type of messages send over this channel
 *
 * @see ChannelBuilder
 */
public interface Channel<T> extends NoErrorAutoClosable {

    /**
     * Send the given message over this channel.
     *
     * @param message the send message
     * @throws AlreadyClosedException if the channel is already closed
     */
    void send(T message);

    /**
     * Send the given processingContext object over this channel.
     *
     * <p>Channels use ProcessingContext objects internally to store and share processing relevant information. Examples
     * are a shared key-value map or the history of past channels. In case several channels are logical connected and the
     * information and history should be kept, channels can accept the ProcessingContext object of the previous channel directly.
     * </p>
     *
     * @param processingContext
     * @throws AlreadyClosedException if the channel is already closed
     */
    void send(ProcessingContext<T> processingContext);

    /**
     * Adds the filter to the list of pre filter.
     *
     * <p>Each channel has three points, where filter can be added: pre, process and post. All pre filter will always be
     * executed before the first process filter. The same goes for process and post filter.</p>
     *
     * @param filter the filter to be added
     */
    void addPreFilter(Filter<ProcessingContext<T>> filter);

    /**
     * Adds the filter at the given position to the list of pre filter.
     *
     * <p>Each channel has three points, where filter can be added: pre, process and post. All pre filter will always be
     * executed before the first process filter. The same goes for process and post filter.</p>
     *
     * @param filter   the filter to be added
     * @param position the position of the filter
     * @throws ArrayIndexOutOfBoundsException if the position is higher than the number of filters or negative
     */
    void addPreFilter(Filter<ProcessingContext<T>> filter, int position);

    /**
     * Returns a list of all filter registered in the pre list.
     *
     * @return list of filter in the pre position
     */
    List<Filter<ProcessingContext<T>>> getPreFilter();

    /**
     * Removes the filter from the pre list.
     *
     * @param filter the filter to be removed
     */
    void removePreFilter(Filter<ProcessingContext<T>> filter);

    /**
     * Adds the filter to the list of process filter.
     *
     * <p>Each channel has three points, where filter can be added: pre, process and post. All pre filter will always be
     * executed before the first process filter. The same goes for process and post filter.</p>
     *
     * @param filter the filter to be added
     */
    void addProcessFilter(Filter<ProcessingContext<T>> filter);

    /**
     * Adds the filter at the given position to the list of process filter.
     *
     * <p>Each channel has three points, where filter can be added: pre, process and post. All pre filter will always be
     * executed before the first process filter. The same goes for process and post filter.</p>
     *
     * @param filter   the filter to be added
     * @param position the position of the filter
     * @throws ArrayIndexOutOfBoundsException if the position is higher than the number of filters or negative
     */
    void addProcessFilter(Filter<ProcessingContext<T>> filter, int position);

    /**
     * Returns a list of all filter registered in the process list.
     *
     * @return list of filter in the process position
     */
    List<Filter<ProcessingContext<T>>> getProcessFilter();

    /**
     * Removes the filter from the process list.
     *
     * @param filter the filter to be removed
     */
    void removeProcessFilter(Filter<ProcessingContext<T>> filter);

    /**
     * Adds the filter to the list of post filter.
     *
     * <p>Each channel has three points, where filter can be added: pre, process and post. All pre filter will always be
     * executed before the first process filter. The same goes for process and post filter.</p>
     *
     * @param filter the filter to be added
     */
    void addPostFilter(Filter<ProcessingContext<T>> filter);

    /**
     * Adds the filter at the given position to the list of post filter.
     *
     * <p>Each channel has three points, where filter can be added: pre, process and post. All pre filter will always be
     * executed before the first process filter. The same goes for process and post filter.</p>
     *
     * @param filter   the filter to be added
     * @param position the position of the filter
     * @throws ArrayIndexOutOfBoundsException if the position is higher than the number of filters or negative
     */
    void addPostFilter(Filter<ProcessingContext<T>> filter, int position);

    /**
     * Returns a list of all filter registered in the post list.
     *
     * @return list of filter in the post position
     */
    List<Filter<ProcessingContext<T>>> getPostFilter();
    /**
     * Removes the filter from the post list.
     *
     * @param filter the filter to be removed
     */
    void removePostFilter(Filter<ProcessingContext<T>> filter);

    /**
     * Returns the default action of this channel.
     *
     * @return the default action of this channel
     */
    Action<T> getDefaultAction();

    /**
     * Returns a ChannelStatusInformation object, which can be used to query the channel's statistics.
     *
     * @return a ChannelStatusInformation object
     */
    ChannelStatusInformation getStatusInformation();

    /**
     * Closes the channel so that the channel shutdowns.
     *
     * <p>When setting the parameter to true, the channel tries to finish remaining tasks, that are still pending. Setting the
     * parameter to false instructs the channel to shutdown immediately. It is not defined how unfinished tasks should be handled.
     * Independent of the parameter, the channel will be closed. All tries to send messages will result in exceptions.</p>
     *
     * @param finishRemainingTasks boolean flag indicating, whether the channel should try to finish pending tasks
     */
    void close(boolean finishRemainingTasks);

    /**
     * Returns {@code true} if this {@code close} has been called on this channel.
     *
     * @return true, if a {@code close} was already called, or false otherwise
     */
    boolean isClosed();

    /**
     * Blocks the caller until all remainings tasks have completed execution after a close has been called, or the timeout occurs,
     * or the current thread is interrupted.
     *
     * @param timeout the duration to wait
     * @param timeUnit the time unit of the timeout
     * @return {@code true} if this executor terminated and
     *         {@code false} if the timeout elapsed before termination
     *         {@code false} if close was not yet called
     * @throws InterruptedException if interrupted while waiting
     */
    boolean awaitTermination(int timeout, TimeUnit timeUnit) throws InterruptedException;
}
