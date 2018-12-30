package com.envimate.messageMate.messageFunction.givenWhenThen;

import com.envimate.messageMate.messageFunction.MessageFunction;
import com.envimate.messageMate.messageFunction.testResponses.TestRequest;
import com.envimate.messageMate.messageFunction.testResponses.TestResponse;
import com.envimate.messageMate.qcec.shared.TestAction;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.qcec.shared.TestValidation;
import lombok.RequiredArgsConstructor;

import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.EXCEPTION;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.RESULT;
import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
public class Then {
    private final TestMessageFunctionBuilder testMessageFunctionBuilder;
    private final TestMessageFunctionActionBuilder testMessageFunctionActionBuilder;

    public void then(TestMessageFunctionValidationBuilder testMessageFunctionValidationBuilder) {
        final MessageFunction<TestRequest, TestResponse> messageFunction = testMessageFunctionBuilder.build();
        final TestEnvironment testEnvironment = testMessageFunctionBuilder.getTestEnvironment();
        final TestAction<MessageFunction<TestRequest, TestResponse>> testAction = testMessageFunctionActionBuilder.build();
        try {
            final Object result = testAction.execute(messageFunction, testEnvironment);
            if (result != null) {
                testEnvironment.setProperty(RESULT, result);
            }
        }catch (Exception e){
            testEnvironment.setProperty(EXCEPTION, e);
        }

        final TestValidation validation = testMessageFunctionValidationBuilder.build();
        validation.validate(testEnvironment);
    }
}
