package com.envimate.messageMate.useCaseConnecting;

import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import lombok.RequiredArgsConstructor;

import static com.envimate.messageMate.messageBus.MessageBusBuilder.aMessageBus;
import static com.envimate.messageMate.qcec.shared.TestEnvironment.emptyTestEnvironment;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.CONTROLLABLE_ENV_OBJECT;
import static com.envimate.messageMate.qcec.shared.TestEnvironmentProperty.SUT;
import static com.envimate.messageMate.useCaseConnecting.UseCaseConnectorBuilder.aUseCaseConnector;
import static com.envimate.messageMate.useCaseConnecting.useCase.UseCaseInvoker.useCase;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class UseCaseConnectorSetupBuilder {
    private final TestEnvironment testEnvironment;

    public static UseCaseConnectorSetupBuilder aConfiguredUseCaseConnector() {
        final TestEnvironment testEnvironment = emptyTestEnvironment();
        final MessageBus messageBus = aMessageBus()
                .build();
        testEnvironment.setProperty(CONTROLLABLE_ENV_OBJECT, messageBus);
        final UseCaseConnector useCaseConnector = aUseCaseConnector()
                .delivering(SampleRequest.class)
                .to(useCase(SampleUseCase.class))
                .using(messageBus);
        testEnvironment.setProperty(SUT, useCaseConnector);
        return new UseCaseConnectorSetupBuilder(testEnvironment);
    }

    public TestEnvironment build() {
        return testEnvironment;
    }
}
