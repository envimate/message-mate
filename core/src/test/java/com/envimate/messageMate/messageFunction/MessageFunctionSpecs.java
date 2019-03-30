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

import org.junit.jupiter.api.Test;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import static com.envimate.messageMate.messageFunction.givenWhenThen.Given.given;
import static com.envimate.messageMate.messageFunction.givenWhenThen.TestMessageFunctionActionBuilder.*;
import static com.envimate.messageMate.messageFunction.givenWhenThen.TestMessageFunctionSetupBuilder.aMessageFunction;
import static com.envimate.messageMate.messageFunction.givenWhenThen.TestMessageFunctionValidationBuilder.*;

//TODO: check fulfilled for onError of response
//TODO: check for null as response
public class MessageFunctionSpecs {

    @Test
    public void testMessageFunction_obtainsResponseForRequest() {
        given(aMessageFunction()
                .withTheRequestAnsweredByACorrelatedResponse())
                .when(aRequestIsSend())
                .then(expectTheResponseToBeReceived());
    }

    @Test
    public void testMessageFunction_canDifferentiateBetweenDifferentResponses() {
        given(aMessageFunction()
                .withTheRequestAnsweredByACorrelatedResponse())
                .when(severalRequestsAreSend())
                .then(expectCorrectResponseReceivedForEachRequest());
    }

    @Test
    public void testMessageFunction_fullFillsOnlyForCorrectResponse() {
        given(aMessageFunction()
                .acceptingTwoDifferentResponsesForTheTestRequest())
                .when(twoRequestsAreSendThatWithOneOfEachResponsesAnswered())
                .then(expectCorrectTheResponseToBeReceived());
    }


    @Test
    public void testMessageFunction_futureIsOnlyFulfilledOnce_forRedundantMessage() {
        given(aMessageFunction()
                .withFulfillingResponseSendTwice())
                .when(aFollowUpActionExecutingOnlyOnceIsAddedBeforeRequest())
                .then(expectTheFutureToBeFulFilledOnlyOnce());
    }

    @Test
    public void testMessageFunction_futureIsOnlyFulfilledOnce_forMessageAndException() {
        given(aMessageFunction()
                .withRequestAnsweredByResponseThenByException())
                .when(aFollowUpActionExecutingOnlyOnceIsAddedBeforeRequest())
                .then(expectTheFutureToBeFulFilledOnlyOnce());
    }

    @Test
    public void testMessageFunction_futureIsOnlyFulfilledOnce_forExceptionAndMessage() {
        given(aMessageFunction()
                .withRequestAnsweredByExceptionThenByMessage())
                .when(aFollowUpActionExecutingOnlyOnceIsAddedBeforeRequest())
                .then(expectTheFutureToBeFulFilledOnlyOnce());
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
        given(aMessageFunction()
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

    //cancelling
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
                .when(aRequestIsCancelledSeveralTimes())
                .then(expectTheRequestToBeCancelledAndNoFollowUpActionToBeExecuted());
    }

    @Test
    public void testMessageFunction_cancellationAlwaysReturnsTheSameResult() {
        given(aMessageFunction()
                .definedWithAnUnansweredResponse())
                .when(aRequestIsCancelledSeveralTimes())
                .then(expectAllCancellationsToHaveReturnedTheSameResult());
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

    @Test
    public void testMessageFunction_cancellingAFulfilled() {
        given(aMessageFunction()
                .definedWithAnUnansweredResponse())
                .when(theFutureIsFulfilledAndThenCancelled())
                .then(expectTheCancellationToFailed());
    }

    @Test
    public void testMessageFunction_aResponseAfterACancellationDoesNotExecuteFollowUpAction() {
        given(aMessageFunction()
                .definedWithAnUnansweredResponse())
                .when(aResponseToACancelledRequestDoesNotExecuteFollowUpAction())
                .then(expectTheRequestToBeCancelledAndNoFollowUpActionToBeExecuted());
    }

    @Test
    public void testMessageFunction_addingAFollowUpActionToACancelledFutureFails() {
        given(aMessageFunction()
                .withTheRequestAnsweredByACorrelatedResponse())
                .when(aFollowUpActionIsAddedToACancelledFuture())
                .then(expectAExceptionToBeThrownOfType(CancellationException.class));
    }

}
