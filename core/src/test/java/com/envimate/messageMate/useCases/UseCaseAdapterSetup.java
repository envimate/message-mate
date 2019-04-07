package com.envimate.messageMate.useCases;

import com.envimate.messageMate.messageBus.MessageBus;
import com.envimate.messageMate.qcec.shared.TestEnvironment;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
final class UseCaseAdapterSetup {
    @Getter
    private final TestEnvironment testEnvironment;
    @Getter
    private final TestUseCase testUseCase;
    @Getter
    private final MessageBus messageBus;
}
