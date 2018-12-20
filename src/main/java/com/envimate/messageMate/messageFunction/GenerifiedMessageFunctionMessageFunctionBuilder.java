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

package com.envimate.messageMate.messageFunction;

import com.envimate.messageMate.correlation.CorrelationId;
import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.messageFunction.building.*;
import com.envimate.messageMate.messageFunction.correlationIdExtracting.CorrelationIdExtractor;
import com.envimate.messageMate.messageFunction.requestResponseRelation.RequestResponseRelationMap;
import com.envimate.messageMate.messageFunction.responseHandling.ResponseHandlingSubscriber;
import lombok.NonNull;

import java.util.function.Function;

import static com.envimate.messageMate.messageFunction.MessageFunctionImpl.messageFunction;
import static com.envimate.messageMate.messageFunction.correlationIdExtracting.CorrelationIdExtractor.correlationIdExtractor;
import static com.envimate.messageMate.messageFunction.requestResponseRelation.RequestResponseRelationMapFactory.aRequestResponseRelationMap;
import static com.envimate.messageMate.messageFunction.responseHandling.ResponseHandlingSubscriberFactory.responseHandlingSubscriber;

class GenerifiedMessageFunctionMessageFunctionBuilder<R, S> implements Step4MessageFunctionBuilder<R, S>,
        Step4RequestAnswerStep1MessageFunctionBuilder<R, S>,
        Step4RequestAnswerStep2MessageFunctionBuilder<R, S>,
        Step5RequestCorrelationIdMessageFunctionBuilder<R, S>,
        Step6ResponseCorrelationIdMessageFunctionBuilder<R, S>,
        Step7UsingMessageBusMessageFunctionBuilder<R, S>,
        Step8FinalMessageFunctionBuilder<R, S> {

    private final Class<R> requestClass;
    private final Class<S> responseClass;
    private final CorrelationIdExtractor<R> requestCorrelationIdExtractor;
    private final CorrelationIdExtractor<S> responseCorrelationIdExtractor;
    private final RequestResponseRelationMap<R, S> requestResponseRelationMap;
    private Class<R> currentHandledResponse;
    private MessageBus messageBus;

    GenerifiedMessageFunctionMessageFunctionBuilder(final Class<R> requestClass, final Class<S> responseClass) {
        this.requestClass = requestClass;
        this.responseClass = responseClass;
        requestCorrelationIdExtractor = correlationIdExtractor();
        responseCorrelationIdExtractor = correlationIdExtractor();
        requestResponseRelationMap = aRequestResponseRelationMap(requestCorrelationIdExtractor, responseCorrelationIdExtractor);
    }

    @Override
    public <U extends R> Step4RequestAnswerStep1MessageFunctionBuilder<R, S> with(final Class<U> requestClass) {
        currentHandledResponse = (Class<R>) requestClass;
        return this;
    }

    @Override
    public <U extends S> Step4RequestAnswerStep2MessageFunctionBuilder<R, S> answeredBy(final Class<U> responseClass) {
        requestResponseRelationMap.addSuccessResponse(currentHandledResponse, (Class<S>) responseClass);
        return this;
    }

    @Override
    public <U extends S> Step4RequestAnswerStep2MessageFunctionBuilder<R, S> or(final Class<U> responseClass) {
        requestResponseRelationMap.addSuccessResponse(currentHandledResponse, (Class<S>) responseClass);
        return this;
    }

    @Override
    public <U extends S> Step4RequestAnswerStep2MessageFunctionBuilder<R, S> orByError(final Class<U> responseClass) {
        requestResponseRelationMap.addErrorResponse(currentHandledResponse, (Class<S>) responseClass);
        return this;
    }

    @Override
    public <U extends S> Step5RequestCorrelationIdMessageFunctionBuilder<R, S> withGeneralErrorResponse(
            final Class<U> generalErrorResponse) {
        requestResponseRelationMap.addGeneralErrorResponse((Class<S>) generalErrorResponse);
        return this;
    }

    @Override
    public <U extends R> Step6ResponseCorrelationIdMessageFunctionBuilder<R, S> obtainingCorrelationIdsOfRequestsWith(
            final Function<U, CorrelationId> consumer) {
        requestCorrelationIdExtractor.addExtraction(requestClass, message -> consumer.apply((U) message));
        return this;
    }

    @Override
    public <U extends S> Step7UsingMessageBusMessageFunctionBuilder<R, S> obtainingCorrelationIdsOfResponsesWith(
            final Function<U, CorrelationId> consumer) {
        responseCorrelationIdExtractor.addExtraction(responseClass, message -> consumer.apply((U) message));
        return this;
    }

    @Override
    public Step8FinalMessageFunctionBuilder<R, S> usingMessageBus(@NonNull final MessageBus messageBus) {
        this.messageBus = messageBus;
        return this;
    }

    @Override
    public MessageFunction<R, S> build() {
        final ResponseHandlingSubscriber<S> responseHandlingSubscriber = responseHandlingSubscriber();
        return messageFunction(messageBus, responseHandlingSubscriber, requestResponseRelationMap);
    }
}