package com.envimate.messageMate.useCaseConnecting;

import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class Given {
    public static When given(final UseCaseConnectorSetupBuilder useCaseConnectorSetupBuilder) {
        return new When(useCaseConnectorSetupBuilder);
    }
}
