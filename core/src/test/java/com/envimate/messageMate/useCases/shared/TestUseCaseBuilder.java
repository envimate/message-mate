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
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.useCases.building.DeserializationStep1Builder;
import com.envimate.messageMate.useCases.building.ResponseSerializationStep1Builder;
import com.envimate.messageMate.useCases.building.Step3Builder;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.*;

import static com.envimate.messageMate.internal.enforcing.NotNullEnforcer.ensureNotNull;

public class TestUseCaseBuilder {
    private final List<Consumer<ResponseSerializationStep1Builder>> serializationDefinition = new LinkedList<>();
    private final List<Consumer<DeserializationStep1Builder>> deserializationDefinition = new LinkedList<>();
    private Class<?> useCaseClass;
    private EventType eventType;
    private BiConsumer<MessageBus, TestEnvironment> setup;
    private Supplier<Object> useCaseInstanceSupplier;
    private Function<TestEnvironment, Object> requestObjectSupplier;
    private Function<TestEnvironment, Object> expectedResultSupplier;
    private Consumer<Step3Builder<?>> useCaseCall;
    private BiConsumer<MessageBusBuilder, TestEnvironment> messageBusEnhancer;
    private UseCaseBusCall useCaseBusCall;

    public static TestUseCaseBuilder aTestUseCase() {
        return new TestUseCaseBuilder();
    }

    public TestUseCaseBuilder forUseCaseClass(final Class<?> useCaseClass) {
        this.useCaseClass = useCaseClass;
        return this;
    }

    public TestUseCaseBuilder forEventType(final EventType eventType) {
        this.eventType = eventType;
        return this;
    }

    public TestUseCaseBuilder withRequest(final Function<Map<String, Object>, Object> requestProvider) {
        this.requestObjectSupplier = testEnvironment -> {
            final Map<String, Object> map = new HashMap<>();
            final Object request = requestProvider.apply(map);
            return request;
        };
        return this;
    }

    public TestUseCaseBuilder withRequestMap(final Consumer<Map<String, Object>> requestProvider) {
        this.requestObjectSupplier = testEnvironment -> {
            final Map<String, Object> map = new HashMap<>();
            requestProvider.accept(map);
            return map;
        };
        return this;
    }

    public TestUseCaseBuilder withRequestProvider(
            final BiFunction<TestEnvironment, Map<String, Object>, Object> requestProvider) {
        this.requestObjectSupplier = testEnvironment -> {
            final Map<String, Object> map = new HashMap<>();
            return requestProvider.apply(testEnvironment, map);
        };
        return this;
    }

    public <T> TestUseCaseBuilder withAParameterSerialization(final Class<T> type,
                                                              final BiConsumer<T, Map<String, Object>> serialization) {
        this.serializationDefinition.add(responseSerializationStep1Builder -> {
            responseSerializationStep1Builder.serializingResponseObjectsOfType(type)
                    .using(object -> {
                        final Map<String, Object> map = new HashMap<>();
                        serialization.accept(object, map);
                        return map;
                    });
        });
        return this;
    }

    public TestUseCaseBuilder withAParameterSerialization(final Predicate<Object> predicate,
                                                          final BiConsumer<Object, Map<String, Object>> serialization) {
        this.serializationDefinition.add(responseSerializationStep1Builder -> {
            responseSerializationStep1Builder.serializingResponseObjectsThat(predicate)
                    .using(object -> {
                        final Map<String, Object> map = new HashMap<>();
                        serialization.accept(object, map);
                        return map;
                    });
        });
        return this;
    }

    public <T> TestUseCaseBuilder withAUseCaseInvocationRequestSerialization(
            final Class<T> type,
            final BiConsumer<T, Map<String, Object>> serialization) {
        return withAParameterSerialization(type, serialization);
    }

    public TestUseCaseBuilder withAUseCaseInvocationRequestSerialization(
            final Predicate<Object> predicate,
            final BiConsumer<Object, Map<String, Object>> serialization) {
        return withAParameterSerialization(predicate, serialization);
    }

    public TestUseCaseBuilder withExpectedResponse(final Function<Map<String, Object>, Object> responseProvider) {
        this.expectedResultSupplier = testEnvironment -> {
            final Map<String, Object> map = new HashMap<>();
            return responseProvider.apply(map);
        };
        return this;
    }

    public TestUseCaseBuilder withExpectedResponse(
            final BiFunction<TestEnvironment, Map<String, Object>, Object> responseProvider) {
        this.expectedResultSupplier = testEnvironment -> {
            final Map<String, Object> map = new HashMap<>();
            return responseProvider.apply(testEnvironment, map);
        };
        return this;
    }

