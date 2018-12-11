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

package com.envimate.messageMate.internal.statistics;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.math.BigInteger;
import java.util.Date;

import static lombok.AccessLevel.PRIVATE;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = PRIVATE)
public final class MessageStatistics {

    private final Date timestamp;
    private final BigInteger acceptedMessages;
    private final BigInteger successfulMessages;
    private final BigInteger failedMessages;
    private final BigInteger droppedMessages;
    private final BigInteger replacedMessages;
    private final BigInteger forgottenMessages;
    private final BigInteger waitingMessages;
    private final BigInteger currentlyTransportedMessages;
    private final BigInteger currentlyDeliveredMessages;

    public static MessageStatistics messageStatistics(@NonNull final Date timestamp,
                                                      @NonNull final BigInteger acceptedMessages,
                                                      @NonNull final BigInteger successfulMessages,
                                                      @NonNull final BigInteger failedMessages,
                                                      @NonNull final BigInteger droppedMessages,
                                                      @NonNull final BigInteger replacedMessages,
                                                      @NonNull final BigInteger forgottenMessages,
                                                      @NonNull final BigInteger waitingMessages,
                                                      @NonNull final BigInteger currentlyTransportedMessages,
                                                      @NonNull final BigInteger currentlyDeliveredMessages) {
        return new MessageStatistics(timestamp, acceptedMessages, successfulMessages, failedMessages, droppedMessages,
                replacedMessages, forgottenMessages, waitingMessages, currentlyTransportedMessages, currentlyDeliveredMessages);
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public BigInteger getAcceptedMessages() {
        return acceptedMessages;
    }

    public BigInteger getSuccessfulMessages() {
        return successfulMessages;
    }

    public BigInteger getFailedMessages() {
        return failedMessages;
    }

    public BigInteger getDroppedMessages() {
        return droppedMessages;
    }

    public BigInteger getReplacedMessages() {
        return replacedMessages;
    }

    public BigInteger getForgottenMessages() {
        return forgottenMessages;
    }

    public BigInteger getWaitingMessages() {
        return waitingMessages;
    }

    public BigInteger getCurrentlyTransportedMessages() {
        return currentlyTransportedMessages;
    }

    public BigInteger getCurrentlyDeliveredMessages() {
        return currentlyDeliveredMessages;
    }
}
