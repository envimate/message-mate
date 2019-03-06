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

package com.envimate.messageMate.messageFunction.givenWhenThen;

import com.envimate.messageMate.correlation.CorrelationId;
import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.messageBus.MessageBusBuilder;
import com.envimate.messageMate.messageBus.givenWhenThen.MessageBusTestExceptionHandler;
import com.envimate.messageMate.messageFunction.MessageFunction;
import com.envimate.messageMate.messageFunction.MessageFunctionBuilder;
import com.envimate.messageMate.messageFunction.internal.building.Step3MessageFunctionBuilder;
import com.envimate.messageMate.messageFunction.internal.building.Step8UsingMessageBusMessageFunctionBuilder;
import com.envimate.messageMate.messageFunction.internal.building.Step9FinalMessageFunctionBuilder;
import com.envimate.messageMate.messageFunction.testResponses.*;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.shared.subscriber.TestException;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import static com.envimate.messageMate.messageBus.MessageBusType.ASYNCHRONOUS;
import static com.envimate.messageMate.messageFunction.testResponses.ErrorTestResponse.errorTestResponse;
import static com.envimate.messageMate.pipe.configuration.AsynchronousConfiguration.constantPoolSizeAsynchronousPipeConfiguration;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.EXPECTED_RESULT;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.MOCK;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class TestMessageFunctionSetupBuilder {
    private final TestEnvironment testEnvironment = TestEnvironment.emptyTestEnvironment();
    private final MessageBusBuilder messageBusBuilder = MessageBusBuilder.aMessageBus()
            .forType(ASYNCHRONOUS)
            .withAsynchronousConfiguration(constantPoolSizeAsynchronousPipeConfiguration(5));
    private final Step3MessageFunctionBuilder<TestRequest, TestResponse> messageFunctionBuilder;
    private final List<Consumer<MessageBus>> setupActions = new LinkedList<>();
    private Step8UsingMessageBusMessageFunctionBuilder<TestRequest, TestResponse> finalBuilder;

    public static TestMessageFunctionSetupBuilder aMessageFunction() {
        final Step3MessageFunctionBuilder<TestRequest, TestResponse> step3MessageFunctionBuilder = MessageFunctionBuilder.aMessageFunction()
                .forRequestType(TestRequest.class)
                .forResponseType(TestResponse.class);
        return new TestMessageFunctionSetupBuilder(step3MessageFunctionBuilder);
    }

    public TestMessageFunctionSetupBuilder definedWithARequestResponseMapping() {
        finalBuilder = messageFunctionBuilder.with(SimpleTestRequest.class)
                .answeredBy(SimpleTestResponse.class)
                .obtainingCorrelationIdsOfRequestsWith(TestRequest::getCorrelationId)
                .obtainingCorrelationIdsOfResponsesWith(TestResponse::getCorrelationId);
        setupActions.add(messageBus -> messageBus.subscribe(SimpleTestRequest.class, simpleTestRequest -> {
            final CorrelationId correlationId = simpleTestRequest.getCorrelationId();
            final SimpleTestResponse simpleTestResponse = SimpleTestResponse.testResponse(correlationId);
            messageBus.send(simpleTestResponse);
        }));
        return this;
    }

    public TestMessageFunctionSetupBuilder acceptingTwoDifferentResponsesForTheTestRequest() {
        finalBuilder = messageFunctionBuilder.with(SimpleTestRequest.class)
                .answeredBy(SimpleTestResponse.class).or(AlternativTestResponse.class)
                .obtainingCorrelationIdsOfRequestsWith(TestRequest::getCorrelationId)
                .obtainingCorrelationIdsOfResponsesWith(TestResponse::getCorrelationId);
        return this;
    }

    public TestMessageFunctionSetupBuilder acceptingErrorResponses() {
        finalBuilder = messageFunctionBuilder.with(SimpleTestRequest.class)
                .answeredBy(SimpleTestResponse.class).orByError(ErrorTestResponse.class)
                .obtainingCorrelationIdsOfRequestsWith(TestRequest::getCorrelationId)
                .obtainingCorrelationIdsOfResponsesWith(TestResponse::getCorrelationId);
        setupActions.add(messageBus -> messageBus.subscribe(SimpleTestRequest.class, simpleTestRequest -> {
            final CorrelationId correlationId = simpleTestRequest.getCorrelationId();
            final ErrorTestResponse errorTestResponse = errorTestResponse(correlationId);
            messageBus.send(errorTestResponse);
        }));
        return this;
    }

    public TestMessageFunctionSetupBuilder acceptingGeneralErrorResponses() {
        finalBuilder = messageFunctionBuilder.with(SimpleTestRequest.class)
                .answeredBy(SimpleTestResponse.class)
                .withGeneralErrorResponse(ErrorTestResponse.class)
                .obtainingCorrelationIdsOfRequestsWith(TestRequest::getCorrelationId)
                .obtainingCorrelationIdsOfResponsesWith(TestResponse::getCorrelationId);
        setupActions.add(messageBus -> messageBus.subscribe(SimpleTestRequest.class, simpleTestRequest -> {
            final CorrelationId correlationId = simpleTestRequest.getCorrelationId();
            final ErrorTestResponse errorTestResponse = errorTestResponse(correlationId);
            messageBus.send(errorTestResponse);
        }));
        return this;
    }

    public TestMessageFunctionSetupBuilder withAGeneralErrorResponseSendTwice() {
        finalBuilder = messageFunctionBuilder.with(SimpleTestRequest.class)
                .answeredBy(SimpleTestResponse.class)
                .withGeneralErrorResponse(ErrorTestResponse.class)
                .obtainingCorrelationIdsOfRequestsWith(TestRequest::getCorrelationId)
                .obtainingCorrelationIdsOfResponsesWith(TestResponse::getCorrelationId);
        setupActions.add(messageBus -> messageBus.subscribe(SimpleTestRequest.class, simpleTestRequest -> {
            final CorrelationId correlationId = simpleTestRequest.getCorrelationId();
            final ErrorTestResponse firstResponse = errorTestResponse(correlationId);
            messageBus.send(firstResponse);
            testEnvironment.setProperty(EXPECTED_RESULT, firstResponse);
            final ErrorTestResponse secondErrorResponse = errorTestResponse(correlationId);
            messageBus.send(secondErrorResponse);
        }));
        return this;
    }

    public TestMessageFunctionSetupBuilder acceptingGeneralErrorResponsesWithCondition() {
        finalBuilder = messageFunctionBuilder.with(SimpleTestRequest.class)
                .answeredBy(SimpleTestResponse.class)
                .withGeneralErrorResponse(ErrorTestResponse.class, (errorTestResponse, testRequest) -> errorTestResponse.getCorrelationId().equals(testRequest.getCorrelationId()))
                .obtainingCorrelationIdsOfRequestsWith(TestRequest::getCorrelationId)
                .obtainingCorrelationIdsOfResponsesWith(TestResponse::getCorrelationId);
        setupActions.add(messageBus -> messageBus.subscribe(SimpleTestRequest.class, simpleTestRequest -> {
            final CorrelationId differentCorrelationId = CorrelationId.newUniqueId();
            final ErrorTestResponse notMatchingResponse = errorTestResponse(differentCorrelationId);
            messageBus.send(notMatchingResponse);
            final CorrelationId correlationId = simpleTestRequest.getCorrelationId();
            final ErrorTestResponse matchingResponse = errorTestResponse(correlationId);
            testEnvironment.setProperty(EXPECTED_RESULT, matchingResponse);
            messageBus.send(matchingResponse);
        }));
        return this;
    }

    public TestMessageFunctionSetupBuilder definedWithAnUnansweredResponse() {
        finalBuilder = messageFunctionBuilder.with(SimpleTestRequest.class)
                .answeredBy(SimpleTestResponse.class)
                .obtainingCorrelationIdsOfRequestsWith(TestRequest::getCorrelationId)
                .obtainingCorrelationIdsOfResponsesWith(TestResponse::getCorrelationId);
        setupActions.add(messageBus -> messageBus.subscribe(SimpleTestRequest.class, simpleTestRequest -> {
        }));
        return this;
    }

    public TestMessageFunctionSetupBuilder definedWithResponseThrowingAnException() {
        finalBuilder = messageFunctionBuilder.with(SimpleTestRequest.class)
                .answeredBy(SimpleTestResponse.class)
                .obtainingCorrelationIdsOfRequestsWith(TestRequest::getCorrelationId)
                .obtainingCorrelationIdsOfResponsesWith(TestResponse::getCorrelationId);
        messageBusBuilder.withExceptionHandler(MessageBusTestExceptionHandler.allExceptionHandlingTestExceptionHandler(testEnvironment));
        setupActions.add(messageBus -> messageBus.subscribe(SimpleTestRequest.class, simpleTestRequest -> {
            throw new RuntimeException("Expected exception in subcriber");
        }));
        return this;
    }

    public TestMessageFunctionSetupBuilder withFulfillingResponseSendTwice() {
        finalBuilder = messageFunctionBuilder.with(SimpleTestRequest.class)
                .answeredBy(SimpleTestResponse.class)
                .obtainingCorrelationIdsOfRequestsWith(TestRequest::getCorrelationId)
                .obtainingCorrelationIdsOfResponsesWith(TestResponse::getCorrelationId);

        setupActions.add(messageBus -> messageBus.subscribe(SimpleTestRequest.class, simpleTestRequest -> {
            final CorrelationId correlationId = simpleTestRequest.getCorrelationId();
            final SimpleTestResponse simpleTestResponse1 = SimpleTestResponse.testResponse(correlationId);
            testEnvironment.setProperty(EXPECTED_RESULT, simpleTestResponse1);
            messageBus.send(simpleTestResponse1);
            final SimpleTestResponse simpleTestResponse2 = SimpleTestResponse.testResponse(correlationId);
            messageBus.send(simpleTestResponse2);
        }));
        return this;
    }

    public TestMessageFunctionSetupBuilder withRequestAnsweredByResponseThenByException() {
        finalBuilder = messageFunctionBuilder.with(SimpleTestRequest.class)
                .answeredBy(SimpleTestResponse.class)
                .obtainingCorrelationIdsOfRequestsWith(TestRequest::getCorrelationId)
                .obtainingCorrelationIdsOfResponsesWith(TestResponse::getCorrelationId);

        messageBusBuilder.withExceptionHandler(MessageBusTestExceptionHandler.allExceptionIgnoringExceptionHandler());
        setupActions.add(messageBus -> messageBus.subscribe(SimpleTestRequest.class, simpleTestRequest -> {
            final CorrelationId correlationId = simpleTestRequest.getCorrelationId();
            final SimpleTestResponse simpleTestResponse = SimpleTestResponse.testResponse(correlationId);
            messageBus.send(simpleTestResponse);
            testEnvironment.setProperty(EXPECTED_RESULT, simpleTestResponse);
            throw new TestException();
        }));
        return this;
    }

    public TestMessageFunctionSetupBuilder withRequestAnsweredByExceptionThenByMessage() {
        finalBuilder = messageFunctionBuilder.with(SimpleTestRequest.class)
                .answeredBy(SimpleTestResponse.class)
                .obtainingCorrelationIdsOfRequestsWith(TestRequest::getCorrelationId)
                .obtainingCorrelationIdsOfResponsesWith(TestResponse::getCorrelationId);

        messageBusBuilder.withExceptionHandler(MessageBusTestExceptionHandler.allExceptionIgnoringExceptionHandler());
        setupActions.add(messageBus -> messageBus.subscribe(SimpleTestRequest.class, simpleTestRequest -> {
            try {
                throw new TestException();
            } finally {
                final CorrelationId correlationId = simpleTestRequest.getCorrelationId();
                final SimpleTestResponse simpleTestResponse = SimpleTestResponse.testResponse(correlationId);
                messageBus.send(simpleTestResponse);
                testEnvironment.setProperty(EXPECTED_RESULT, simpleTestResponse);
            }
        }));
        return this;
    }

    public TestEnvironment getTestEnvironment() {
        return testEnvironment;
    }

    public MessageFunction<TestRequest, TestResponse> build() {
        final MessageBus messageBus = messageBusBuilder.build();
        setupActions.forEach(f -> f.accept(messageBus));
        final Step9FinalMessageFunctionBuilder<TestRequest, TestResponse> builder = finalBuilder.usingMessageBus(messageBus);
        testEnvironment.setProperty(MOCK, messageBus);
        return builder.build();
    }
}
