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

import com.envimate.messageMate.messageFunction.givenWhenThen.Given;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import static com.envimate.messageMate.messageFunction.givenWhenThen.Given.given;
import static com.envimate.messageMate.messageFunction.givenWhenThen.TestMessageFunctionActionBuilder.*;
import static com.envimate.messageMate.messageFunction.givenWhenThen.TestMessageFunctionSetupBuilder.aMessageFunction;
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
    public void testMessageFunction_canSetConditionForGeneralErrorResponse() {
        given(aMessageFunction()
                .acceptingGeneralErrorResponsesWithCondition())
                .when(aMatchingAndOneNotMatchingGeneralErrorResponseIsSend())
                .then(expectTheErrorResponseToBeReceived());
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
    public void testMessageFunction_futureIsOnlyFulfilledOnce_forGeneralErrorMessage() {
        given(aMessageFunction()
                .withAGeneralErrorResponseSendTwice())
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
