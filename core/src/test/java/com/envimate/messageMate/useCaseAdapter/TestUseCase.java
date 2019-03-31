package com.envimate.messageMate.useCaseAdapter;

import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.messageBus.MessageBusBuilder;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.soonToBeExternal.building.UseCaseAdapterStep3Builder;
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
    private final Class<?> useCaseEvent;
    @Getter
    private final BiConsumer<MessageBus, TestEnvironment> messageBusSetup;
    @Getter
    private final Supplier<Object> instantiationFunction;
    @Getter
    private final Consumer<UseCaseAdapterStep3Builder<?, ?>> parameterMapping;

    private final Function<TestEnvironment, Object> requestObjectSupplier;
    @Getter
    private final Function<TestEnvironment, Object> expectedResultSupplier;

    @Getter
    private final Consumer<MessageBusBuilder> messageBusEnhancer;

    public static TestUseCase testUseCase(final Class<?> useCaseClass,
                                          final Class<?> useCaseEvent,
                                          final BiConsumer<MessageBus, TestEnvironment> messageBusSetup,
                                          final Supplier<Object> instantiationFunction,
                                          final Consumer<UseCaseAdapterStep3Builder<?, ?>> parameterMapping,
                                          final Object requestObject,
                                          final Object expectedResult) {
        final Function<TestEnvironment, Object> requestObjectSupplier = testEnvironment -> requestObject;
        final Function<TestEnvironment, Object> expectedResultSupplier = testEnvironment -> expectedResult;
        return testUseCase(useCaseClass, useCaseEvent, messageBusSetup, instantiationFunction, parameterMapping, requestObjectSupplier, expectedResultSupplier);
    }

    public static TestUseCase testUseCase(final Class<?> useCaseClass,
                                          final Class<?> useCaseEvent,
                                          final BiConsumer<MessageBus, TestEnvironment> messageBusSetup,
                                          final Supplier<Object> instantiationFunction,
                                          final Consumer<UseCaseAdapterStep3Builder<?, ?>> parameterMapping,
                                          final Object requestObject,
                                          final Object expectedResult,
                                          final Consumer<MessageBusBuilder> messageBusEnhancer) {
        final Function<TestEnvironment, Object> requestObjectSupplier = testEnvironment -> requestObject;
        final Function<TestEnvironment, Object> expectedResultSupplier = testEnvironment -> expectedResult;
        return testUseCase(useCaseClass, useCaseEvent, messageBusSetup, instantiationFunction, parameterMapping, requestObjectSupplier, expectedResultSupplier, messageBusEnhancer);
    }

    public static TestUseCase testUseCase(final Class<?> useCaseClass,
                                          final Class<?> useCaseEvent,
                                          final BiConsumer<MessageBus, TestEnvironment> messageBusSetup,
                                          final Supplier<Object> instantiationFunction,
                                          final Consumer<UseCaseAdapterStep3Builder<?, ?>> parameterMapping,
                                          final Function<TestEnvironment, Object> requestObjectSupplier,
                                          final Function<TestEnvironment, Object> expectedResultSupplier) {
        final Consumer<MessageBusBuilder> messageBusEnhancer = messageBusBuilder -> {
        };
        return testUseCase(useCaseClass, useCaseEvent, messageBusSetup, instantiationFunction, parameterMapping, requestObjectSupplier, expectedResultSupplier, messageBusEnhancer);
    }

    public static TestUseCase testUseCase(final Class<?> useCaseClass,
                                          final Class<?> useCaseEvent,
                                          final BiConsumer<MessageBus, TestEnvironment> messageBusSetup,
                                          final Supplier<Object> instantiationFunction,
                                          final Consumer<UseCaseAdapterStep3Builder<?, ?>> parameterMapping,
                                          final Function<TestEnvironment, Object> requestObjectSupplier,
                                          final Function<TestEnvironment, Object> expectedResultSupplier,
                                          final Consumer<MessageBusBuilder> messageBusEnhancer) {
        return new TestUseCase(useCaseClass, useCaseEvent, messageBusSetup, instantiationFunction, parameterMapping, requestObjectSupplier, expectedResultSupplier, messageBusEnhancer);
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

    public void defineParameterMapping(final UseCaseAdapterStep3Builder<?, ?> callingBuilder) {
        parameterMapping.accept(callingBuilder);
    }
}
