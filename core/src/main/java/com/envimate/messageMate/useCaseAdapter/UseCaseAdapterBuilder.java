package com.envimate.messageMate.useCaseAdapter;

import com.envimate.messageMate.messageBus.EventType;
import com.envimate.messageMate.useCaseAdapter.building.*;
import com.envimate.messageMate.useCaseAdapter.mapping.RequestDeserializer;
import com.envimate.messageMate.useCaseAdapter.mapping.RequestMapper;
import com.envimate.messageMate.useCaseAdapter.mapping.filtermap.FilterMapBuilder;
import com.envimate.messageMate.useCaseAdapter.methodInvoking.ParameterValueMappings;
import com.envimate.messageMate.useCaseAdapter.usecaseInstantiating.UseCaseInstantiator;
import com.envimate.messageMate.useCaseAdapter.usecaseInvoking.Caller;
import com.envimate.messageMate.useCaseAdapter.usecaseInvoking.UseCaseCallingInformation;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;

import static com.envimate.messageMate.useCaseAdapter.UseCaseAdapterImpl.useCaseAdapterImpl;
import static com.envimate.messageMate.useCaseAdapter.mapping.filtermap.FilterMapBuilder.filterMapBuilder;
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
        return eventType -> new MappingBuilder<>(UseCaseAdapterBuilder.this, useCaseClass, eventType);
    }

    @Override
    public UseCaseAdapter obtainingUseCaseInstancesUsing(final UseCaseInstantiator useCaseInstantiator) {
        return useCaseAdapterImpl(useCaseCallingInformations, useCaseInstantiator);
    }

    @RequiredArgsConstructor(access = PRIVATE)
    private final class MappingBuilder<USECASE> implements UseCaseAdapterStep3Builder<USECASE>, UseCaseAdapterCallingBuilder<USECASE> {
        private final FilterMapBuilder<Class<?>, Map<String, Object>, RequestMapper<?>> requestMappers = filterMapBuilder();
        private final UseCaseAdapterBuilder wrappingBuilder;
        private final Class<USECASE> useCaseClass;
        private final EventType eventType;

        @Override
        public <X> Using<DeserializationStage<UseCaseAdapterCallingBuilder<USECASE>>, RequestMapper<X>> mappingRequestsToUseCaseParametersThat(final BiPredicate<Class<?>, Map<String, Object>> filter) {
            return mapper -> {
                requestMappers.put(filter, mapper);
                return this;
            };
        }

        @Override
        public UseCaseAdapterCallingBuilder<USECASE> mappingRequestsToUseCaseParametersByDefaultUsing(final RequestMapper<Object> mapper) {
            requestMappers.setDefaultValue(mapper);
            return this;
        }

        @Override
        public UseCaseAdapterStep1Builder callingBy(Caller<USECASE> caller) {
            final RequestDeserializer requestDeserializer = RequestDeserializer.requestDeserializer(requestMappers.build());
            final UseCaseCallingInformation<USECASE> invocationInformation = useCaseInvocationInformation(useCaseClass, eventType, caller, requestDeserializer);
            wrappingBuilder.useCaseCallingInformations.add(invocationInformation);
            return wrappingBuilder;
        }
    }
}
