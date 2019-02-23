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

package com.envimate.messageMate.pipe.statistics;

import lombok.*;

import java.math.BigInteger;
import java.util.Date;

import static lombok.AccessLevel.PRIVATE;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = PRIVATE)
public final class PipeStatistics {
    @Getter
    private final Date timestamp;
    @Getter
    private final BigInteger acceptedMessages;
    @Getter
    private final BigInteger queuedMessages;
    @Getter
    private final BigInteger successfulMessages;
    @Getter
    private final BigInteger failedMessages;

    public static PipeStatistics pipeStatistics(@NonNull final Date timestamp,
                                                @NonNull final BigInteger acceptedMessages,
                                                @NonNull final BigInteger queuedMessages,
                                                @NonNull final BigInteger successfulMessages,
                                                @NonNull final BigInteger failedMessages) {
        return new PipeStatistics(timestamp, acceptedMessages, queuedMessages, successfulMessages, failedMessages);
    }

}
