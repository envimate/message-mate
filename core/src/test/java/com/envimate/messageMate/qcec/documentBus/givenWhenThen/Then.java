/*
 * Copyright (c) 2019 envimate GmbH - https://envimate.com/.
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

package com.envimate.messageMate.qcec.documentBus.givenWhenThen;

import com.envimate.messageMate.qcec.domainBus.DocumentBus;
import com.envimate.messageMate.shared.givenWhenThen.TestAction;
import com.envimate.messageMate.shared.environment.TestEnvironment;
import com.envimate.messageMate.shared.givenWhenThen.TestValidation;
import lombok.RequiredArgsConstructor;

import static com.envimate.messageMate.shared.environment.TestEnvironmentProperty.RESULT;
import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
public class Then {
    private final TestDocumentBusBuilder testDocumentBusBuilder;
    private final DocumentBusActionBuilder documentBusActionBuilder;

    public void then(final DocumentBusValidationBuilder documentBusValidationBuilder) {
        final DocumentBus documentBus = testDocumentBusBuilder.build();
        final TestEnvironment testEnvironment = testDocumentBusBuilder.getTestEnvironment();
        final TestAction<DocumentBus> action = documentBusActionBuilder.build();
        final Object result = action.execute(documentBus, testEnvironment);
        if (result != null) {
            testEnvironment.setProperty(RESULT, result);
        }

        final TestValidation validation = documentBusValidationBuilder.build();
        validation.validate(testEnvironment);
    }
}
