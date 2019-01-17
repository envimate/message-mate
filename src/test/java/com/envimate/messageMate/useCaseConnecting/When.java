package com.envimate.messageMate.useCaseConnecting;

import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PACKAGE;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PACKAGE)
public final class When {
    private final UseCaseConnectorSetupBuilder useCaseConnectorSetupBuilder;

    public Then when(final UseCaseConnectorActionBuilder useCaseConnectorActionBuilder) {
        return new Then(useCaseConnectorSetupBuilder, useCaseConnectorActionBuilder);
    }
}
