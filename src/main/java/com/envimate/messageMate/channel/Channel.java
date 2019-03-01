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
import com.envimate.messageMate.filtering.Filter;

import java.util.List;
import java.util.concurrent.TimeUnit;

public interface Channel<T> {

    void send(T message);

    void send(ProcessingContext<T> processingContext);

    void addPreFilter(Filter<ProcessingContext<T>> filter);

    void addPreFilter(Filter<ProcessingContext<T>> filter, int position);

    List<Filter<ProcessingContext<T>>> getPreFilter();

    void removePreFilter(Filter<ProcessingContext<T>> filter);

    void addProcessFilter(Filter<ProcessingContext<T>> filter);

    void addProcessFilter(Filter<ProcessingContext<T>> filter, int position);

    List<Filter<ProcessingContext<T>>> getProcessFilter();

    void removeProcessFilter(Filter<ProcessingContext<T>> filter);

    void addPostFilter(Filter<ProcessingContext<T>> filter);

    void addPostFilter(Filter<ProcessingContext<T>> filter, int position);

    List<Filter<ProcessingContext<T>>> getPostFilter();

    void removePostFilter(Filter<ProcessingContext<T>> filter);

    Action<T> getDefaultAction();

    ChannelStatusInformation getStatusInformation();

    void close(boolean finishRemainingTasks);

    boolean isShutdown();

    boolean awaitTermination(int timeout, TimeUnit timeUnit) throws InterruptedException;
}
