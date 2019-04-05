package com.envimate.messageMate.useCaseAdapter.usecaseInvoking;

import com.envimate.messageMate.messageBus.EventType;
import com.envimate.messageMate.useCaseAdapter.mapping.RequestDeserializer;
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
public final class UseCaseCallingInformation<USECASE> {
    @Getter
    private final Class<USECASE> useCaseClass;
    @Getter
    private final EventType eventType;
    @Getter
    private final Caller<USECASE> caller;
    @Getter
    private final RequestDeserializer requestDeserializer;

    public static <USECASE> UseCaseCallingInformation<USECASE> useCaseInvocationInformation(
            final Class<USECASE> useCaseClass,
            final EventType eventType,
            final Caller<USECASE> caller,
            final RequestDeserializer requestDeserializer) {
        ensureNotNull(useCaseClass, "useCaseClass");
        ensureNotNull(eventType, "eventType");
        ensureNotNull(caller, "caller");
        ensureNotNull(requestDeserializer, "requestDeserializer");
        return new UseCaseCallingInformation<>(useCaseClass, eventType, caller, requestDeserializer);
    }
}
