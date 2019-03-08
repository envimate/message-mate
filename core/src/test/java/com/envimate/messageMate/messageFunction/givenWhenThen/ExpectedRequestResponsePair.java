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

import com.envimate.messageMate.messageFunction.correlation.CorrelationId;
import com.envimate.messageMate.messageFunction.testResponses.*;
import lombok.RequiredArgsConstructor;

import static com.envimate.messageMate.messageFunction.testResponses.AlternativTestResponse.alternativTestResponse;
import static com.envimate.messageMate.messageFunction.testResponses.SimpleTestResponse.testResponse;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class ExpectedRequestResponsePair {
    public final TestRequest request;
    public final TestResponse response;

    public static ExpectedRequestResponsePair generateNewPair() {
        final SimpleTestRequest testRequest = SimpleTestRequest.testRequest();
        final CorrelationId correlationId = testRequest.getCorrelationId();
        final SimpleTestResponse testResponse = testResponse(correlationId);
        return new ExpectedRequestResponsePair(testRequest, testResponse);
    }

    public static ExpectedRequestResponsePair generateNewPairWithAlternativeResponse() {
        final SimpleTestRequest testRequest = SimpleTestRequest.testRequest();
        final CorrelationId correlationId = testRequest.getCorrelationId();
        final AlternativTestResponse testResponse = alternativTestResponse(correlationId);
        return new ExpectedRequestResponsePair(testRequest, testResponse);
    }
}
