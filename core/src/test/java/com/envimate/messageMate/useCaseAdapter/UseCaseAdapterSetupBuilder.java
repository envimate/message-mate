package com.envimate.messageMate.useCaseAdapter;

import com.envimate.messageMate.internal.pipe.configuration.AsynchronousConfiguration;
import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import com.envimate.messageMate.soonToBeExternal.building.EventToUseCaseDispatcherStep3Builder;
import com.envimate.messageMate.useCaseAdapter.useCases.noParameter.NoParameterUseCase;
import com.envimate.messageMate.useCaseAdapter.useCases.noReturnValue.CallbackTestRequest;
import com.envimate.messageMate.useCaseAdapter.useCases.noReturnValue.NoReturnValueUseCase;
import com.envimate.messageMate.useCaseAdapter.useCases.singleObjectParameter.SingleObjectParameterUseCase;
import com.envimate.messageMate.useCaseAdapter.useCases.singleObjectParameter.TestUseCaseRequest;

import static com.envimate.messageMate.internal.pipe.configuration.AsynchronousConfiguration.constantPoolSizeAsynchronousPipeConfiguration;
import static com.envimate.messageMate.messageBus.MessageBusBuilder.aMessageBus;
import static com.envimate.messageMate.messageBus.MessageBusType.ASYNCHRONOUS;
import static com.envimate.messageMate.qcec.shared.TestEnvironment.emptyTestEnvironment;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.MOCK;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.SUT;

public class UseCaseAdapterSetupBuilder {
    private final TestEnvironment testEnvironment = emptyTestEnvironment();
    private final EventToUseCaseDispatcherStep3Builder useCaseAdapterBuilder = UseCaseAdapterBuilder.anUseCaseAdapter();

    public static UseCaseAdapterSetupBuilder aUseCaseAdapter() {
        return new UseCaseAdapterSetupBuilder();
    }

    public UseCaseAdapterSetupBuilder withAUseCaseWithASingleTestEventAsParameter() {
        useCaseAdapterBuilder.invokingUseCase(SingleObjectParameterUseCase.class)
                .forEvent(TestUseCaseRequest.class)
                .callingTheSingleUseCaseMethod();
        return this;
    }

    public UseCaseAdapterSetupBuilder withAUseCaseWithoutParameter() {
        useCaseAdapterBuilder.invokingUseCase(NoParameterUseCase.class)
                .forEvent(TestUseCaseRequest.class)
                .callingTheSingleUseCaseMethod();
        return this;
    }

    public UseCaseAdapterSetupBuilder withAUseCaseWithoutReturnValue() {
        useCaseAdapterBuilder.invokingUseCase(NoReturnValueUseCase.class)
                .forEvent(CallbackTestRequest.class)
                .callingTheSingleUseCaseMethod();
        return this;
    }

    public TestEnvironment build() {
        final UseCaseAdapter useCaseAdapter = useCaseAdapterBuilder.obtainingUseCaseInstancesUsingTheZeroArgumentConstructor();
        testEnvironment.setProperty(SUT, useCaseAdapter);
        final AsynchronousConfiguration asynchronousConfiguration = constantPoolSizeAsynchronousPipeConfiguration(3);
        final MessageBus messageBus = aMessageBus()
                .forType(ASYNCHRONOUS)
                .withAsynchronousConfiguration(asynchronousConfiguration)
                .build();
        useCaseAdapter.attachTo(messageBus);
        testEnvironment.setProperty(MOCK, messageBus);
        return testEnvironment;
    }
}
