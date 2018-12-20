package com.envimate.messageMate.messageFunction;

import com.envimate.messageMate.correlation.CorrelationId;
import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.messageBus.MessageBusBuilder;
import com.envimate.messageMate.messageFunction.building.Step4MessageFunctionBuilder;
import com.envimate.messageMate.messageFunction.building.Step8FinalMessageFunctionBuilder;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import lombok.RequiredArgsConstructor;

import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.MOCK;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class TestMessageFunctionBuilder {
    private final TestEnvironment testEnvironment = TestEnvironment.emptyTestEnvironment();
    private final MessageBus messageBus = MessageBusBuilder.aMessageBus().build();
    private final Step4MessageFunctionBuilder<TestRequest, TestResponse> messageFunctionBuilder;
    private Step8FinalMessageFunctionBuilder<TestRequest, TestResponse> finalBuilder;

    public static TestMessageFunctionBuilder aMessageFunction() {
        final Step4MessageFunctionBuilder<TestRequest, TestResponse> step4MessageFunctionBuilder = MessageFunctionBuilder.aMessageFunction()
                .forRequestType(TestRequest.class)
                .forResponseType(TestResponse.class);
        return new TestMessageFunctionBuilder(step4MessageFunctionBuilder);
    }

    public TestMessageFunctionBuilder definedWithARequestResponseMapping() {
        finalBuilder = messageFunctionBuilder.with(SimpleTestRequest.class)
                .answeredBy(SimpleTestResponse.class)
                .obtainingCorrelationIdsOfRequestsWith(TestRequest::getCorrelationId)
                .obtainingCorrelationIdsOfResponsesWith(TestResponse::getCorrelationId)
                .usingMessageBus(messageBus);
        messageBus.subscribe(SimpleTestRequest.class, simpleTestRequest -> {
            final CorrelationId correlationId = simpleTestRequest.getCorrelationId();
            final SimpleTestResponse simpleTestResponse = SimpleTestResponse.testResponse(correlationId);
            messageBus.send(simpleTestResponse);
        });
        return this;
    }

    public TestMessageFunctionBuilder acceptingTwoDifferentResponsesForTheTestRequest() {
        finalBuilder = messageFunctionBuilder.with(SimpleTestRequest.class)
                .answeredBy(SimpleTestResponse.class).or(AlternativTestResponse.class)
                .obtainingCorrelationIdsOfRequestsWith(TestRequest::getCorrelationId)
                .obtainingCorrelationIdsOfResponsesWith(TestResponse::getCorrelationId)
                .usingMessageBus(messageBus);
        return this;
    }

    public TestMessageFunctionBuilder acceptingErrorResponses() {
        finalBuilder = messageFunctionBuilder.with(SimpleTestRequest.class)
                .answeredBy(SimpleTestResponse.class).orByError(ErrorTestResponse.class)
                .obtainingCorrelationIdsOfRequestsWith(TestRequest::getCorrelationId)
                .obtainingCorrelationIdsOfResponsesWith(TestResponse::getCorrelationId)
                .usingMessageBus(messageBus);
        messageBus.subscribe(SimpleTestRequest.class, simpleTestRequest -> {
            final CorrelationId correlationId = simpleTestRequest.getCorrelationId();
            final ErrorTestResponse errorTestResponse = ErrorTestResponse.errorTestResponse(correlationId);
            messageBus.send(errorTestResponse);
        });
        return this;
    }

    public TestMessageFunctionBuilder acceptingGeneralErrorResponses() {
        finalBuilder = messageFunctionBuilder.with(SimpleTestRequest.class)
                .answeredBy(SimpleTestResponse.class)
                .withGeneralErrorResponse(ErrorTestResponse.class)
                .obtainingCorrelationIdsOfRequestsWith(TestRequest::getCorrelationId)
                .obtainingCorrelationIdsOfResponsesWith(TestResponse::getCorrelationId)
                .usingMessageBus(messageBus);
        messageBus.subscribe(SimpleTestRequest.class, simpleTestRequest -> {
            final CorrelationId correlationId = simpleTestRequest.getCorrelationId();
            final ErrorTestResponse errorTestResponse = ErrorTestResponse.errorTestResponse(correlationId);
            messageBus.send(errorTestResponse);
        });
        return this;
    }

    public TestMessageFunctionBuilder definedWithAnUnansweredResponse() {
        finalBuilder = messageFunctionBuilder.with(SimpleTestRequest.class)
                .answeredBy(SimpleTestResponse.class)
                .obtainingCorrelationIdsOfRequestsWith(TestRequest::getCorrelationId)
                .obtainingCorrelationIdsOfResponsesWith(TestResponse::getCorrelationId)
                .usingMessageBus(messageBus);
        return this;
    }

    public TestEnvironment getTestEnvironment() {
        return testEnvironment;
    }

    public MessageFunction<TestRequest, TestResponse> build() {
        testEnvironment.setProperty(MOCK, messageBus);
        return finalBuilder.build();
    }
}
