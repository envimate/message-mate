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

package com.envimate.messageMate.shared.pipeMessageBus.givenWhenThen;

import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class PipeChannelMessageBusSharedTestProperties {
    public static final String SINGLE_SEND_MESSAGE = "SINGLE_SEND_MESSAGE";
    public static final String EXPECTED_RECEIVERS = "EXPECTED_RECEIVERS";
    public static final String INITIAL_SUBSCRIBER = "INITIAL_SUBSCRIBER";
    public static final String EXPECTED_SUBSCRIBER = "EXPECTED_SUBSCRIBER";
    public static final String POTENTIAL_RECEIVERS = "POTENTIAL_RECEIVERS";
    public static final String SINGLE_RECEIVER = "SINGLE_RECEIVER";
    public static final String MESSAGES_SEND = "MESSAGES_SEND";
    public static final String MESSAGES_SEND_OF_INTEREST = "MESSAGES_SEND_OF_INTEREST";
    public static final String EXPECTED_CHANGED_CONTENT = "EXPECTED_CHANGED_CONTENT";
    public static final String EXECUTION_END_SEMAPHORE = "EXECUTION_END_SEMAPHORE";
    public static final String ERROR_SUBSCRIBER = "ERROR_SUBSCRIBER";
    public static final String EXPECTED_FILTER = "EXPECTED_FILTER";
    public static final String SLEEP_BETWEEN_EXECUTION_STEPS = "SLEEP_BETWEEN_EXECUTION_STEPS";
    public static final String SLEEP_AFTER_EXECUTION = "SLEEP_AFTER_EXECUTION";
    public static final String USED_SUBSCRIPTION_ID = "USED_SUBSCRIPTION_ID";
    public static final String SEND_MESSAGE_ID = "SEND_MESSAGE_ID";
    public static final String EXPECTED_CORRELATION_ID = "EXPECTED_CORRELATION_ID";
    public static final String EXCEPTION_OCCURRED_INSIDE_FILTER = "EXCEPTION_OCCURRED_INSIDE_FILTER";
    public static final String EXCEPTION_OCCURRED_DURING_DELIVERY = "EXCEPTION_OCCURRED_DURING_DELIVERY";
    public static final String EXPECTED_AND_IGNORED_EXCEPTION = "EXPECTED_AND_IGNORED_EXCEPTION";
}
