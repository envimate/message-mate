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

package com.envimate.messageMate.internal.pipe.statistics;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.math.BigInteger;
import java.util.Date;

import static lombok.AccessLevel.PRIVATE;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = PRIVATE)
public final class PipeStatistics {
    private final Date timestamp;
    @Getter
    private final BigInteger acceptedMessages;
    @Getter
    private final BigInteger queuedMessages;
    @Getter
    private final BigInteger successfulMessages;
    @Getter
    private final BigInteger failedMessages;

    public static PipeStatistics pipeStatistics(final Date timestamp,
                                                final BigInteger acceptedMessages,
                                                final BigInteger queuedMessages,
                                                final BigInteger successfulMessages,
                                                final BigInteger failedMessages) {
        return new PipeStatistics(timestamp, acceptedMessages, queuedMessages, successfulMessages, failedMessages);
    }

    public Date getTimestamp() {
        final long copyForSafeSharing = timestamp.getTime();
        return new Date(copyForSafeSharing);
    }

}
