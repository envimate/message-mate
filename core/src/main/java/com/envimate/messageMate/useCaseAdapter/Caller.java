package com.envimate.messageMate.useCaseAdapter;

import java.util.Optional;

@FunctionalInterface
public interface Caller<USECASE, EVENT> {
    Optional<?> call(USECASE useCase, EVENT event);
}
