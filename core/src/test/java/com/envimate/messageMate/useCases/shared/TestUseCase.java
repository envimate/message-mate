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

package com.envimate.messageMate.useCases.shared;

import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.messageBus.MessageBusBuilder;
import com.envimate.messageMate.processingContext.EventType;
import com.envimate.messageMate.shared.environment.TestEnvironment;
import com.envimate.messageMate.useCases.building.DeserializationStep1Builder;
import com.envimate.messageMate.useCases.building.ResponseSerializationStep1Builder;
import com.envimate.messageMate.useCases.building.Step3Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
public final class TestUseCase {
    @Getter
    private final Class<?> useCaseClass;
    @Getter
    private final EventType eventType;
    @Getter
    private final BiConsumer<MessageBus, TestEnvironment> messageBusSetup;
    @Getter
    private final Supplier<Object> instantiationFunction;
    @Getter
    private final Consumer<DeserializationStep1Builder> deserializationEnhancer;
    @Getter
    private final Consumer<ResponseSerializationStep1Builder> serializationEnhancer;
    @Getter
    private final Consumer<Step3Builder<?>> customCallingLogic;
    private final Function<TestEnvironment, Object> requestObjectSupplier;
    @Getter
    private final Function<TestEnvironment, Object> expectedResultSupplier;
    @Getter
    private final BiConsumer<MessageBusBuilder, TestEnvironment> messageBusEnhancer;
    @Getter
    private final UseCaseBusCall useCaseBusCall;

    public void performNecessaryResultSubscriptionsOn(final MessageBus messageBus, final TestEnvironment testEnvironment) {
        messageBusSetup.accept(messageBus, testEnvironment);
    }

    public Object getRequestObject(final TestEnvironment testEnvironment) {
        return requestObjectSupplier.apply(testEnvironment);
    }

    public Object getExpectedResult(final TestEnvironment testEnvironment) {
        return expectedResultSupplier.apply(testEnvironment);
    }

    public void useCustomInvocationLogic(final Step3Builder<?> callingBuilder) {
        customCallingLogic.accept(callingBuilder);
    }

    public void defineDeserialization(final DeserializationStep1Builder deserializationBuilder) {
        deserializationEnhancer.accept(deserializationBuilder);
    }

    public void defineSerialization(final ResponseSerializationStep1Builder serializationBuilder) {
        serializationEnhancer.accept(serializationBuilder);
    }

}
