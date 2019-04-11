/*
 * Copyright (c) 2019 envimate GmbH - https://envimate.com/.
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

package com.envimate.messageMate.internal.pipe.config;

import com.envimate.messageMate.internal.pipe.PipeType;
import com.envimate.messageMate.internal.pipe.configuration.AsynchronousConfiguration;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static com.envimate.messageMate.internal.pipe.PipeType.ASYNCHRONOUS;
import static com.envimate.messageMate.internal.pipe.PipeType.SYNCHRONOUS;
import static com.envimate.messageMate.internal.pipe.configuration.AsynchronousConfiguration.constantPoolSizeAsynchronousPipeConfiguration;

@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class PipeTestConfig {
    public static final int ASYNCHRONOUS_POOL_SIZE = 5;
    public static final int ASYNCHRONOUS_QUEUED_BOUND = 3;
    @Getter
    private final PipeType pipeType;
    @Getter
    private final AsynchronousConfiguration asynchronousConfiguration;

    static PipeTestConfig aSynchronousPipe() {
        return new PipeTestConfig(SYNCHRONOUS, null);
    }

    static PipeTestConfig anAsynchronousPipe() {
        return new PipeTestConfig(ASYNCHRONOUS, constantPoolSizeAsynchronousPipeConfiguration(ASYNCHRONOUS_POOL_SIZE));
    }

    public static PipeTestConfig anAsynchronousBoundedPipe() {
        final int poolSize = ASYNCHRONOUS_POOL_SIZE;
        final int waitingQueueBound = ASYNCHRONOUS_QUEUED_BOUND;
        final AsynchronousConfiguration config = constantPoolSizeAsynchronousPipeConfiguration(poolSize, waitingQueueBound);
        return new PipeTestConfig(ASYNCHRONOUS, config);
    }
}
