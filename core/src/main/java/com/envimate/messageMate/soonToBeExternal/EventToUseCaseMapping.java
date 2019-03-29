package com.envimate.messageMate.soonToBeExternal;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static com.envimate.messageMate.internal.enforcing.NotNullEnforcer.ensureNotNull;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class EventToUseCaseMapping<USECASE, EVENT> {
    public final Class<USECASE> useCaseClass;
    public final Class<EVENT> eventClass;
    public final Caller<USECASE, EVENT> caller;

    public static <USECASE, EVENT> EventToUseCaseMapping<USECASE, EVENT> eventToUseCaseMapping(final Class<USECASE> useCaseClass,
                                                                                               final Class<EVENT> eventClass,
                                                                                               final Caller<USECASE, EVENT> caller) {
        ensureNotNull(useCaseClass, "useCaseClass");
        ensureNotNull(eventClass, "eventClass");
        ensureNotNull(caller, "caller");
        return new EventToUseCaseMapping<>(useCaseClass, eventClass, caller);
    }
}
