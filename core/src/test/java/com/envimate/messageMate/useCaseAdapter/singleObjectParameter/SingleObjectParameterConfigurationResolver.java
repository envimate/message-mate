package com.envimate.messageMate.useCaseAdapter.singleObjectParameter;

import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.shared.config.AbstractTestConfigProvider;
import com.envimate.messageMate.soonToBeExternal.building.UseCaseAdapterStep3Builder;
import com.envimate.messageMate.useCaseAdapter.TestUseCase;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.RESULT;
import static com.envimate.messageMate.useCaseAdapter.TestUseCase.testUseCase;

public class SingleObjectParameterConfigurationResolver extends AbstractTestConfigProvider {
    @Override
    protected Class<?> forConfigClass() {
        return TestUseCase.class;
    }

    @Override
    protected Object testConfig() {
        final String expectedResponse = "expected Response";
        final BiConsumer<MessageBus, TestEnvironment> messageBusSetup = (messageBus, testEnvironment) -> {
            messageBus.subscribe(String.class, s -> {
                if (testEnvironment.has(RESULT)) {
                    testEnvironment.addToListProperty(RESULT, s);
                } else {
                    testEnvironment.setProperty(RESULT, s);
                }
            });
        };
        final Object requestObject = TestUseCaseRequest.testUseCaseRequest(expectedResponse);
        final Supplier<Object> instantiationFunction = SingleObjectParameterUseCase::new;
        final Consumer<UseCaseAdapterStep3Builder<?, ?>> parameterMapping = callingBuilder -> {
            callingBuilder.calling((useCaseInstance, event) -> {
                final SingleObjectParameterUseCase useCase = (SingleObjectParameterUseCase) useCaseInstance;
                final TestUseCaseRequest testUseCaseRequest = (TestUseCaseRequest) event;
                final String stringReturnValue = useCase.useCaseMethod(testUseCaseRequest);
                return stringReturnValue;
            });
        };
        return testUseCase(SingleObjectParameterUseCase.class, TestUseCaseRequest.class, messageBusSetup, instantiationFunction, parameterMapping, requestObject, expectedResponse);
    }
}
