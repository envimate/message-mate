package com.envimate.messageMate.useCaseAdapter;

import com.envimate.messageMate.messageBus.EventType;
import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.messageBus.MessageBusBuilder;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.useCaseAdapter.building.UseCaseAdapterDeserializationStep1Builder;
import com.envimate.messageMate.useCaseAdapter.building.UseCaseAdapterStep3Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
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
    private final Consumer<UseCaseAdapterDeserializationStep1Builder> deserializationEnhancer;
    @Getter
    private final Consumer<UseCaseAdapterStep3Builder<?>> customCallingLogic;

    private final Function<TestEnvironment, Object> requestObjectSupplier;
    @Getter
    private final Function<TestEnvironment, Object> expectedResultSupplier;

    @Getter
    private final Consumer<MessageBusBuilder> messageBusEnhancer;

    public static TestUseCase testUseCase(final Class<?> useCaseClass,
                                          final EventType eventType,
                                          final BiConsumer<MessageBus, TestEnvironment> messageBusSetup,
                                          final Supplier<Object> instantiationFunction,
                                          final Consumer<UseCaseAdapterStep3Builder<?>> customCallingLogic,
                                          final Object requestObject,
                                          final Object expectedResult) {
        final Function<TestEnvironment, Object> requestObjectSupplier = testEnvironment -> requestObject;
        final Function<TestEnvironment, Object> expectedResultSupplier = testEnvironment -> expectedResult;
        return testUseCase(useCaseClass, eventType, messageBusSetup, instantiationFunction, customCallingLogic,
                requestObjectSupplier, expectedResultSupplier);
    }

    public static TestUseCase testUseCase(final Class<?> useCaseClass,
                                          final EventType eventType,
                                          final BiConsumer<MessageBus, TestEnvironment> messageBusSetup,
                                          final Supplier<Object> instantiationFunction,
                                          final Consumer<UseCaseAdapterDeserializationStep1Builder> deserializationEnhancer,
                                          final Consumer<UseCaseAdapterStep3Builder<?>> customCallingLogic,
                                          final Object requestObject,
                                          final Object expectedResult) {
        final Function<TestEnvironment, Object> requestObjectSupplier = testEnvironment -> requestObject;
        final Function<TestEnvironment, Object> expectedResultSupplier = testEnvironment -> expectedResult;
        final Consumer<MessageBusBuilder> messageBusEnhancer = messageBusBuilder -> {
        };
        return testUseCase(useCaseClass, eventType, messageBusSetup, instantiationFunction, deserializationEnhancer,
                customCallingLogic, requestObjectSupplier, expectedResultSupplier, messageBusEnhancer);
    }

    public static TestUseCase testUseCase(final Class<?> useCaseClass,
                                          final EventType eventType,
                                          final BiConsumer<MessageBus, TestEnvironment> messageBusSetup,
                                          final Supplier<Object> instantiationFunction,
                                          final Consumer<UseCaseAdapterStep3Builder<?>> customCallingLogic,
                                          final Object requestObject,
                                          final Object expectedResult,
                                          final Consumer<MessageBusBuilder> messageBusEnhancer) {
        final Function<TestEnvironment, Object> requestObjectSupplier = testEnvironment -> requestObject;
        final Function<TestEnvironment, Object> expectedResultSupplier = testEnvironment -> expectedResult;
        return testUseCase(useCaseClass, eventType, messageBusSetup, instantiationFunction, customCallingLogic,
                requestObjectSupplier, expectedResultSupplier, messageBusEnhancer);
    }

    public static TestUseCase testUseCase(final Class<?> useCaseClass,
                                          final EventType eventType,
                                          final BiConsumer<MessageBus, TestEnvironment> messageBusSetup,
                                          final Supplier<Object> instantiationFunction,
                                          final Consumer<UseCaseAdapterStep3Builder<?>> customCallingLogic,
                                          final Function<TestEnvironment, Object> requestObjectSupplier,
                                          final Function<TestEnvironment, Object> expectedResultSupplier) {
        final Consumer<MessageBusBuilder> messageBusEnhancer = messageBusBuilder -> {
        };
        return testUseCase(useCaseClass, eventType, messageBusSetup, instantiationFunction, customCallingLogic, requestObjectSupplier, expectedResultSupplier, messageBusEnhancer);
    }

    public static TestUseCase testUseCase(final Class<?> useCaseClass,
                                          final EventType eventType,
                                          final BiConsumer<MessageBus, TestEnvironment> messageBusSetup,
                                          final Supplier<Object> instantiationFunction,
                                          final Consumer<UseCaseAdapterStep3Builder<?>> customCallingLogic,
                                          final Function<TestEnvironment, Object> requestObjectSupplier,
                                          final Function<TestEnvironment, Object> expectedResultSupplier,
                                          final Consumer<MessageBusBuilder> messageBusEnhancer) {
        final Consumer<UseCaseAdapterDeserializationStep1Builder> deserializationEnhancer = mappingBuilder -> {
        };
        return new TestUseCase(useCaseClass, eventType, messageBusSetup, instantiationFunction, deserializationEnhancer,
                customCallingLogic, requestObjectSupplier, expectedResultSupplier, messageBusEnhancer);
    }

    public static TestUseCase testUseCase(final Class<?> useCaseClass,
                                          final EventType eventType,
                                          final BiConsumer<MessageBus, TestEnvironment> messageBusSetup,
                                          final Supplier<Object> instantiationFunction,
                                          final Consumer<UseCaseAdapterDeserializationStep1Builder> deserializationEnhancer,
                                          final Consumer<UseCaseAdapterStep3Builder<?>> customCallingLogic,
                                          final Function<TestEnvironment, Object> requestObjectSupplier,
                                          final Function<TestEnvironment, Object> expectedResultSupplier) {
        final Consumer<MessageBusBuilder> messageBusEnhancer = messageBusBuilder -> {
        };
        return new TestUseCase(useCaseClass, eventType, messageBusSetup, instantiationFunction, deserializationEnhancer,
                customCallingLogic, requestObjectSupplier, expectedResultSupplier, messageBusEnhancer);
    }

    public static TestUseCase testUseCase(final Class<?> useCaseClass,
                                          final EventType eventType,
                                          final BiConsumer<MessageBus, TestEnvironment> messageBusSetup,
                                          final Supplier<Object> instantiationFunction,
                                          final Consumer<UseCaseAdapterDeserializationStep1Builder> deserializationEnhancer,
                                          final Consumer<UseCaseAdapterStep3Builder<?>> customCallingLogic,
                                          final Object request,
                                          final Object result,
                                          final Consumer<MessageBusBuilder> messageBusEnhancer) {
        final Function<TestEnvironment, Object> requestObjectSupplier = (testEnvironment) -> request;
        final Function<TestEnvironment, Object> expectedResultSupplier = (testEnvironment) -> result;
        return new TestUseCase(useCaseClass, eventType, messageBusSetup, instantiationFunction, deserializationEnhancer,
                customCallingLogic, requestObjectSupplier, expectedResultSupplier, messageBusEnhancer);
    }

    public static TestUseCase testUseCase(final Class<?> useCaseClass,
                                          final EventType eventType,
                                          final BiConsumer<MessageBus, TestEnvironment> messageBusSetup,
                                          final Supplier<Object> instantiationFunction,
                                          final Consumer<UseCaseAdapterDeserializationStep1Builder> deserializationEnhancer,
                                          final Consumer<UseCaseAdapterStep3Builder<?>> customCallingLogic,
                                          final Function<TestEnvironment, Object> requestObjectSupplier,
                                          final Function<TestEnvironment, Object> expectedResultSupplier,
                                          final Consumer<MessageBusBuilder> messageBusEnhancer) {
        return new TestUseCase(useCaseClass, eventType, messageBusSetup, instantiationFunction, deserializationEnhancer,
                customCallingLogic, requestObjectSupplier, expectedResultSupplier, messageBusEnhancer);
    }

    public void performNecessaryResultSubscriptionsOn(final MessageBus messageBus, final TestEnvironment testEnvironment) {
        messageBusSetup.accept(messageBus, testEnvironment);
    }

    public Object getRequestObjectSupplier(final TestEnvironment testEnvironment) {
        return requestObjectSupplier.apply(testEnvironment);
    }

    public Object getExpectedResult(final TestEnvironment testEnvironment) {
        return expectedResultSupplier.apply(testEnvironment);
    }

    public void useCustomInvocationLogic(final UseCaseAdapterStep3Builder<?> callingBuilder) {
        customCallingLogic.accept(callingBuilder);
    }

    public void defineDeserialization(UseCaseAdapterDeserializationStep1Builder deserializationBuilder) {
        deserializationEnhancer.accept(deserializationBuilder);
    }
}
