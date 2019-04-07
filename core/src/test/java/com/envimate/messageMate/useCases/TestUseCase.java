package com.envimate.messageMate.useCases;

import com.envimate.messageMate.messageBus.EventType;
import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.messageBus.MessageBusBuilder;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.useCases.building.DeserializationStep1Builder;
import com.envimate.messageMate.useCases.building.ResponseSerializationStep1Builder;
import com.envimate.messageMate.useCases.building.Step3Builder;
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

    public static TestUseCase testUseCase(final Class<?> useCaseClass,
                                          final EventType eventType,
                                          final BiConsumer<MessageBus, TestEnvironment> messageBusSetup,
                                          final Supplier<Object> instantiationFunction,
                                          final Consumer<DeserializationStep1Builder> deserializationEnhancer,
                                          final Consumer<ResponseSerializationStep1Builder> serializationEnhancer,
                                          final Consumer<Step3Builder<?>> customCallingLogic,
                                          final Object requestObject,
                                          final Object expectedResult) {
        final Function<TestEnvironment, Object> requestObjectSupplier = testEnvironment -> requestObject;
        final Function<TestEnvironment, Object> expectedResultSupplier = testEnvironment -> expectedResult;

        final BiConsumer<MessageBusBuilder, TestEnvironment> messageBusEnhancer = (messageBusBuilder, testEnvironment) -> {
        };
        return new TestUseCase(useCaseClass, eventType, messageBusSetup, instantiationFunction, deserializationEnhancer, serializationEnhancer,
                customCallingLogic, requestObjectSupplier, expectedResultSupplier, messageBusEnhancer);
    }
    public static TestUseCase testUseCase(final Class<?> useCaseClass,
                                          final EventType eventType,
                                          final BiConsumer<MessageBus, TestEnvironment> messageBusSetup,
                                          final Supplier<Object> instantiationFunction,
                                          final Consumer<DeserializationStep1Builder> deserializationEnhancer,
                                          final Consumer<ResponseSerializationStep1Builder> serializationEnhancer,
                                          final Consumer<Step3Builder<?>> customCallingLogic,
                                          final Function<TestEnvironment, Object> requestObjectSupplier,
                                          final Function<TestEnvironment, Object> expectedResultSupplier) {

        final BiConsumer<MessageBusBuilder, TestEnvironment> messageBusEnhancer = (messageBusBuilder, testEnvironment) -> {
        };
        return new TestUseCase(useCaseClass, eventType, messageBusSetup, instantiationFunction, deserializationEnhancer, serializationEnhancer,
                customCallingLogic, requestObjectSupplier, expectedResultSupplier, messageBusEnhancer);
    }

    public static TestUseCase testUseCase(final Class<?> useCaseClass,
                                          final EventType eventType,
                                          final BiConsumer<MessageBus, TestEnvironment> messageBusSetup,
                                          final Supplier<Object> instantiationFunction,
                                          final Consumer<DeserializationStep1Builder> deserializationEnhancer,
                                          final Consumer<Step3Builder<?>> customCallingLogic,
                                          final Object requestObject,
                                          final Object expectedResult) {
        final Function<TestEnvironment, Object> requestObjectSupplier = testEnvironment -> requestObject;
        final Function<TestEnvironment, Object> expectedResultSupplier = testEnvironment -> expectedResult;
        final Consumer<ResponseSerializationStep1Builder> serializationEnhancer = b -> {

        };
        final BiConsumer<MessageBusBuilder, TestEnvironment> messageBusEnhancer = (messageBusBuilder, testEnvironment) -> {
        };
        return new TestUseCase(useCaseClass, eventType, messageBusSetup, instantiationFunction, deserializationEnhancer,serializationEnhancer,
                customCallingLogic, requestObjectSupplier, expectedResultSupplier, messageBusEnhancer);
    }


    public static TestUseCase testUseCase(final Class<?> useCaseClass,
                                          final EventType eventType,
                                          final BiConsumer<MessageBus, TestEnvironment> messageBusSetup,
                                          final Supplier<Object> instantiationFunction,
                                          final Consumer<DeserializationStep1Builder> deserializationEnhancer,
                                          final Consumer<Step3Builder<?>> customCallingLogic,
                                          final Object request,
                                          final Object result,
                                          final BiConsumer<MessageBusBuilder, TestEnvironment> messageBusEnhancer) {
        final Function<TestEnvironment, Object> requestObjectSupplier = (testEnvironment) -> request;
        final Function<TestEnvironment, Object> expectedResultSupplier = (testEnvironment) -> result;
        final Consumer<ResponseSerializationStep1Builder> serializationEnhancer = b -> {

        };
        return new TestUseCase(useCaseClass, eventType, messageBusSetup, instantiationFunction, deserializationEnhancer, serializationEnhancer,
                customCallingLogic, requestObjectSupplier, expectedResultSupplier, messageBusEnhancer);
    }


    public static TestUseCase testUseCase(final Class<?> useCaseClass,
                                          final EventType eventType,
                                          final BiConsumer<MessageBus, TestEnvironment> messageBusSetup,
                                          final Supplier<Object> instantiationFunction,
                                          final Consumer<DeserializationStep1Builder> deserializationEnhancer,
                                          final Consumer<ResponseSerializationStep1Builder> serializationEnhancer,
                                          final Consumer<Step3Builder<?>> customCallingLogic,
                                          final Object request,
                                          final Object result,
                                          final BiConsumer<MessageBusBuilder, TestEnvironment> messageBusEnhancer) {
        final Function<TestEnvironment, Object> requestObjectSupplier = (testEnvironment) -> request;
        final Function<TestEnvironment, Object> expectedResultSupplier = (testEnvironment) -> result;
        return new TestUseCase(useCaseClass, eventType, messageBusSetup, instantiationFunction, deserializationEnhancer,
                serializationEnhancer, customCallingLogic, requestObjectSupplier, expectedResultSupplier, messageBusEnhancer);
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

    public void useCustomInvocationLogic(final Step3Builder<?> callingBuilder) {
        customCallingLogic.accept(callingBuilder);
    }

    public void defineDeserialization(DeserializationStep1Builder deserializationBuilder) {
        deserializationEnhancer.accept(deserializationBuilder);
    }

    public void defineSerialization(ResponseSerializationStep1Builder serializationBuilder) {
        serializationEnhancer.accept(serializationBuilder);
    }
}
