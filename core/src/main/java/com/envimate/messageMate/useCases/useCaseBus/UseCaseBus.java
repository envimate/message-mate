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

package com.envimate.messageMate.useCases.useCaseBus;

import com.envimate.messageMate.messageBus.EventType;
import com.envimate.messageMate.messageBus.PayloadAndErrorPayload;
import com.envimate.messageMate.serializedMessageBus.SerializedMessageBus;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public interface UseCaseBus {

    static UseCaseBus useCaseBus(final SerializedMessageBus serializedMessageBus) {
        return new UseCaseBusImpl(serializedMessageBus);
    }

    <P, E> PayloadAndErrorPayload<P, E> invokeAndWait(
            EventType eventType,
            Object data,
            Class<P> payloadClass,
            Class<E> errorPayloadClass) throws InterruptedException, ExecutionException;

    <P, E> PayloadAndErrorPayload<P, E> invokeAndWait(
            EventType eventType,
            Object data,
            Class<P> payloadClass,
            Class<E> errorPayloadClass,
            long timeout,
            TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException;

    PayloadAndErrorPayload<Map<String, Object>, Map<String, Object>> invokeAndWaitNotDeserialized(
            EventType eventType,
            Object data) throws InterruptedException, ExecutionException;

    PayloadAndErrorPayload<Map<String, Object>, Map<String, Object>> invokeAndWaitNotDeserialized(
            EventType eventType,
            Object data,
            long timeout,
            TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException;

}
