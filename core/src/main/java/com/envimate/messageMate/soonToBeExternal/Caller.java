package com.envimate.messageMate.soonToBeExternal;

import java.util.Optional;

@FunctionalInterface
public interface Caller<USECASE, EVENT> {
    Optional<?> call(USECASE useCase, EVENT event);
}
