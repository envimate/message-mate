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

    public void defineDeserialization(DeserializationStep1Builder deserializationBuilder) {
        deserializationEnhancer.accept(deserializationBuilder);
    }

    public void defineSerialization(ResponseSerializationStep1Builder serializationBuilder) {
        serializationEnhancer.accept(serializationBuilder);
    }

}
