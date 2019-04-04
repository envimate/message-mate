package com.envimate.messageMate.useCaseAdapter;

import com.envimate.messageMate.messageBus.EventType;
import com.envimate.messageMate.useCaseAdapter.building.UseCaseAdapterStep1Builder;
import com.envimate.messageMate.useCaseAdapter.building.UseCaseAdapterStep2Builder;
import com.envimate.messageMate.useCaseAdapter.building.UseCaseAdapterStep3Builder;
import com.envimate.messageMate.useCaseAdapter.methodInvoking.ParameterValueMappings;
import com.envimate.messageMate.useCaseAdapter.usecaseInstantiating.UseCaseInstantiator;
import com.envimate.messageMate.useCaseAdapter.usecaseInvoking.Caller;
import com.envimate.messageMate.useCaseAdapter.usecaseInvoking.UseCaseCallingInformation;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import static com.envimate.messageMate.useCaseAdapter.UseCaseAdapterImpl.useCaseAdapterImpl;
import static com.envimate.messageMate.useCaseAdapter.methodInvoking.ParameterValueMappings.emptyParameterValueMappings;
import static com.envimate.messageMate.useCaseAdapter.usecaseInvoking.UseCaseCallingInformation.useCaseInvocationInformation;
import static lombok.AccessLevel.PRIVATE;

public class UseCaseAdapterBuilder implements UseCaseAdapterStep1Builder {
    private final List<UseCaseCallingInformation> useCaseCallingInformations = new LinkedList<>();

    public static UseCaseAdapterStep1Builder anUseCaseAdapter() {
        return new UseCaseAdapterBuilder();
    }

    //TODO: registerUseCase + alles andere optional

    @Override
    public <USECASE> UseCaseAdapterStep2Builder<USECASE> invokingUseCase(final Class<USECASE> useCaseClass) {
        return new UseCaseAdapterStep2Builder<USECASE>() {

            @Override
            public UseCaseAdapterStep3Builder<USECASE> forType(EventType eventType) {
                return new MappingBuilder<>(UseCaseAdapterBuilder.this, useCaseClass, eventType);
            }

        };
    }


    @Override
    public UseCaseAdapter obtainingUseCaseInstancesUsing(final UseCaseInstantiator useCaseInstantiator) {
        return useCaseAdapterImpl(useCaseCallingInformations, useCaseInstantiator);
    }

    @RequiredArgsConstructor(access = PRIVATE)
    private final class MappingBuilder<USECASE> implements UseCaseAdapterStep3Builder<USECASE> {
        private final ParameterValueMappings parameterValueMappings = emptyParameterValueMappings();
        private final UseCaseAdapterBuilder wrappingBuilder;
        private final Class<USECASE> useCaseClass;
        private final EventType eventType;


        @Override
        public <PARAM> UseCaseAdapterStep3Builder<USECASE> mappingEventToParameter(Class<PARAM> paramClass, Function<Object, Object> mapping) {
            parameterValueMappings.registerMapping(paramClass, event -> mapping.apply(event));
            return this;
        }

        @Override
        public UseCaseAdapterStep1Builder callingBy(Caller<USECASE> caller) {
            final UseCaseCallingInformation<USECASE> invocationInformation = useCaseInvocationInformation(useCaseClass, eventType, caller, parameterValueMappings);
            wrappingBuilder.useCaseCallingInformations.add(invocationInformation);
            return wrappingBuilder;
        }
    }
}
