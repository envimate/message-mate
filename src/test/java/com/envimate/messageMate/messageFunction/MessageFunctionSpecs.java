package com.envimate.messageMate.messageFunction;

import com.envimate.messageMate.messageFunction.givenWhenThen.Given;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import static com.envimate.messageMate.messageFunction.givenWhenThen.Given.given;
import static com.envimate.messageMate.messageFunction.givenWhenThen.TestMessageFunctionActionBuilder.*;
import static com.envimate.messageMate.messageFunction.givenWhenThen.TestMessageFunctionBuilder.aMessageFunction;
import static com.envimate.messageMate.messageFunction.givenWhenThen.TestMessageFunctionValidationBuilder.*;

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
                .definedWithAnUnansweredResponse())
                .when(aFollowUpActionIsAddedBeforeSend())
                .then(expectTheFollowUpToBeExecuted());
    }

    @Test
    public void testMessageFunction_getsAccessToExceptionInFollowUp() {
        given(aMessageFunction()
                .definedWithResponseThrowingAnException())
                .when(aFollowUpActionForAnExceptionIsAdded())
                .then(expectAExceptionToBeThrown());
    }

    @Test
    public void testMessageFunction_futuresFinishesWhenDeliveryFailedMessageIsReceived() {
        Given.given(aMessageFunction()
                .definedWithResponseThrowingAnException())
                .when(aRequestIsSendThatCausesADeliveryFailedMessage())
                .then(expectAFutureToBeFinishedWithException(ExecutionException.class));
    }

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

    @Test
    public void testMessageFunction_throwsExceptionWhenResultOfACancelledResponseIsRequested() {
        given(aMessageFunction()
                .definedWithAnUnansweredResponse())
                .when(theResultOfACancelledRequestIsTaken())
                .then(expectAExceptionToBeThrownOfType(CancellationException.class));
    }

}
