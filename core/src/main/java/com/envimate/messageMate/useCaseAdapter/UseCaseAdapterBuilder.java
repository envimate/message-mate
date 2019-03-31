package com.envimate.messageMate.useCaseAdapter;

import com.envimate.messageMate.soonToBeExternal.building.UseCaseAdapterStep1Builder;
import com.envimate.messageMate.soonToBeExternal.building.UseCaseAdapterStep2Builder;
import com.envimate.messageMate.soonToBeExternal.building.UseCaseAdapterStep3Builder;
import com.envimate.messageMate.soonToBeExternal.methodInvoking.ParameterValueMappings;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import static com.envimate.messageMate.soonToBeExternal.methodInvoking.ParameterValueMappings.emptyParameterValueMappings;
import static com.envimate.messageMate.useCaseAdapter.UseCaseAdapterImpl.useCaseAdapterImpl;
import static com.envimate.messageMate.useCaseAdapter.UseCaseInvocationInformation.useCaseInvocationInformation;

public class UseCaseAdapterBuilder implements UseCaseAdapterStep1Builder {
    private final List<UseCaseInvocationInformation> useCaseInvocationInformations = new LinkedList<>();

    public static UseCaseAdapterStep1Builder anUseCaseAdapter() {
        return new UseCaseAdapterBuilder();
    }

    @Override
    public <USECASE> UseCaseAdapterStep2Builder<USECASE> invokingUseCase(final Class<USECASE> useCaseClass) {
        final ParameterValueMappings parameterValueMappings = emptyParameterValueMappings();
        return new UseCaseAdapterStep2Builder<USECASE>() {

            @Override
            public <EVENT> UseCaseAdapterStep3Builder<USECASE, EVENT> forEvent(Class<EVENT> eventClass) {
                return new UseCaseAdapterStep3Builder<USECASE, EVENT>() {
                    @Override
                    public <PARAM> UseCaseAdapterStep3Builder<USECASE, EVENT> mappingEventToParameter(Class<PARAM> paramClass, Function<EVENT, Object> mapping) {
                        parameterValueMappings.registerMapping(paramClass, event -> mapping.apply((EVENT) event));
                        return this;
                    }

                    @Override
                    public UseCaseAdapterStep1Builder callingBy(Caller<USECASE, EVENT> caller) {
                        useCaseInvocationInformations.add(useCaseInvocationInformation(useCaseClass, eventClass, caller));
                        return UseCaseAdapterBuilder.this;
                    }
                };
            }

        };
    }


    @Override
    public UseCaseAdapter obtainingUseCaseInstancesUsing(final UseCaseInstantiator useCaseInstantiator) {
        return useCaseAdapterImpl(useCaseInvocationInformations, useCaseInstantiator);
    }
}
