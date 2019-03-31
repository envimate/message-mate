package com.envimate.messageMate.useCaseAdapter.severalParameter;

import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.shared.config.AbstractTestConfigProvider;
import com.envimate.messageMate.useCaseAdapter.TestUseCase;
import com.envimate.messageMate.useCaseAdapter.building.UseCaseAdapterStep3Builder;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.RESULT;
import static com.envimate.messageMate.useCaseAdapter.TestUseCase.testUseCase;

public class SeveralParameterConfigurationResolver extends AbstractTestConfigProvider {

    public static final Class<?> USE_CASE_CLASS = SeveralParameterUseCase.class;
    public static final Class<?> EventClass = SeveralParameterUseCaseRequest.class;

    @Override
    protected Class<?> forConfigClass() {
        return TestUseCase.class;
    }

    @Override
    protected Object testConfig() {
        final BiConsumer<MessageBus, TestEnvironment> messageBusSetup = (messageBus, testEnvironment) -> {
            messageBus.subscribe(SeveralParameterUseCaseResponse.class, severalParameterUseCaseResponse -> {
                testEnvironment.setPropertyIfNotSet(RESULT, severalParameterUseCaseResponse);
            });
        };
        final SeveralParameterUseCaseRequest requestObject = new SeveralParameterUseCaseRequest("1", new Object(), 5, true);
        final SeveralParameterUseCaseResponse expectedResult = createExpectedResponse(requestObject);
        final Supplier<Object> instantiationFunction = SeveralParameterUseCase::new;
        final Consumer<UseCaseAdapterStep3Builder<?, ?>> parameterMapping = callingBuilder -> {
            callingBuilder.mappingEventToParameter(String.class, o -> ((SeveralParameterUseCaseRequest) o).stringParameter);
            callingBuilder.mappingEventToParameter(Object.class, o -> ((SeveralParameterUseCaseRequest) o).objectParameter);
            callingBuilder.mappingEventToParameter(int.class, o -> ((SeveralParameterUseCaseRequest) o).intParameter);
            callingBuilder.mappingEventToParameter(Boolean.class, o -> ((SeveralParameterUseCaseRequest) o).booleanParameter);
        };
        final Consumer<UseCaseAdapterStep3Builder<?, ?>> customCallingLogic = callingBuilder -> {
            callingBuilder.calling((useCaseInstance, event) -> {
                final SeveralParameterUseCase useCase = (SeveralParameterUseCase) useCaseInstance;
                final SeveralParameterUseCaseRequest request = (SeveralParameterUseCaseRequest) event;
                return useCase.useCaseMethod(request.stringParameter, request.objectParameter, request.intParameter, request.booleanParameter);
            });
        };
        return testUseCase(USE_CASE_CLASS, EventClass, messageBusSetup, instantiationFunction, parameterMapping, customCallingLogic, requestObject, expectedResult);
    }

    private SeveralParameterUseCaseResponse createExpectedResponse(SeveralParameterUseCaseRequest requestObject) {
        final String stringParameter = requestObject.stringParameter;
        final Object objectParameter = requestObject.objectParameter;
        final int intParameter = requestObject.intParameter;
        final Boolean booleanParameter = requestObject.booleanParameter;
        return new SeveralParameterUseCaseResponse(stringParameter, objectParameter, intParameter, booleanParameter);
    }
}
