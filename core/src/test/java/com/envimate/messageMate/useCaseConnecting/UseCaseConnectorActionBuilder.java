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

package com.envimate.messageMate.useCaseConnecting;

import com.envimate.messageMate.qcec.shared.TestAction;
import lombok.RequiredArgsConstructor;

import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.EXPECTED_RESULT;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.RESULT;
import static com.envimate.messageMate.useCaseConnecting.SampleRequest.sampleRequestForResponse;
import static com.envimate.messageMate.useCaseConnecting.SampleRequest.sampleRequestThrowingException;
import static com.envimate.messageMate.useCaseConnecting.SampleResponse.sampleResponse;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class UseCaseConnectorActionBuilder {
    private final TestAction<UseCaseConnector> testAction;

    public static UseCaseConnectorActionBuilder aSuccessfulResponseIsReceived() {
        return new UseCaseConnectorActionBuilder((useCaseConnector, testEnvironment) -> {
            final SampleResponse expectedResponse = sampleResponse();
            testEnvironment.setProperty(EXPECTED_RESULT, expectedResponse);
            final SampleRequest request = sampleRequestForResponse(expectedResponse);
            useCaseConnector.send(request, response -> testEnvironment.setProperty(RESULT, response));
            return null;
        });
    }

    public static UseCaseConnectorActionBuilder anExceptionIsThrown() {
        return new UseCaseConnectorActionBuilder((useCaseConnector, testEnvironment) -> {
            final RuntimeException expectedResponse = new RuntimeException("Expected exception");
            testEnvironment.setProperty(EXPECTED_RESULT, expectedResponse);
            final SampleRequest request = sampleRequestThrowingException(expectedResponse);
            useCaseConnector.send(request, response -> testEnvironment.setProperty(RESULT, response));
            return null;
        });
    }

    public static UseCaseConnectorActionBuilder aMessagesIsSendAfterClose() {
        return new UseCaseConnectorActionBuilder((useCaseConnector, testEnvironment) -> {
            useCaseConnector.close();
            final SampleResponse expectedResponse = sampleResponse();
            final SampleRequest request = sampleRequestForResponse(expectedResponse);
            useCaseConnector.send(request, response -> {
                throw new RuntimeException("Callback should not be called");
            });
            return null;
        });
    }

    public static UseCaseConnectorActionBuilder closeIsCalledSeveralTimes() {
        return callClose(5);
    }

    public static UseCaseConnectorActionBuilder closeIsCalled() {
        return callClose(1);
    }


    private static UseCaseConnectorActionBuilder callClose(final int numberOfCalls) {
        return new UseCaseConnectorActionBuilder((useCaseConnector, testEnvironment) -> {
            for (int i = 0; i < numberOfCalls; i++) {
                useCaseConnector.close();
            }
            return null;
        });
    }

    public TestAction<UseCaseConnector> build() {
        return testAction;
    }
}
