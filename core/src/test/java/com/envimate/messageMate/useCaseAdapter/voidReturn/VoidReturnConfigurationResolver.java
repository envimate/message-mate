package com.envimate.messageMate.useCaseAdapter.voidReturn;

import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.shared.config.AbstractTestConfigProvider;
import com.envimate.messageMate.soonToBeExternal.building.UseCaseAdapterStep3Builder;
import com.envimate.messageMate.useCaseAdapter.TestUseCase;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.EXPECTED_RESULT;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.RESULT;
import static com.envimate.messageMate.useCaseAdapter.TestUseCase.testUseCase;
import static com.envimate.messageMate.useCaseAdapter.UseCaseAdapterTestProperties.MESSAGE_FUNCTION_USED;
import static com.envimate.messageMate.useCaseAdapter.voidReturn.CallbackTestRequest.callbackTestRequest;

public class VoidReturnConfigurationResolver extends AbstractTestConfigProvider {

    public static final Class<?> USE_CASE_CLASS = VoidReturnUseCase.class;
    public static final Class<?> EventClass = CallbackTestRequest.class;

    @Override
    protected Class<?> forConfigClass() {
        return TestUseCase.class;
    }

    @Override
    protected Object testConfig() {
        final BiConsumer<MessageBus, TestEnvironment> messageBusSetup = (messageBus, testEnvironment) -> {
        };
        final Function<TestEnvironment, Object> requestObjectFunction = testEnvironment -> {
            final CallbackTestRequest testRequest = callbackTestRequest(o -> {
                if (!testEnvironment.getPropertyAsType(MESSAGE_FUNCTION_USED, Boolean.class)) {
                    testEnvironment.setProperty(RESULT, o);
                }
            });
            testEnvironment.setProperty(EXPECTED_RESULT, testRequest);
            return testRequest;
        };
        final Supplier<Object> instantiationFunction = VoidReturnUseCase::new;
        final Consumer<UseCaseAdapterStep3Builder<?, ?>> parameterMapping = callingBuilder -> {
            callingBuilder.calling((useCaseInstance, event) -> {
                final VoidReturnUseCase useCase = (VoidReturnUseCase) useCaseInstance;
                final CallbackTestRequest callbackTestRequest = (CallbackTestRequest) event;
                useCase.useCaseMethod(callbackTestRequest);
                return null;
            });
        };
        final Function<TestEnvironment, Object> expectedResultSupploer = testEnvironment -> {
            if (testEnvironment.getPropertyAsType(MESSAGE_FUNCTION_USED, Boolean.class)) {
                return null;
            } else {
                return testEnvironment.getProperty(EXPECTED_RESULT);
            }
        };
        return testUseCase(USE_CASE_CLASS, EventClass, messageBusSetup, instantiationFunction, parameterMapping, requestObjectFunction, expectedResultSupploer);
    }
}
