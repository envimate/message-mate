package com.envimate.messageMate.messageFunction;

import org.junit.jupiter.api.Test;

import static com.envimate.messageMate.messageFunction.Given.given;
import static com.envimate.messageMate.messageFunction.TestMessageFunctionActionBuilder.*;
import static com.envimate.messageMate.messageFunction.TestMessageFunctionBuilder.aMessageFunction;
import static com.envimate.messageMate.messageFunction.TestMessageFunctionValidationBuilder.*;

public class MessageFunctionSpecs {

    @Test
    public void testMessageFunction_obtainsResponseForRequest() {
        given(aMessageFunction()
                .definedWithARequestResponseMapping())
                .when(aRequestIsSend())
                .then(expectTheResponseToBeReceived());
    }

    @Test
    public void testMessageFunction_canDifferentiateBetweenDifferentResponses() {
        given(aMessageFunction()
                .definedWithARequestResponseMapping())
                .when(severalRequestsAreSend())
                .then(expectCorrectResponseReceivedForEachRequest());
    }

    @Test
    public void testMessageFunction_canAcceptDifferentResponses() {
        given(aMessageFunction()
                .acceptingTwoDifferentResponsesForTheTestRequest())
                .when(twoRequestsAreSendThatWithOneOfEachResponsesAnswered())
                .then(expectCorrectResponseReceivedForEachRequest());
    }

    @Test
    public void testMessageFunction_canAcceptErrorResponse() {
        given(aMessageFunction()
                .acceptingErrorResponses())
                .when(aRequestResultingInErrorIsSend())
                .then(expectTheErrorResponseToBeReceived());
    }

    @Test
    public void testMessageFunction_canAcceptGeneralErrorResponse() {
        given(aMessageFunction()
                .acceptingGeneralErrorResponses())
                .when(aRequestResultingInErrorIsSend())
                .then(expectTheErrorResponseToBeReceived());
    }

    @Test
    public void testMessageFunction_executesFollowUpWhenFuturesIsFulfilled() {
        given(aMessageFunction()
                .definedWithARequestResponseMapping())
                .when(aFollowUpActionIsAddedBeforeSend())
                .then(expectTheFollowUpToBeExecuted());
    }

    @Test
    public void testMessageFunction_bubblesExceptionThatIsThrownInFollowUpAction() {
        given(aMessageFunction()
                .definedWithARequestResponseMapping())
                .when(aFollowUpActionWithExceptionIsAddedBeforeSend())
                .then(expectAExceptionToBeThrown());
    }

    /*
    check future contract: e.g. isDone, cancellation, ExecutionException, wake up
    @Test
    public void testMessageFunction_futuresFinishesWhenDeliveryFailedMessageIsReceived() {
        Given.given(TestMessageFunctionBuilder.aMessageFunction()
                .definedWithARequestResponseMapping())
                .when(TestMessageFunctionActionBuilder.aRequestIsSendThatCausesADeliveryFailedMessage())
                .then(TestMessageFunctionValidationBuilder.expectAExceptionToBeThrownOfType(ExecutionException.class));
    }
    */

    @Test
    public void testMessageFunction_getWaitsForTimeout() {
        given(aMessageFunction()
                .definedWithAnUnansweredResponse())
                .when(forTheResponseIsWaitedASpecificTime())
                .then(expectTheTimeoutToBeTriggered());
    }

    @Test
    public void testMessageFunction_canCancelAResponse() {
        given(aMessageFunction()
                .definedWithAnUnansweredResponse())
                .when(aRequestIsCancelled())
                .then(expectTheRequestToBeCancelledAndNoFollowUpActionToBeExecuted());
    }

    @Test
    public void testMessageFunction_canCancelAResponseSeveralTimes() {
        given(aMessageFunction()
                .definedWithAnUnansweredResponse())
                .when(aRequestCanBeCancelledMoreThanOnce())
                .then(expectTheRequestToBeCancelledAndNoFollowUpActionToBeExecuted());
    }

    @Test
    public void testMessageFunction_interruptsOtherWaitingThreads() {
        given(aMessageFunction()
                .definedWithAnUnansweredResponse())
                .when(aRequestsIsCancelledWhileOtherThreadsWait())
                .then(expectTheRequestToBeCancelledAndNoFollowUpActionToBeExecuted());
    }

}
