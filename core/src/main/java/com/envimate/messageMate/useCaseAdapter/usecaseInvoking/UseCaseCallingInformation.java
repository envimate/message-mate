package com.envimate.messageMate.useCaseAdapter.usecaseInvoking;

import com.envimate.messageMate.useCaseAdapter.methodInvoking.ParameterValueMappings;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static com.envimate.messageMate.internal.enforcing.NotNullEnforcer.ensureNotNull;
import static lombok.AccessLevel.PRIVATE;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = PRIVATE)
public final class UseCaseCallingInformation<USECASE, EVENT> {
    @Getter
    private final Class<USECASE> useCaseClass;
    @Getter
    private final Class<EVENT> eventClass;
    @Getter
    private final Caller<USECASE, EVENT> caller;
    @Getter
    private final ParameterValueMappings parameterValueMappings;

    public static <USECASE, EVENT> UseCaseCallingInformation<USECASE, EVENT> useCaseInvocationInformation(
            final Class<USECASE> useCaseClass,
            final Class<EVENT> eventClass,
            final Caller<USECASE, EVENT> caller,
            final ParameterValueMappings parameterValueMappings) {
        ensureNotNull(useCaseClass, "useCaseClass");
        ensureNotNull(eventClass, "eventClass");
        ensureNotNull(caller, "caller");
        return new UseCaseCallingInformation<>(useCaseClass, eventClass, caller, parameterValueMappings);
    }
}
