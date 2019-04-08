package com.envimate.messageMate.useCases;

import com.envimate.messageMate.messageBus.EventType;
import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.messageBus.MessageBusBuilder;
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
        this.requestObjectSupplier = (testEnvironment -> {
            final Map<String, Object> map = new HashMap<>();
            final Object request = requestProvider.apply(map);
            return request;
        });
        return this;
    }

    public TestUseCaseBuilder withRequestMap(final Consumer<Map<String, Object>> requestProvider) {
        this.requestObjectSupplier = (testEnvironment -> {
            final Map<String, Object> map = new HashMap<>();
            requestProvider.accept(map);
            return map;
        });
        return this;
    }

    public TestUseCaseBuilder withRequestProvider(final BiFunction<TestEnvironment, Map<String, Object>, Object> requestProvider) {
        this.requestObjectSupplier = testEnvironment -> {
            final Map<String, Object> map = new HashMap<>();
            return requestProvider.apply(testEnvironment, map);
        };
        return this;
    }


    public <T> TestUseCaseBuilder withAParameterSerialization(Class<T> type, BiConsumer<T, Map<String, Object>> serialization) {
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

    public TestUseCaseBuilder withAParameterSerialization(Predicate<Object> predicate, BiConsumer<Object, Map<String, Object>> serialization) {
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


    public <T> TestUseCaseBuilder withAUseCaseInvocationRequestSerialization(Class<T> type, BiConsumer<T, Map<String, Object>> serialization) {
        return withAParameterSerialization(type, serialization);
    }

    public TestUseCaseBuilder withExpectedResponse(final Function<Map<String, Object>, Object> responseProvider) {
        this.expectedResultSupplier = (testEnvironment -> {
            final Map<String, Object> map = new HashMap<>();
            return responseProvider.apply(map);
        });
        return this;
    }

    public TestUseCaseBuilder withExpectedResponseMap(final Consumer<Map<String, Object>> responseProvider) {
        this.expectedResultSupplier = (testEnvironment -> {
            final Map<String, Object> map = new HashMap<>();
            responseProvider.accept(map);
            return map;
        });
        return this;
    }

    public TestUseCaseBuilder withExpectedResponse(final BiFunction<TestEnvironment, Map<String, Object>, Object> responseProvider) {
        this.expectedResultSupplier = testEnvironment -> {
            final Map<String, Object> map = new HashMap<>();
            return responseProvider.apply(testEnvironment, map);
        };
        return this;
    }

    public <T> TestUseCaseBuilder withParameterDeserialization(Class<T> type, Function<Map<String, Object>, T> deserialization) {
        this.deserializationDefinition.add(deserializationStep1Builder -> {
            deserializationStep1Builder.mappingRequestsToUseCaseParametersOfType(type).using((targetType, map) -> {
                return deserialization.apply(map);
            });
        });
        return this;
    }


    public TestUseCaseBuilder withAUseCaseInvocationResponseDeserialization(BiPredicate<Class<?>, Map<String, Object>> predicate, Function<Map<String, Object>, Object> deserialization) {
        this.deserializationDefinition.add(responseSerializationStep1Builder -> {
            responseSerializationStep1Builder.mappingRequestsToUseCaseParametersThat(predicate)
                    .using((targetType, map) -> {
                        return deserialization.apply(map);
                    });
        });
        return this;
    }

    public <T> TestUseCaseBuilder withAUseCaseInvocationResponseDeserialization(Class<T> type, Function<Map<String, Object>, T> deserialization) {
        return withParameterDeserialization(type, deserialization);
    }

    public TestUseCaseBuilder withAUseCaseInvocationRequestSerialization(Predicate<Object> predicate, BiConsumer<Object, Map<String, Object>> serialization) {
        return withAParameterSerialization(predicate, serialization);
    }

    public TestUseCaseBuilder instantiatingUseCaseWith(Supplier<Object> useCaseInstanceSupplier) {
        this.useCaseInstanceSupplier = useCaseInstanceSupplier;
        return this;
    }

    public TestUseCaseBuilder callingUseCaseWith(CustomUseCaseCall customUseCaseCall) {
        this.useCaseCall = step3Builder -> {
            step3Builder.callingBy((useCase, event, requestDeserializer, responseSerializer) -> {
                final Map<String, Object> requestMap = (Map<String, Object>) event;
                final Map<String, Object> responseMap = new HashMap<>();
                customUseCaseCall.call(useCase, requestMap, responseMap);
                return responseMap;
            });
        };
        return this;
    }

    public TestUseCaseBuilder callingUseCaseWith(Consumer<Step3Builder<?>> customUseCaseCall) {
        this.useCaseCall = customUseCaseCall;
        return this;
    }

    public TestUseCaseBuilder withSetup(BiConsumer<MessageBus, TestEnvironment> setup) {
        this.setup = setup;
        return this;
    }

    public TestUseCaseBuilder withMessageBusEnhancer(BiConsumer<MessageBusBuilder, TestEnvironment> messageBusEnhancer) {
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