    public TestUseCaseBuilder withExpectedResponseMap(final Consumer<Map<String, Object>> responseProvider) {
        this.expectedResultSupplier = testEnvironment -> {
            final Map<String, Object> map = new HashMap<>();
            responseProvider.accept(map);
            return map;
        };
        return this;
    }

    public <T> TestUseCaseBuilder withParameterDeserialization(final Class<T> type,
                                                               final Function<Map<String, Object>, T> deserialization) {
        this.deserializationDefinition.add(deserializationStep1Builder -> {
            deserializationStep1Builder.mappingRequestsToUseCaseParametersOfType(type).using((targetType, map) -> {
                return deserialization.apply(map);
            });
        });
        return this;
    }

    public TestUseCaseBuilder withAUseCaseInvocationResponseDeserialization(
            final BiPredicate<Class<?>, Map<String, Object>> predicate,
            final Function<Map<String, Object>, Object> deserialization) {
        this.deserializationDefinition.add(responseSerializationStep1Builder -> {
            responseSerializationStep1Builder.mappingRequestsToUseCaseParametersThat(predicate)
                    .using((targetType, map) -> {
                        return deserialization.apply(map);
                    });
        });
        return this;
    }

    public <T> TestUseCaseBuilder withAUseCaseInvocationResponseDeserialization(
            final Class<T> type,
            final Function<Map<String, Object>, T> deserialization) {
        return withParameterDeserialization(type, deserialization);
    }

    public TestUseCaseBuilder instantiatingUseCaseWith(final Supplier<Object> useCaseInstanceSupplier) {
        this.useCaseInstanceSupplier = useCaseInstanceSupplier;
        return this;
    }

    public TestUseCaseBuilder callingUseCaseWith(final CustomUseCaseCall customUseCaseCall) {
        this.useCaseCall = step3Builder -> {
            step3Builder.callingBy((useCase, event, requestDeserializer, responseSerializer) -> {
                @SuppressWarnings("unchecked")
                final Map<String, Object> requestMap = (Map<String, Object>) event;
                final Map<String, Object> responseMap = new HashMap<>();
                customUseCaseCall.call(useCase, requestMap, responseMap);
                return responseMap;
            });
        };
        return this;
    }

    public TestUseCaseBuilder withSetup(final BiConsumer<MessageBus, TestEnvironment> setup) {
        this.setup = setup;
        return this;
    }

    public TestUseCaseBuilder withMessageBusEnhancer(final BiConsumer<MessageBusBuilder, TestEnvironment> messageBusEnhancer) {
        this.messageBusEnhancer = messageBusEnhancer;
        return this;
    }

    public TestUseCaseBuilder invokingOnTheUseCaseBusWith(final UseCaseBusCallBuilder useCaseBusCallBuilder) {
        this.useCaseBusCall = useCaseBusCallBuilder.build(eventType);
        return this;
    }

    public TestUseCase build() {
        ensureNotNull(useCaseClass, "useCaseClass");
        ensureNotNull(eventType, "eventType");
        ensureNotNull(setup, "setup");
        ensureNotNull(useCaseInstanceSupplier, "useCaseInstanceSupplier");
        ensureNotNull(useCaseCall, "useCaseCall");
        ensureNotNull(requestObjectSupplier, "requestObjectSupplier");
        ensureNotNull(expectedResultSupplier, "expectedResultSupplier");
        ensureNotNull(useCaseBusCall, "useCaseBusCall");
        final Consumer<DeserializationStep1Builder> deserializationEnhancer = deserializationStep1Builder -> {
            this.deserializationDefinition.forEach(b -> b.accept(deserializationStep1Builder));
        };
        final Consumer<ResponseSerializationStep1Builder> serializationEnhancer = responseSerializationStep1Builder -> {
            this.serializationDefinition.forEach(b -> b.accept(responseSerializationStep1Builder));
        };
        if (messageBusEnhancer == null) {
            messageBusEnhancer = (messageBusBuilder, testEnvironment) -> {
            };
        }
        return new TestUseCase(useCaseClass, eventType, setup, useCaseInstanceSupplier, deserializationEnhancer,
                serializationEnhancer, useCaseCall, requestObjectSupplier, expectedResultSupplier,
                messageBusEnhancer, useCaseBusCall);
    }

    public interface CustomUseCaseCall {
        void call(Object useCase, Map<String, Object> requestMap, Map<String, Object> responseMap);
    }
}
