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

package com.envimate.messageMate.useCases.shared;

import com.envimate.messageMate.processingContext.EventType;
import com.envimate.messageMate.useCases.payloadAndErrorPayload.PayloadAndErrorPayload;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static com.envimate.messageMate.useCases.payloadAndErrorPayload.PayloadAndErrorPayload.payloadAndErrorPayload;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class UseCaseBusCallBuilder {
    private Object data;
    private Class<?> payloadClass;
    private Class<?> errorPayloadClass;
    private PayloadAndErrorPayload<?, ?> expectedResult;
    private PayloadAndErrorPayload<Map<String, Object>, Map<String, Object>> notDeserializedExpectedResult;

    public static UseCaseBusCallBuilder aUseCasBusCall() {
        return new UseCaseBusCallBuilder();
    }

    public UseCaseBusCallBuilder withRequestData(final Object data) {
        this.data = data;
        return this;
    }

    public UseCaseBusCallBuilder withSuccessResponseClass(final Class<?> payloadClass) {
        this.payloadClass = payloadClass;
        return this;
    }

    public UseCaseBusCallBuilder withErrorResponseClass(final Class<?> errorPayloadClass) {
        this.errorPayloadClass = errorPayloadClass;
        return this;
    }

    public UseCaseBusCallBuilder expectOnlySuccessPayload(final Object expectedResult) {
        final PayloadAndErrorPayload<?, ?> payloadAndErrorPayload = payloadAndErrorPayload(expectedResult, null);
        return expectAsResult(payloadAndErrorPayload);
    }

    public UseCaseBusCallBuilder expectOnlyErrorPayload(final Object expectedErrorResult) {
        final PayloadAndErrorPayload<?, ?> payloadAndErrorPayload = payloadAndErrorPayload(null, expectedErrorResult);
        return expectAsResult(payloadAndErrorPayload);
    }

    public UseCaseBusCallBuilder expectAsResult(final PayloadAndErrorPayload<?, ?> expectedResult) {
        this.expectedResult = expectedResult;
        return this;
    }

    public UseCaseBusCallBuilder expectedSuccessPayloadNotDeserialized(final Consumer<Map<String, Object>> mapConsumer) {
        final Map<String, Object> successMap = new HashMap<>();
        mapConsumer.accept(successMap);
        final PayloadAndErrorPayload<Map<String, Object>, Map<String, Object>> payloadAndErrorPayload =
                payloadAndErrorPayload(successMap, null);
        return expectResultNotDeserialized(payloadAndErrorPayload);
    }

    public UseCaseBusCallBuilder expectedErrorPayloadNotDeserialized(final Consumer<Map<String, Object>> mapConsumer) {
        final Map<String, Object> errorMap = new HashMap<>();
        mapConsumer.accept(errorMap);
        final PayloadAndErrorPayload<Map<String, Object>, Map<String, Object>> payloadAndErrorPayload =
                payloadAndErrorPayload(null, errorMap);
        return expectResultNotDeserialized(payloadAndErrorPayload);
    }

    public UseCaseBusCallBuilder expectResultNotDeserialized(
            final PayloadAndErrorPayload<Map<String, Object>, Map<String, Object>> expectedResult) {
        this.notDeserializedExpectedResult = expectedResult;
        return this;
    }

    public UseCaseBusCall build(final EventType eventType) {
        return new UseCaseBusCall(eventType, data, payloadClass, errorPayloadClass, expectedResult,
                notDeserializedExpectedResult);
    }
}
