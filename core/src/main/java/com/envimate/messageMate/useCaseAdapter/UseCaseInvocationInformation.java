package com.envimate.messageMate.useCaseAdapter;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static com.envimate.messageMate.internal.enforcing.NotNullEnforcer.ensureNotNull;
import static lombok.AccessLevel.PRIVATE;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = PRIVATE)
public final class UseCaseInvocationInformation<USECASE, EVENT> {
    public final Class<USECASE> useCaseClass;
    public final Class<EVENT> eventClass;
    public final Caller<USECASE, EVENT> caller;

    public static <USECASE, EVENT> UseCaseInvocationInformation<USECASE, EVENT> useCaseInvocationInformation(
            final Class<USECASE> useCaseClass,
            final Class<EVENT> eventClass,
            final Caller<USECASE, EVENT> caller) {
        ensureNotNull(useCaseClass, "useCaseClass");
        ensureNotNull(eventClass, "eventClass");
        ensureNotNull(caller, "caller");
        return new UseCaseInvocationInformation<>(useCaseClass, eventClass, caller);
    }
}
